package com.lihaogn.sparkProject.main

import com.lihaogn.sparkProject.dao.{CourseClickCountDAO, CourseSearchClickCountDAO}
import com.lihaogn.sparkProject.domain.{ClickLog, CourseClickCount, CourseSearchClickCount}
import com.lihaogn.sparkProject.utils.DateUtils
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils

import scala.collection.mutable.ListBuffer

/**
  * 使用spark streaming分析日志
  */
object SparkStreamingApp {

  def main(args: Array[String]): Unit = {

    if (args.length != 4) {
      System.err.println("usage: KafKaReceiverWC <zkQuorum> <group> <topics> <numThreads>")
    }

    val Array(zkQuorum, group, topics, numThreads) = args

    val sparkConf = new SparkConf().setAppName("SparkStreamingApp").setMaster("local[5]")

    val ssc = new StreamingContext(sparkConf, Seconds(5))

    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap

    // spark streaming 对接 kafka
    val messages = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap)

    // 步骤一：测试数据接收
    messages.map(_._2).count().print()

    // 步骤二：数据清洗
    val logs = messages.map(_._2)
    val cleanData = logs.map(line => {
      val infos = line.split("\t")

      val url = infos(2).split(" ")(1)
      var courseId = 0

      // 获取课程标号
      if (url.startsWith("/class")) {
        val courseHtml = url.split("/")(2)
        courseId = courseHtml.substring(0, courseHtml.lastIndexOf(".")).toInt
      }

      ClickLog(infos(0), DateUtils.parseToMinute(infos(1)), courseId, infos(3).toInt, infos(4))
    }).filter(clicklog => clicklog.courseId != 0)

    cleanData.print()

    // 步骤三：统计今天到现在为止的课程访问量
    cleanData.map(x=>{
      (x.time.substring(0,8)+"_"+x.courseId,1)
    }).reduceByKey(_+_).foreachRDD(rdd=>{
      rdd.foreachPartition(partitionRecords=>{
        val list=new ListBuffer[CourseClickCount]

        partitionRecords.foreach(pair=>{
          list.append(CourseClickCount(pair._1,pair._2))
        })
        // 写入数据库
        CourseClickCountDAO.save(list)

      })
    })

    // 步骤四：统计从搜索引擎过来的从今天开始到现在的课程的访问量
    cleanData.map(x=>{
      val referer=x.referer.replaceAll("//","/")
      val splits=referer.split("/")
      var host=""
      if(splits.length>2) {
        host=splits(1)
      }

      (host,x.courseId,x.time)
    }).filter(_._1!="").map(x=>{
      (x._3.substring(0,8)+"_"+x._1+"_"+x._2,1)
    }).reduceByKey(_+_).foreachRDD(rdd=>{
      rdd.foreachPartition(partitionRecords=>{
        val list =new ListBuffer[CourseSearchClickCount]

        partitionRecords.foreach(pair=>{
          list.append(CourseSearchClickCount(pair._1,pair._2))
        })
        // 写入数据库
        CourseSearchClickCountDAO.save(list)

      })
    })

    ssc.start()

    ssc.awaitTermination()
  }
}
