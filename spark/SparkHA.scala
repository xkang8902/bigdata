import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils}

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder

import scalikejdbc._
import scalikejdbc.config._

object App {
  private val brokers = "hadoop01:9092"
  private val topics = "test01"
  private val checkPointPath = "hdfs://hadoop01:9000/sparkStreaming/kafka"

  def main(args: Array[String]): Unit = {

    val ssc = StreamingContext.getOrCreate(checkPointPath,()=>{
    val spark = getSparkSession
    val ssc = new StreamingContext(spark.sparkContext, Seconds(3))
    ssc.checkpoint(checkPointPath)
    val kafkaInputDstream = getInputDstream(ssc, checkPointPath, topics, brokers)
 
    /**  业务逻辑位置
        val result=kafkaInputDstream.map(x => {x.value()}).flatMap(x => {
        x.split(" ").map(x => {(x, 1)})
        }).reduceByKey(_ + _)
        result.print()

        OffsetManager.saveOffsets(kafkaInputDstream)
    */

      ssc
    })

    //sparkstreaming 优雅的关闭方式
    // 方式一： 通过Http方式优雅的关闭策略
    GraceCloseUtils.daemonHttpServer(8012,ssc)
    // 方式二： 通过扫描HDFS文件来优雅的关闭
    // GraceCloseUtils.stopByMarkFile(ssc)

    ssc.start()
    ssc.awaitTermination()
  }

    def getSparkSession(): SparkSession = {
        SparkSession.builder().
            appName("StreamingTest").
            master("local[4]").
            config("spark.serializer", "org.apache.spark.serializer.KryoSerializer").
            config("park.streaming.stopGracefullyOnShutdown",true).
            /* Spark Streaming 重启后Kafka数据堆积调优 */
            config("spark.streaming.backpressure.enabled", true). // 激活反压功能
            config("spark.streaming.backpressure.initialRate", 5000). // 启动反压功能后，读取的最大数据量
            config("spark.streaming.kafka.maxRatePerPartition", 2000). // 设置每秒每个分区最大获取日志数，控制处理数据量，保证数据均匀处理。
            getOrCreate()
    }

    def getInputDstream(ssc: StreamingContext, checkPointPath: String, topics: String, brokers: String): InputDStream[ConsumerRecord[String, String]] = {
        val topicArray = topics.split(",").toList
        val kafkaParams = Map[String, Object](
            "bootstrap.servers" -> brokers,
            "key.deserializer" -> classOf[StringDeserializer],
            "value.deserializer" -> classOf[StringDeserializer],
            "group.id" -> "groupid",
            "auto.offset.reset" -> "latest",
            "enable.auto.commit" -> (false: java.lang.Boolean)
        )
        DBs.setup()
        /**
            mysql> create table tb_offset(
            topic varchar(32),
            groupid varchar(50),
            partitions int,
            fromoffset bigint,
            untiloffset bigint,
            primary key(topic,groupid,partitions)
            );
        */
        /**
            <dependency>
                <groupId>org.scalikejdbc</groupId>
                <artifactId>scalikejdbc_2.11</artifactId>
                <version>2.5.0</version>
            </dependency>
            <dependency>
                <groupId>org.scalikejdbc</groupId>
                <artifactId>scalikejdbc-config_2.11</artifactId>
                <version>2.5.0</version>
            </dependency>
            import scalikejdbc._
            import scalikejdbc.config._
        
            先使用scalikejdbc从MySQL数据库中读取offset信息
            +------------+------------------+------------+------------+-------------+
            | topic      | groupid          | partitions | fromoffset | untiloffset |
            +------------+------------------+------------+------------+-------------+
            MySQL表结构如上，将“topic”，“partitions”，“untiloffset”列读取出来
            组成 fromOffsets: Map[TopicAndPartition, Long]，后面createDirectStream用到
        */
        val fromOffset = DB.readOnly( implicit session => {
            SQL("select * from tb_offset").map(rs => {
            (TopicAndPartition(rs.string("topic"),rs.int("partitions")),rs.long("untiloffset"))
            }).list().apply()
        }).toMap
        //如果MySQL表中没有offset信息，就从0开始消费；如果有，就从已经存在的offset开始消费
        val messages = if (fromOffset.isEmpty) {
            println("从头开始消费...")
            KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,topics)
        } else {
            println("从已存在记录开始消费...")
            val messageHandler = (mm:MessageAndMetadata[String,String]) => (mm.key(),mm.message())
            KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder,(String,String)](ssc,kafkaParams,fromOffset,messageHandler)
        }

        /**
        val messages = KafkaUtils.createDirectStream[String, String](
            ssc,
            LocationStrategies.PreferConsistent,
            ConsumerStrategies.Subscribe[String, String](topicArray, kafkaParams)
        )
        */
        messages
    }
}

