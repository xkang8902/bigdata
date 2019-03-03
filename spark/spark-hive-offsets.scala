object FamilyBandService {
    val logger = LoggerFactory.getLogger(this.getClass)
    def main(args: Array[String]): Unit = {
        val conf =new SparkConf()
            .setAppName(s"${this.getClass.getSimpleName}")
            .setMaster("local[*]")
        conf.set("spark.defalut.parallelism","500")
            //每秒钟每个分区kafka拉取消息的速率      
            .set("spark.streaming.kafka.maxRatePerPartition","500")
            // 序列化      
            .set("spark.serilizer","org.apache.spark.serializer.KryoSerializer")
            // 建议开启rdd的压缩      
            .set("spark.rdd.compress","true")
        val sc =new SparkContext(conf)
        val ssc =new StreamingContext(sc,Seconds(3))
        val brokers = PropertyUtil.getInstance().getProperty("brokerList","")  
        //配置文件读取工具类需自行编写
        val groupId = PropertyUtil.getInstance().getProperty("groupid","")
        val topic = PropertyUtil.getInstance().getProperty("topic","")
        var topics =Array(topic)
        Logger.getLogger("org").setLevel(Level.ERROR) //临时测试的时候只开启error级别，方便排错。
        //封装参数    
        val kafkaParams =Map[String, Object](
            "bootstrap.servers" -> brokers,
            "key.deserializer" ->classOf[StringDeserializer],
            "value.deserializer" ->classOf[StringDeserializer],
            "group.id" -> groupId,
            "auto.offset.reset" -> "latest",
            "enable.auto.commit" -> (false: java.lang.Boolean))
        //从redis中获取到偏移量    
        val offsets: Long = RedisUtil.hashGet("offset","offsets").toLong
        val topicPartition: TopicPartition =new TopicPartition(topic,0)
        val partitionoffsets:Map[TopicPartition, Long] = Map(topicPartition -> offsets)
        //获取到实时流对象    
        val kafkaStream = if (offsets ==0) {
            KafkaUtils.createDirectStream[String,String](
                ssc,
                PreferConsistent,  
                //这里有3种模式,一般情况下，都是使用PreferConsistent
                //LocationStrategies.PreferConsistent：将在可用的执行器之间均匀分配分区。
                //PreferBrokers  执行程序与Kafka代理所在的主机相同，将更喜欢在该分区的Kafka leader上安排分区
                //PreferFixed 如果您在分区之间的负载有显着偏差，这允许您指定分区到主机的显式映射（任何未指定的分区将使用一致的位置）。
                Subscribe[String,String](topics, kafkaParams) //消息订阅
        )} else {
            KafkaUtils.createDirectStream[String,String](
                ssc,
                PreferConsistent,
                Subscribe[String,String](topics, kafkaParams, partitionoffsets)//此种方式是针对具体某个分区或者topic只有一个分区的情况
                )
        }
                //业务处理    
            kafkaStream.foreachRDD(rdd => {
                val ranges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges  
                //获取到分区和偏移量信息
                val events: RDD[Some[String]] = rdd.map(x => {
                    val data = x.value()
                    Some(data)})
                val session = SQLContextSingleton.getSparkSession(events.sparkContext)  
                //构建一个Sparksession的单例
                session.sql("set hive.exec.dynamic.partition=true")      
                //配置hive支持动态分区
                session.sql("set hive.exec.dynamic.partition.mode=nonstrict")   
                //配置hive动态分区为非严格模式
                //如果将数据转换为Seq(xxxx),然后倒入隐式转换import session.implicalit._  是否能实现呢，答案是否定的。
                val dataRow = events.map(line => {                                           
                    //构建row
                    val temp = line.get.split("###")                                                   
                    Row(temp(0), temp(1), temp(2), temp(3), temp(4), temp(5))})
                    //"deviceid","code","time","info","sdkversion","appversion" 
                val structType =StructType(Array(                          
                    //确定字段的类别
                    StructField("deviceid", StringType,true),
                    StructField("code", StringType,true),
                    StructField("time", StringType,true),
                    StructField("info", StringType,true),
                    StructField("sdkversion", StringType,true),
                    StructField("appversion", StringType,true)))
                val df = session.createDataFrame(dataRow, structType)   
                //构建df
                df.createOrReplaceTempView("jk_device_info")
                session.sql("insert into test.jk_device_info select * from jk_device_info")
                for (rs <- ranges) {//实时保存偏移量到redis        
                    val value = rs.untilOffset.toString
                    RedisUtil.hashSet("offset","offsets", value)   
                    //偏移量保存
                    println(s"the offset:${value}")
                }
            })
        println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
        ssc.start()
        ssc.awaitTermination()
    }
}
//上面说到了单分区情况下的实时写入hive的情况，此种情况要求数据具有很高的时序性，
//但是并发会受到影响。那么我们采用多分区的情况下，如何实现呢，代码如下：
//单分区情况  
//从redis中获取到偏移量   redis工具类需要自行编写
val offsets: Long = RedisUtil.hashGet("offset", "offsets").toLong    
val topicPartition: TopicPartition = new TopicPartition(topic, 0)
val partitionoffsets: Map[TopicPartition, Long] = Map(topicPartition -> offsets)
//多分区情况
val partitions = 3 
var fromdbOffset =Map[TopicPartition, Long]()
for (partition <- 0 until partitions) {
    val topicPartition =new TopicPartition(topic, partition)
    val offsets = RedisUtil.hashGet("offset",s"${topic}_${partition}").toLong
    fromdbOffset += (topicPartition -> offsets)
}
    //获取到实时流对象    
