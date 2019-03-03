import java.util

import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{Accumulable, Accumulator, SparkContext, SparkConf}
import org.apache.spark.broadcast.Broadcast

object BroadcastAccumulatorStreaming {
    /**
    * 声明一个广播和累加器！
    */
    private var broadcastList:Broadcast[List[String]]  = _
    private var accumulator:Accumulator[Int] = _

    def main(args: Array[String]) {

        val sparkConf = new SparkConf().setMaster("local[4]").setAppName("broadcasttest")
        val sc = new SparkContext(sparkConf)
        /**
        * duration是ms
        */
        val ssc = new StreamingContext(sc,Duration(2000))
        // broadcastList = ssc.sparkContext.broadcast(util.Arrays.asList("Hadoop","Spark"))
        broadcastList = ssc.sparkContext.broadcast(List("Hadoop","Spark"))
        accumulator= ssc.sparkContext.accumulator(0,"broadcasttest")
        /**
        * 获取数据！
        */
        val lines = ssc.socketTextStream("localhost",9999)
        /**
        * 拿到数据后 怎么处理！
        * 1.flatmap把行分割成词。
        * 2.map把词变成tuple(word,1)
        * 3.reducebykey累加value
        * (4.sortBykey排名)
        * 4.进行过滤。 value是否在累加器中。
        * 5.打印显示。
        */
        val words = lines.flatMap(line => line.split(" "))
        val wordpair = words.map(word => (word,1))
        wordpair.filter(record => {broadcastList.value.contains(record._1)})
        val pair = wordpair.reduceByKey(_+_)
        /**
        * 这步为什么要先foreachRDD？
        * 因为这个pair 是PairDStream<String, Integer>
        * 进行foreachRDD是为了？
        pair.foreachRDD(rdd => {
            rdd.filter(record => {
                if (broadcastList.value.contains(record._1)) {
                    accumulator.add(1)
                    return true
                } else {
                    return false
                }
            })
        })
        */
        val filtedpair = pair.filter(record => {
            if (broadcastList.value.contains(record._1)) {
                accumulator.add(record._2)
                true
            } else {
                false
            }
        }).print
        println("累加器的值"+accumulator.value) 
        /**
        * pair.filter(record => {broadcastList.value.contains(record._1)})
        * val keypair = pair.map(pair => (pair._2,pair._1))
        * 如果DStream自己没有某个算子操作。就通过转化transform！
            keypair.transform(rdd => {
                rdd.sortByKey(false)//TODO
            })
        */
        pair.print()
        ssc.start()
        ssc.awaitTermination()
    }
}

/**
闭包与广播变量对比
有两种方式将数据从driver节点发送到worker节点：通过 闭包 和通过 广播变量 。
    闭包是随着task的组装和分发自动进行的，而广播变量则是需要程序猿手动操作的，具体地可以通过如下方式操作广播变量(假设 sc 为 SparkContext 类型的对象， bc 为 Broadcast 类型的对象)：
    可通过 sc.broadcast(xxx) 创建广播变量。
    可在各计算节点中(闭包代码中)通过 bc.value 来引用广播的数据。
    bc.unpersist() 可将各executor中缓存的广播变量删除，后续再使用时数据将被重新发送。
    bc.destroy() 可将广播变量的数据和元数据一同销毁，销毁之后就不能再使用了。
任务闭包包含了任务所需要的代码和数据，如果一个executor数量小于RDD partition的数量，那么每个executor就会得到多个同样的任务闭包，这通常是低效的。而广播变量则只会将数据发送到每个executor一次，并且可以在多个计算操作中共享该广播变量，而且广播变量使用了类似于p2p形式的非常高效的广播算法，大大提高了效率。另外，广播变量由spark存储管理模块进行管理，并以MEMORY_AND_DISK级别进行持久化存储。

什么时候用闭包自动分发数据？情况有几种：
    数据比较小的时候。
    数据已在driver程序中可用。典型用例是常量或者配置参数。
什么时候用广播变量分发数据？情况有几种：
    数据比较大的时候(实际上，spark支持非常大的广播变量，甚至广播变量中的元素数超过java/scala中Array的最大长度限制(2G，约21.5亿)都是可以的)。
    数据是某种分布式计算结果。典型用例是训练模型等中间计算结果。
    当数据或者变量很小的时候，我们可以在Spark程序中直接使用它们，而无需使用广播变量。
使用广播变量的注意事项：
    1.能不能将一个 RDD 使用广播变量广播出去？不能，因为RDD是不存储数据的。可以将RDD的结果广播出去。
    2.广播变量只能在 Driver 端定义，不能在 Executor 端定义。
    3.在Driver端可以修改广播变量的值，在 Executor 端无法修改广播变量的值。
    4.广播变量允许程序员将一个只读的变量缓存在每台机器上，而不用在任务之间传递变量。
    5.广播变量可被用于有效地给每个节点一个大输入数据集的副本
    6.Spark还尝试使用高效地广播算法来分发变量，进而减少通信的开销。
使用累加器的注意事项：
    累加器是在Spark计算操作中变量值累加起来，可以被用来实现计数器、或者求和操作。Spark原生地只支持数字类型的累加器，编程者可以添加新类型的支持。
    如果创建累加器时指定了名字，可就以在SparkUI界面看到。这有利于理解每个执行阶段的进程。综合一句话来说，累加器在Driver端定义赋初始值，累加器只能在Driver端读取，在 Excutor 端更新。
*/