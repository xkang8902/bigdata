package com.sid.com.project
 
import com.sid.com.project.dao.StatDao
import com.sid.com.project.domain.{DayVideoAccessStat, DayVideoCityAccessStat, DayVideoTrafficsTopN}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
 
import scala.collection.mutable.ListBuffer
/**
  * 统计分析
  * */
object StatApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[3]")
      /**参数优化
        * 关闭分区字段的数据类型自动推导，
        * 不然day分区字段我们要string类型，会被自动推导成integer类型
        * 而且没有必要浪费这个资源*/
      .config("spark.sql.sources.partitionColumnTypeInference.enabled",false)
      .appName("statApp").getOrCreate()
 
    val logDF = spark.read.format("parquet")
      .load("file:///F:\\mc\\SparkSQL\\data\\afterclean")
    val day = "20170511"
 
 
    /**
      * 代码优化：复用已有数据
      * 既然每次统计都是统计的当天的视频，
      * 先把该数据拿出来，然后直接传到每个具体的统计方法中
      * 不要在每个具体的统计方法中都执行一次同样的过滤
      *
      * 用$列名得到列值，需要隐式转换 import spark.implicits._
      * */
    import spark.implicits._
    val dayVideoDF = logDF.filter($"day" ===day&&$"cmsType"==="video")
    /**
      * 将这个在后文中会复用多次的dataframe缓存到内存中
      * 这样后文在复用的时候会快很多
      *
      * default storage level (`MEMORY_AND_DISK`).
      * */
    dayVideoDF.cache()
 
    //logDF.printSchema()
    //logDF.show()
 
    StatDao.deletaDataByDay(day)
 
    //统计每天最受欢迎(访问次数)的TopN视频产品
    videoAccessTopNStatDFAPI(spark,dayVideoDF)
 
    //按照地势统计每天最受欢迎(访问次数)TopN视频产品 每个地市只要最后欢迎的前三个
    cityAccessTopNStat(spark,dayVideoDF)
 
    //统计每天最受欢迎(流量)TopN视频产品
    videoTrafficsTopNStat(spark,dayVideoDF)
 
    //清除缓存
    dayVideoDF.unpersist(true)
    spark.stop()
  }
 
  /**
    * 统计每天最受欢迎(访问次数)的TopN视频产品
    *
    * 取出20170511那天访问的视频类产品。
    * 统计每种视频类产品的访问次数，并按访问次数降序排列
    * 用DataFrame API 来写逻辑
    * */
  def videoAccessTopNStatDFAPI(spark:SparkSession,logDF:DataFrame): Unit ={
    //agg(count())需要导入sparksql的函数部分 import org.apache.spark.sql.functions._
    import spark.implicits._
    val videoLogTopNDF = logDF
      .groupBy("day","cmsId")
      .agg(count("cmsId").as("times"))
      .orderBy($"times".desc)
    //videoLogTopNDF.show(false)
    /**
      * 将统计结果写入MySQL中
      * 代码优化：
      * 在进行数据库操作的时候，不要每个record都去操作一次数据库
      * 通常写法是每个partition操作一次数据库
      **/
    try {
      videoLogTopNDF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayVideoAccessStat]
        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val times = info.getAs[Long]("times")
 
          list.append(DayVideoAccessStat(day, cmsId, times))
        })
        StatDao.insertDayVideoTopN(list)
      })
    }catch{
      case e:Exception =>e.printStackTrace()
    }
  }
 
  /**
    * 统计每天最受欢迎(访问次数)的TopN视频产品
    *
    * 取出20170511那天访问的视频类产品。
    * 统计每种视频类产品的访问次数，并按访问次数降序排列
    * 用SQL API 来写逻辑
    * */
  def videoAccessTopNStatSQLAPI(spark:SparkSession,logDF:DataFrame): Unit = {
    logDF.createOrReplaceTempView("tt_log")
    val videoTopNDF = spark.sql("select day,cmsId,count(1) as times from tt_log group by day,cmsId order by times desc")
    //videoTopNDF.show(false)
 
    /**
      * 将统计结果写入MySQL中
      **/
    try {
    videoTopNDF.foreachPartition(partitionOfRecords => {
      val list = new ListBuffer[DayVideoAccessStat]
      partitionOfRecords.foreach(info => {
        val day = info.getAs[String]("day")
        val cmsId = info.getAs[Long]("cmsId")
        val times = info.getAs[Long]("times")
 
        list.append(DayVideoAccessStat(day, cmsId, times))
      })
      StatDao.insertDayVideoTopN(list)
    })
   }catch{
      case e:Exception =>e.printStackTrace()
    }
  }
 
  /**
    * 按照地势统计每天最受欢迎(访问次数)TopN视频产品 每个地市只要最后欢迎的前三个
    * */
  def cityAccessTopNStat(spark:SparkSession,logDF:DataFrame): Unit ={
    //用$列名得到列值，需要隐式转换 import spark.implicits._
    //agg(count())需要导入sparksql的函数部分 import org.apache.spark.sql.functions._
    val videoCityLogTopNDF = logDF.groupBy("day","city","cmsId")
      .agg(count("cmsId").as("times"))
    //这里的结果是
    //日期     地域   产品ID  访问次数
    //20170511 北京市 14390 11175
    //20170511 北京市 4500 11014
    //20170511 浙江省 14390 11110
    //20170511 浙江省 14322 11151
    //然后最后需要的结果是同一天、同一个地域下，按产品访问次数降序排序
 
    //videoLogTopNDF.show(false)
 
    //window函数在Spark SQL中的使用 分组后组内排序
    val top3DF = videoCityLogTopNDF.select(
      videoCityLogTopNDF("day"),
      videoCityLogTopNDF("city"),
      videoCityLogTopNDF("cmsId"),
      videoCityLogTopNDF("times"),
      row_number().over(Window.partitionBy(videoCityLogTopNDF("city")).
        orderBy(videoCityLogTopNDF("times").desc)
      ).as("times_rank")
    ).filter("times_rank <= 3")//.show(false)//只要每个地势最受欢迎的前3个
    //结果
    //日期     地域   产品ID  访问次数
    //20170511 北京市 14390 11175
    //20170511 北京市 4500 11014
    //20170511 浙江省 14322 11151
    //20170511 浙江省 14390 11110
 
 
    //将统计结果写入MySQL中
    try {
      top3DF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayVideoCityAccessStat]
        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val city = info.getAs[String]("city")
          val times = info.getAs[Long]("times")
          val timesRank = info.getAs[Int]("times_rank")
 
          list.append(DayVideoCityAccessStat(day,cmsId,city,times,timesRank))
        })
        StatDao.insertDayCityVideoTopN(list)
      })
    }catch{
      case e:Exception =>e.printStackTrace()
    }
  }
 
 
 
  /**
    * 统计每天最受欢迎(流量)TopN视频产品
    * */
  def videoTrafficsTopNStat(spark:SparkSession,logDF:DataFrame): Unit ={
    //用$列名得到列值，需要隐式转换 import spark.implicits._
    //agg(count())需要导入sparksql的函数部分 import org.apache.spark.sql.functions._
    import spark.implicits._
    val videoLogTopNDF = logDF.groupBy("day","cmsId")
      .agg(sum("traffic").as("traffics"))
      .orderBy($"traffics".desc)
    //videoLogTopNDF.show(false)
    /**
      * 将统计结果写入MySQL中
      **/
    try {
      videoLogTopNDF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayVideoTrafficsTopN]
        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val traffics = info.getAs[Long]("traffics")
 
          list.append(DayVideoTrafficsTopN(day, cmsId, traffics))
        })
        StatDao.insertDayTrafficsVideoTopN(list)
      })
    }catch{
      case e:Exception =>e.printStackTrace()
    }
  }
}