val kafkaStream = if(fromdbOffset.size == 0) {
    KafkaUtils.createDirectStream[String,String](
        ssc,
        PreferConsistent,
        Subscribe[String,String](topics, kafkaParams))
}else {
    KafkaUtils.createDirectStream[String,String](
        ssc,
        PreferConsistent,
        // Subscribe[String, String](topics, kafkaParams, partitionoffsets) 订阅具体某个分区        
        ConsumerStrategies.Assign[String,String](fromdbOffset.keys, kafkaParams, fromdbOffset))
}
/*
    针对Assign 和 Subscribe ，我们来看下官方源码
    Assign：2个参数
    def Assign[K,V](
        topicPartitions:Iterable[TopicPartition],
        kafkaParams: collection.Map[String, Object]): ConsumerStrategy[K,V] = {
            new Assign[K,V](new ju.ArrayList(
                topicPartitions.asJavaCollection),
                new ju.HashMap[String, Object](kafkaParams.asJava),
                ju.Collections.emptyMap[TopicPartition, jl.Long]()
            )}
    3个参数
    def Assign[K,V](
        topicPartitions:Iterable[TopicPartition],
        kafkaParams: collection.Map[String, Object],
        offsets: collection.Map[TopicPartition, Long]): ConsumerStrategy[K,V] = {
            new Assign[K,V](
                new ju.ArrayList(topicPartitions.asJavaCollection),
                new ju.HashMap[String, Object](kafkaParams.asJava),
                new ju.HashMap[TopicPartition, jl.Long](offsets.mapValues(l => new jl.Long(l)).asJava))
    }
    Subscribe ：2个参数
    def Subscribe[K,V](
        topics:Iterable[jl.String],
        kafkaParams: collection.Map[String, Object]): ConsumerStrategy[K,V] = {
            new Subscribe[K,V](
                new ju.ArrayList(topics.asJavaCollection),
                new ju.HashMap[String, Object](kafkaParams.asJava),
                ju.Collections.emptyMap[TopicPartition, jl.Long]())
    }
    3个参数
    def Subscribe[K,V](
        topics:Iterable[jl.String],
        kafkaParams: collection.Map[String, Object],
        offsets: collection.Map[TopicPartition, Long]): ConsumerStrategy[K,V] = {
            new Subscribe[K,V](
                new ju.ArrayList(topics.asJavaCollection),
                new ju.HashMap[String, Object](kafkaParams.asJava),
                new ju.HashMap[TopicPartition, jl.Long](offsets.mapValues(l => new jl.Long(l)).asJava))}
    经过对比，我们发现，区别就在第一个参数上，一个为topics，一个为topicPartitions

    SparkSessionSingleton单例
    object SparkSessionSinglton(){

        @transient private var instance: SparkSession = _

        def getSparkSession(sc: SparkContext): SparkSession = {            
            if (instance ==null) {
                instance = SparkSession.builder()
                    .enableHiveSupport()
                    .master("local[*]")
                    .config(sc.getConf)
                    .config("spark.files.openCostInBytes", PropertyUtil.getInstance().getProperty("spark.files.openCostInBytes"))
                    连接到hive元数据库     
                    .config("hive.metastore.uris","thrift://192.168.1.61:9083")
                    //--files hdfs:///user/processuser/hive-site.xml 集群上运行需要指定hive-site.xml的位置      
                    .config("spark.sql.warehouse.dir","hdfs://192.168.1.61:8020/user/hive/warehouse").
                    getOrCreate()
            }
            instance
        }
    }

    SQLContextSingleton单例
    val sqlContext = SQLContextSingleton.getInstance(rdd.sparkContext)
    import sqlContext.implicits._
    object SQLContextSingleton {
        @transient  private var instance: SQLContext = _
        def getInstance(sparkContext: SparkContext): SQLContext = {
            if (instance == null) {
            instance = new SQLContext(sparkContext)
            }
            instance
        }
    }

    如果需要连接到hive必须要注意的几个事项：
    1，指定hive的元数据地址
    2，指定spark.sql.warehouse.dir的数据存储位置
    3，enableHiveSupport()
    4，resource下要放hive-site.xml文件xml文件需要配置的信息，以下信息均可从集群的配置中得到：
        hive.exec.scratchdirhive.metastore.warehouse.dir
        hive.querylog.locationhive.metastore.uris    
        javax.jdo.option.ConnectionURL
        javax.jdo.option.ConnectionDriverName
        javax.jdo.option.ConnectionUserName
        javax.jdo.option.ConnectionPassword
    5，本地执行要指定hadoop的目录
        System.setProperty("hadoop.home.dir", PropertyUtil.getInstance().getProperty("localMode"))
        #hadoopinfo
            localMode=D://hadoop-2.6.5//hadoop-2.6.5
            clusterMode=file://usr//hdp//2.6.2.0-205//hadoop

*/