// SparkSql:
object userActionStatAnalyzeSpark {
    def main(args: Array[String]): Unit ={
        //创建context
        val sparkConf = new SparkConf()
        sparkConf.setAppName(Constants.SPARK_APP_NAME_SESSION).setMaster("local")
        val sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
        val sc = sparkSession.sparkContext

        import sparkSession.implicits._
        //数据源
        val userVistActionDF = sparkSession.read.parquet("hdfs:\\spark-project\\data\\input\\parquet\\user_visit_action\\")
        //创建临时表
        userVistActionDF.createOrReplaceTempView("userVisitAction")
        userInfoDF.createOrReplaceTempView("userInfo")
        //使用sql语句操作ds
        val sql4VisitAction =
            s"""
                |SELECT
                | session_id,
                | user_id,
                | action_time,
                |FROM
                | userVisitAction
                |WHERE
                | date >= '$startDate'
                |AND
                | date <= '$endDate'
            """.stripMargin
        val sql4Userinfo =
            s"""
                |SELECT
                | user_id,
                | username,
                | name,
                |FROM
                | userInfo
            """.stripMargin

        val sessionInfoDS = sparkSession.sql(sql4VisitAction).as[SessionFullInfo]
        val userInfoDS = sparkSession.sql(sql4Userinfo).as[UserInfo]
        //使用sparksql算子操作ds
        val sessionFullAggrInfoDS = sessionInfoDS.join(userInfoDS, "user_id")

        val filtered2FullInfoDS = sessionFullAggrInfoDS.filter(row => {  ......  })

        filtered2FullInfoDS.persist()

        val sessionId2FullAggrInfoDS = filtered2FullInfoDS.groupByKey(_.getString(1)).mapGroups((sessionId, iter) => {   .....})
        val sessionId2FullAggrInfoDS = filtered2FullInfoDS.groupByKey(_.getString(1)).flatMapGroups((sessionId, iter) => {   .....})

        val aggredSessionInfo = sessionId2FullAggrInfoDS.reduce((s1, s2) => {....})
        //结果输出
        val insertSql = "INSERT INTO session_aggr_stat values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
        JdbcPoolHelper.getJdbcPoolHelper.execute(insertSql, pstmt => {
            aggredSessionInfo.collect().foreach(row => {
                ....
                pstmt.setString(1, row.getString(1))
                ....
                pstmt.addBatch()
            })
        })



    }
}