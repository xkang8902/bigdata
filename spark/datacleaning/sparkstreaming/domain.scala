package com.lihaogn.sparkProject.domain

/**
  * 清洗后的日志格式
  *
  * @param ip
  * @param time
  * @param courseId
  * @param statusCode 日志访问状态码
  * @param referer
  */
case class ClickLog(ip: String, time: String, courseId: Int, statusCode: Int, referer: String)
/**
  * 课程点击次数实体类
  *
  * @param day_course  对应HBase中的rowkey
  * @param click_count 访问次数
  */
case class CourseClickCount(day_course: String, click_count: Long)
/**
  * 从搜索引擎过来的课程点击数实体类
  * @param day_search_course
  * @param click_count
  */
case class CourseSearchClickCount(day_search_course: String, click_count: Long)
