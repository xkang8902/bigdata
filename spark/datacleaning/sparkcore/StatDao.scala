package com.sid.com.project.dao
 
 
 
import java.sql.{Connection, PreparedStatement}
 
import com.sid.com.project.MySQLUtils
import com.sid.com.project.domain.{DayVideoAccessStat, DayVideoCityAccessStat, DayVideoTrafficsTopN}
 
import scala.collection.mutable.ListBuffer
 
 
/**
  * 统计各个维度的DAO操作
  * */
object StatDao {
 
  /**
    * 批量保存DayVideoAccessStat到MySQL
    * */
  def insertDayVideoTopN(list:ListBuffer[DayVideoAccessStat]): Unit ={
    var connection:Connection = null
    var pstmt : PreparedStatement = null
    try{
      connection = MySQLUtils.getConnection()
 
      connection.setAutoCommit(false)//关闭自动提交
      val sql = "insert into day_video_access_topn_stat(day,cms_id,times) values(?,?,?)"
      pstmt = connection.prepareStatement(sql)
 
      for(ele <- list){
        pstmt.setString(1,ele.day)
        pstmt.setLong(2,ele.cmsId)
        pstmt.setLong(3,ele.times)
        //加入到批次中，后续再执行批量处理 这样性能会好很多
        pstmt.addBatch()
      }
      //执行批量处理
      pstmt.executeBatch()
 
      connection.commit() //手工提交
 
    }catch {
      case e :Exception =>e.printStackTrace()
    }finally {
      MySQLUtils.release(connection,pstmt)
    }
  }
 
  /**
    * 批量保存DayVideoCityAccessStat到MySQL
    * */
  def insertDayCityVideoTopN(list:ListBuffer[DayVideoCityAccessStat]): Unit ={
    var connection:Connection = null
    var pstmt : PreparedStatement = null
    try{
      connection = MySQLUtils.getConnection()
 
      connection.setAutoCommit(false)//关闭自动提交
      val sql = "insert into day_video_city_access_topn_stat(day,cms_id,city,times,times_rank) values(?,?,?,?,?)"
      pstmt = connection.prepareStatement(sql)
 
      for(ele <- list){
        pstmt.setString(1,ele.day)
        pstmt.setLong(2,ele.cmsId)
        pstmt.setString(3,ele.city)
        pstmt.setLong(4,ele.times)
        pstmt.setInt(5,ele.timesRank)
        //加入到批次中，后续再执行批量处理 这样性能会好很多
        pstmt.addBatch()
      }
      //执行批量处理
      pstmt.executeBatch()
 
      connection.commit() //手工提交
 
    }catch {
      case e :Exception =>e.printStackTrace()
    }finally {
      MySQLUtils.release(connection,pstmt)
    }
  }
 
 
  /**
    * 批量保存DayVideoTrafficsTopN到MySQL
    * */
  def insertDayTrafficsVideoTopN(list:ListBuffer[DayVideoTrafficsTopN]): Unit ={
    var connection:Connection = null
    var pstmt : PreparedStatement = null
    try{
      connection = MySQLUtils.getConnection()
 
      connection.setAutoCommit(false)//关闭自动提交
      val sql = "insert into day_video_traffics_access_topn_stat(day,cms_id,traffics) values(?,?,?)"
      pstmt = connection.prepareStatement(sql)
 
      for(ele <- list){
        pstmt.setString(1,ele.day)
        pstmt.setLong(2,ele.cmsId)
        pstmt.setLong(3,ele.traffics)
 
        //加入到批次中，后续再执行批量处理 这样性能会好很多
        pstmt.addBatch()
      }
      //执行批量处理
      pstmt.executeBatch()
 
      connection.commit() //手工提交
 
    }catch {
      case e :Exception =>e.printStackTrace()
    }finally {
      MySQLUtils.release(connection,pstmt)
    }
  }
 
  /**
    * 删除指定日期的数据
    * */
  def deletaDataByDay(day:String): Unit ={
    val tables = Array("day_video_access_topn_stat",
    "day_video_city_access_topn_stat",
    "day_video_traffics_access_topn_stat")
 
    var connection:Connection=null
    var pstmt : PreparedStatement = null
    try{
      connection = MySQLUtils.getConnection()
      for(table <- tables){
        val deleteSql = s"delete from $table where day = ?"
        pstmt = connection.prepareStatement(deleteSql)
        pstmt.setString(1,day)
        pstmt.executeUpdate()
      }
    }catch {
      case e :Exception =>e.printStackTrace()
    }finally {
      MySQLUtils.release(connection,pstmt)
    }
  }
}