object GraceCloseUtils {
    lazy val log = LogManager.getLogger("GraceCloseUtils")
    /**
    * 1. HTTP方式
    * 负责启动守护的jetty服务
    * @param port 对外暴露的端口号
    * @param ssc Stream上下文
    */
    def daemonHttpServer(port:Int, ssc: StreamingContext) = {
        val server = new Server(port)
        val context = new ContextHandler()
        context.setContextPath("/close")
        context.setHandler(new CloseStreamHandler(ssc))
        server.setHandler(context)
        server.start()
    }
    /**
    * 负责接受http请求来优雅的关闭流
    * @param ssc Stream上下文
    */
    class CloseStreamHandler(ssc:StreamingContext) extends AbstractHandler {
        override def handle(s: String, baseRequest: Request, req: HttpServletRequest, response: HttpServletResponse): Unit = {
            log.warn("开始关闭......")
            // 优雅的关闭
            ssc.stop(true, true)
            response.setContentType("text/html; charset=utf-8")
            response.setStatus(HttpServletResponse.SC_OK)
            val out = response.getWriter
            out.println("Close Success")
            baseRequest.setHandled(true)
            log.warn("关闭成功.....")
        }
    }
    /**
    * 2. HDFS文件检测方式
    * 通过一个消息文件来定时触发是否需要关闭流程序
    * @param ssc StreamingContext
    */
    def stopByMarkFile(ssc:StreamingContext): Unit = {
        val intervalMills = 10 * 1000 // 每隔10秒扫描一次消息是否存在
        var isStop = false
        val hdfsFilePath = "/spark/streaming/stop" // 判断消息文件是否存在
        while (!isStop) {
            isStop = ssc.awaitTerminationOrTimeout(intervalMills)
            if (! isStop && isExistsMarkFile(hdfsFilePath)) {
                log.warn("2秒后开始关闭sparstreaming程序.....")
                Thread.sleep(2000)
                ssc.stop(true, true)
            }
        }
    }
    /**
    * 判断是否存在mark file
    * @param hdfsFilePath mark文件的路径
    * @return
    */
    def isExistsMarkFile(hdfsFilePath: String): Boolean = {
        val conf = new Configuration()
        val path = new Path(hdfsFilePath)
        val fs = path.getFileSystem(conf)
        fs.exists(path)
    }

}

object OffsetManager{

    def saveOffsets(inputDStream:InputDStream): Unit = {
        DBs.setup()
        inputDstream.foreachRDD(rdd=>{
            if(!rdd.isEmpty()){
                //输出rdd的数据量
                println("数据统计记录为："+rdd.count())
                //官方案例给出的获得rdd offset信息的方法，offsetRanges是由一系列offsetRange组成的数组
                // trait HasOffsetRanges {
                //     def offsetRanges: Array[OffsetRange]
                //  }
                val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
                offsetRanges.foreach(x => {
                    //输出每次消费的主题，分区，开始偏移量和结束偏移量
                    println(s"---${x.topic},${x.partition},${x.fromOffset},${x.untilOffset}---")
                    //将最新的偏移量信息保存到MySQL表中
                    DB.autoCommit( implicit session => {
                        SQL("replace into tb_offset(topic,groupid,partitions,fromoffset,untiloffset) values (?,?,?,?,?)")
                        .bind(x.topic,"groupid",x.partition,x.fromOffset,x.untilOffset)
                        .update().apply()
                    })
                })
            }
        })
        
    }



}