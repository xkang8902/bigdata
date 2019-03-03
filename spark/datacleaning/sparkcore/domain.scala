package com.sid.com.project.domain
 
/**
  *每天视频课程访问次数实体类
  * */
case class DayVideoAccessStat (day:String,cmsId:Long,times:Long)

case class DayVideoCityAccessStat (day:String,cmsId:Long,city:String,times:Long,timesRank:Int)

case class DayVideoTrafficsTopN(day:String, cmsId:Long, traffics:Long)