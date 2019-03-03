/**
  Spark-Rdd唯一键的TopN的实现
    输入数据
    AAA 23
    BBB 20
    CCC 22
    DDD 35
    EEE 78
    FFF 73
    GGG 89
    输出结果
    +---+---+ TopN的结果1：
    73 	 FFF,73
    78 	 EEE,78
    89 	 GGG,89    
    +---+---+ TopN的结果2：
    89 	 GGG,89
    78 	 EEE,78
    73 	 FFF,73
*/
object SparkUniqueTopN {
    def main(args: Array[String]): Unit = {
        //TopN的值，参数传递
        val num: Int = args(0).toInt
        //读取文件路径
        val path: String = args(1)
        val config: SparkConf = new SparkConf().setMaster("local").setAppName("SparkUniqueTopN")
        //构建Spark上下文
        val sparkContext: SparkContext = SparkSession.builder().config(config).getOrCreate().sparkContext
        //广播变量
        val topN: Broadcast[Int] = sparkContext.broadcast(num)
 
        val rdd: RDD[String] = sparkContext.textFile(path)
        val pairRdd: RDD[(Int, Array[String])] = rdd.map(line => {
            val tokens: Array[String] = line.split(" ")
            (tokens(1).toInt, tokens)
        })
 
        val partitions: RDD[(Int, Array[String])] = pairRdd.mapPartitions(iterator => {
            var sortedMap = SortedMap.empty[Int, Array[String]]
            iterator.foreach({ tuple => {
                sortedMap += tuple
                if (sortedMap.size > topN.value) {
                    sortedMap = sortedMap.takeRight(topN.value)
                }
            }
            })
            sortedMap.takeRight(topN.value).toIterator
        })
 
        val alltopN: Array[(Int, Array[String])] = partitions.collect()
        val finaltopN: SortedMap[Int, Array[String]] = SortedMap.empty[Int, Array[String]].++:(alltopN)
        val resultUsingMapPartition: SortedMap[Int, Array[String]] = finaltopN.takeRight(topN.value)
 
        println("+---+---+ TopN的结果1：")
        resultUsingMapPartition.foreach {
            case (k, v) => println(s"$k \t ${v.asInstanceOf[Array[String]].mkString(",")}")
        }
        val moreConciseApproach: Array[(Int, Iterable[Array[String]])] = pairRdd.groupByKey().sortByKey(ascending = false).take(topN.value)
        println("+---+---+ TopN的结果2：")
        moreConciseApproach.foreach {
            case (k, v) => println(s"$k \t ${v.flatten.mkString(",")}")
        }
    }
}

/**
  * Spark-Rdd非唯一键的TopN的实现
    输入数据
    AAA 23
    BBB 20
    CCC 22
    DDD 35
    EEE 78
    FFF 73
    GGG 89
    AAA 45
    BBB 76
    CCC 54
    DDD 76
    EEE 75
    FFF 53
    GGG 43
    输出结果
    +---+---+ TopN的结果1：
    126 	 FFF
    132 	 GGG
    153 	 EEE   
    +---+---+ TopN的结果2：
    153 	 EEE
    132 	 GGG
  */
object SparkNonUniqueTopN {
    def main(args: Array[String]): Unit = {
        //TopN的值，参数传递
        val num: Int = args(0).toInt
        //读取文件路径
        val path: String = args(1)
        val config: SparkConf = new SparkConf().setMaster("local").setAppName("SparkNonUniqueTopN")
        //构建Spark上下文
        val sparkContext: SparkContext = SparkSession.builder().config(config).getOrCreate().sparkContext
        //广播变量
        val topN: Broadcast[Int] = sparkContext.broadcast(num)
 
        val rdd: RDD[String] = sparkContext.textFile(path)
        val kv: RDD[(String, Int)] = rdd.map(line => {
            val tokens = line.split(" ")
            (tokens(0), tokens(1).toInt)
        })
        //将非唯一键转换为唯一键
        val uniqueKeys: RDD[(String, Int)] = kv.reduceByKey(_ + _)
        val partitions: RDD[(Int, String)] = uniqueKeys.mapPartitions(itr => {
            var sortedMap = SortedMap.empty[Int, String]
            itr.foreach { tuple => {
                sortedMap += tuple.swap
                if (sortedMap.size > topN.value) {
                    sortedMap = sortedMap.takeRight(topN.value)
                }
            }
            }
            sortedMap.takeRight(topN.value).toIterator
        })
 
        val alltopN = partitions.collect()
        val finaltopN = SortedMap.empty[Int, String].++:(alltopN)
        val resultUsingMapPartition = finaltopN.takeRight(topN.value)
        //打印结果
        println("+---+---+ TopN的结果1：")
        resultUsingMapPartition.foreach {
            case (k, v) => println(s"$k \t ${v.mkString(",")}")
        }
 
        val createCombiner: Int => Int = (v: Int) => v
        val mergeValue: (Int, Int) => Int = (a: Int, b: Int) => a + b
        val moreConciseApproach: Array[(Int, Iterable[String])] =
            kv.combineByKey(createCombiner, mergeValue, mergeValue)
                .map(_.swap)
                .groupByKey()
                .sortByKey(ascending = false)
                .take(topN.value)
 
        //打印结果
        println("+---+---+ TopN的结果2：")
        moreConciseApproach.foreach {
            case (k, v) => println(s"$k \t ${v.mkString(",")}")
        }
    }
}

