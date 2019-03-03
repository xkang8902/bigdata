package com.sid.com.project
 
import java.text.SimpleDateFormat
import java.util.{Date, Locale}
 
import org.apache.commons.lang3.time.FastDateFormat
 
/**
  * 日期时间转化工具
  * */
object DateUtils {
  // 10/Nov/2016:00:01:02 +0800
  //输入日期格式
  //SimpleDateFormat是线程不安全的，解析的时候有些时间会解析错
  val YYYYMMDDHHMM_TIME_FORMAT =  FastDateFormat.getInstance("dd/MMM/yyyy:HH:mm:ss Z",Locale.ENGLISH)
 
  //目标日期格式
  val TARGET_FOMAT =  FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
 
  def parse(time:String)={
    TARGET_FOMAT.format(new Date(getTime(time)))
  }
 
  /**
    * 获取输入日志Long类型的时间
    * [10/Nov/2016:00:01:02 +0800]
    * */
  def getTime(time:String) ={
    try {
      YYYYMMDDHHMM_TIME_FORMAT.parse(time.substring(time.indexOf("[") + 1, time.lastIndexOf("]"))).getTime()
    }catch{
      case e :Exception =>{
        0L
      }
    }
  }
 
  def main(args: Array[String]): Unit = {
    println(parse("[10/Nov/2016:00:01:02 +0800]"))
  }
}
