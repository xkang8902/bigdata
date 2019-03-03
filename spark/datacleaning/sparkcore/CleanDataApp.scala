package com.sid.com.project
 
import org.apache.spark.sql.{SaveMode, SparkSession}
import com.sid.com.project.LogConvertUtil
 
 
object CleanDataApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[3]").appName("CleanDataApp").getOrCreate()
 
    /**
      * 第一步数据清洗，从原始日志中抽取出需要的列的数据,按照需要的格式
      * */
    //dataCleaning(spark)
 
   /**
     * 第二步数据清洗，解析第一步清洗后的数据，
     *    处理时间，提出URL中的产品编号、得到产品类型，
     *    由IP得到城市信息（用到开源社区的解析代码）
     *    按照天分区进行存储
     */
    dataCleaning2(spark)
 
 
    spark.stop()
  }
 
  //输入数据  .log文件
  //183.162.52.7 - - [10/Nov/2016:00:01:02 +0800] "POST /api3/getadv HTTP/1.1" 200 813 "www.主站地址.com" "-" cid=0×tamp=1478707261865&uid=2871142&marking=androidbanner&secrect=a6e8e14701ffe9f6063934780d9e2e6d&token=f51e97d1cb1a9caac669ea8acc162b96 "mukewang/5.0.0 (Android 5.1.1; Xiaomi Redmi 3 Build/LMY47V),Network 2G/3G" "-" 10.100.134.244:80 200 0.027 0.027
  //输出数据 没有schema
  //2016-11-10 00:01:02	http://www.主站地址.com/code/1852	2345	117.35.88.11
  def dataCleaning(spark:SparkSession): Unit ={
    val acc = spark.sparkContext.textFile("file:///F:\\mc\\SparkSQL\\data\\access1000.log")
    //acc.take(10).foreach(println)
    //line:183.162.52.7 - - [10/Nov/2016:00:01:02 +0800] "POST /api3/getadv HTTP/1.1" 200 813 "www.主站地址.com" "-" cid=0×tamp=1478707261865&uid=2871142&marking=androidbanner&secrect=a6e8e14701ffe9f6063934780d9e2e6d&token=f51e97d1cb1a9caac669ea8acc162b96 "mukewang/5.0.0 (Android 5.1.1; Xiaomi Redmi 3 Build/LMY47V),Network 2G/3G" "-" 10.100.134.244:80 200 0.027 0.027
    acc.map(line => {
      val splits = line.split(" ")
      val ip = splits(0)
      val time = splits(3)+" "+splits(4)//第四个字段和第五个字段拼接起来就是完成的访问时间。
      val url = splits(11).replaceAll("\"","")
      val traffic = splits(9)//访问流量
 
      // (ip,DateUtils.parse(time),url,traffic)
 
      DateUtils.parse(time)+"\t"+url+"\t"+traffic+"\t"+ip
    }).saveAsTextFile("file:///F:\\mc\\SparkSQL\\data\\access1000step1")
  }
 
  //输入数据
  //2016-11-10 00:01:02	http://www.主站地址.com/code/1852	2345	117.35.88.11
  //输出数据 带schema信息 parquet格式存储
  //http://www.主站地址.com/code/1852 code 1852 2345 117.35.88.11 陕西省 2016-11-10 00:01:02 20161110
  def dataCleaning2(spark:SparkSession): Unit ={
    val logRDD = spark.sparkContext.textFile("file:///F:\\mc\\SparkSQL\\data\\access.log")
    //val logRDD = spark.sparkContext.textFile("file:///F:\\mc\\SparkSQL\\data\\access1000step1.log\\part-00000")
    //logRDD.take(10).foreach(println)
    val logDF = spark.createDataFrame(logRDD.map(x => LogConvertUtil.parseLog(x)),LogConvertUtil.struct)
 
    //logDF.printSchema()
    //logDF.show(false)
 
    /**partitionBy("day")   数据写的时候要按照天进行分区
    coalesce(1)  生成的part-00000文件只有一个且较大，不然3个线程会生成3个part-0000*文件 可以用此参数调优
    mode(SaveMode.Overwrite) 指定路径下如果有文件，覆盖*/
    logDF.coalesce(1).write.format("parquet")
      .mode(SaveMode.Overwrite)
      .partitionBy("day")
      .save("file:///F:\\mc\\SparkSQL\\data\\afterclean")//存储清洗后的数据
  }
}