/**
  * Spark-Rdd计算分组数据中TopN的实现
    输入数据
    spark 78
    sql 98
    java 80
    javascript 98
    scala 69
    hadoop 87
    hbase 97
    hive 86
    
    spark 89
    sql 65
    java 45
    javascript 76
    scala 34
    hadoop 87
    hbase 43
    hive 76
    
    spark 65
    sql 53
    java 65
    javascript 54
    scala 98
    hadoop 32
    hbase 76
    hive 87
    
    spark 38
    sql 42
    java 97
    javascript 34
    scala 76
    hadoop 90
    hbase 55
    hive 88
    
    spark 35
    sql 76
    java 98
    javascript 34
    scala 65
    hadoop 76
    hbase 67
    hive 56
    输出结果
    +---+---+ 使用groupByKey获取TopN的结果：
    (scala,List(98, 76, 69))
    (spark,List(89, 78, 65))
    (hive,List(88, 87, 86))
    (hadoop,List(90, 87, 87))
    (java,List(98, 97, 80))
    (javascript,List(98, 76, 54))
    (sql,List(98, 76, 65))
    (hbase,List(97, 76, 67))
    
    +---+---+ 使用两阶段集合获取TopN的结果：
    (scala,List(98, 76, 69))
    (spark,List(89, 78, 65))
    (hive,List(88, 87, 86))
    (hadoop,List(90, 87, 87))
    (java,List(98, 97, 80))
    (javascript,List(98, 76, 54))
    (sql,List(98, 76, 65))
    (hbase,List(97, 76, 67))
    
    +---+---+ 使用aggregateByKey获取TopN的结果：
    (scala,List(98, 76, 69))
    (spark,List(89, 78, 65))
    (hive,List(88, 87, 86))
    (hadoop,List(90, 87, 87))
    (java,List(98, 97, 80))
    (javascript,List(98, 76, 54))
    (sql,List(98, 76, 65))
    (hbase,List(97, 76, 67))
  */
object SparkGroupTopN {
    def main(args: Array[String]): Unit = {
        val num = args(0).toInt
        //读取文件路径
        val path: String = args(1)
        //Spark配置
        val config: SparkConf = new SparkConf().setMaster("local").setAppName("SparkGroupTopN")
        //创建上下文
        val sparkConext: SparkContext = SparkSession.builder().config(config).getOrCreate().sparkContext
        //读取文件数据形成RDD
        val rdd: RDD[String] = sparkConext.textFile(path)
        //过滤空行，将rdd转换为键值对PairRDD
        val mapredRDD: RDD[(String, Int)] = rdd.filter(line => line.length > 0)
            .map(line => line.split(" "))
            .map(arr => (arr(0).trim, arr(1).trim.toInt))
        //缓存RDD，方便后期的使用
        mapredRDD.cache()
        val topN: Broadcast[Int] = sparkConext.broadcast(num)
        //1、使用groupByKey的方式实现读取TopN的数据
        //缺点：
        //(1)使用groupByKey，在相同的key所对应的数据形成的迭代器在处理过程中的全部数据会加在到内存中，
        //   如果一个key的数据特别多的情况下，就会很容易出现内存溢出（OOM）
        //(2)在同组key中进行数据聚合并汇总，groupByKey的性能不是很高的，因为没有事先对分区数据进行一个临时聚合运算
        val topNResult1: RDD[(String, Seq[Int])] = mapredRDD.groupByKey().map(tuple2 => {
            //获取values里面的topN
            val topn = tuple2._2.toList.sorted.takeRight(topN.value).reverse
            (tuple2._1, topn)
        })
 
        println("+---+---+ 使用groupByKey获取TopN的结果：")
        println(topNResult1.collect().mkString("\n"))
 
        //2.使用两阶段聚合，先使用随机数进行分组聚合取出局部topn,再聚合取出全局topN的数据
        val topNResult2: RDD[(String, List[Int])] = mapredRDD.mapPartitions(iterator => {
            iterator.map(tuple2 => {
                ((Random.nextInt(10), tuple2._1), tuple2._2)
            })
        }).groupByKey().flatMap({
            //获取values中的前N个值 ，并返回topN的集合数据
            case ((_, key), values) =>
                values.toList.sorted.takeRight(topN.value).map(value => (key, value))
        }).groupByKey().map(tuple2 => {
            val topn = tuple2._2.toList.sorted.takeRight(topN.value).reverse
            (tuple2._1, topn)
        })
        println("+---+---+ 使用两阶段集合获取TopN的结果：")
        println(topNResult2.collect().mkString("\n"))
 
        //3、使用aggregateByKey获取topN的记录
        val topNResult3: RDD[(String, List[Int])] = mapredRDD.aggregateByKey(ArrayBuffer[Int]())(
            (u, v) => {
                u += v
                u.sorted.takeRight(topN.value)
            },
            (u1, u2) => {
                //对任意的两个局部聚合值进行聚合操作，可以会发生在combiner阶段和shuffle之后的最终的数据聚合的阶段
                u1 ++= u2
                u1.sorted.takeRight(topN.value)
            }
        ).map(tuple2 => (tuple2._1, tuple2._2.toList.reverse))
 
        println("+---+---+ 使用aggregateByKey获取TopN的结果：")
        println(topNResult3.collect().mkString("\n"))
    }
}
