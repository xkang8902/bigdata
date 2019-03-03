package com.sid.com.project
 
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
 
/**
  * 访问日志转换工具类
  *
  * 输入：访问时间、访问URL、耗费的流量、访问IP地址信息
  * 2016-11-10 00:01:02	http://www.主站地址.com/code/1852	2345	117.35.88.11
  *
  * 输出：URL、cmsType(video/article)、cmsId(编号)、流量、ip、城市信息、访问时间、按天分区存储
  * */
object LogConvertUtil {
 
    //输出字段的schema
    val struct = StructType(
      Array(
        StructField("url", StringType),
        StructField("cmsType", StringType),
        StructField("cmsId", LongType),
        StructField("traffic", LongType),
        StructField("ip", StringType),
        StructField("city", StringType),
        StructField("time", StringType),
        StructField("day", StringType)
      )
    )
 
    //根据输入的每行信息转换成输出的样式
    def parseLog(log: String)={
      try{
      val splits = log.split("\t")
      val url = splits(1)
      val traffic = splits(2).toLong
      val ip = splits(3)
 
      val domain = "http://www.imooc.com/"
      val cms = url.substring(url.indexOf(domain) + domain.length)
      val cmsTypeId = cms.split("/")
 
      var cmsType = ""
      var cmsId = 0L
 
      if (cmsTypeId.length > 1) {
        cmsType = cmsTypeId(0)
        cmsId = cmsTypeId(1).toLong
 
      }
 
      val city = IpUtils.getCity(ip)
      val time = splits(0)
      val day = time.substring(0, 10).replaceAll("-", "")
 
      //row中的字段要和struct中的字段对应上
      Row(url, cmsType, cmsId, traffic, ip, city, time, day)
    }catch{
        case e : Exception =>Row(0)
      }
  }
}
