object HiveJoinMysqlDemo {
  def main(args: Array[String]) {
        //构建上下文
        val conf=new SparkConf()
            .setAppName("HiveJoinMysql")
            .setMaster("local[*]")
        val sc=SparkContext.getOrCreate(conf)
        val sqlContext=new HiveContext(sc)
        //连接mysql数据库
        val url="jdbc:mysql://localhost:3306/yushu"
        val table="tb_dept"
        val props=new Properties()
        props.put("user","root")
        props.put("password","123456")

        // 2. 将dept数据同步到mysql中
        sqlContext
            .read
            .table("db_emp.dept")
            .write
            .mode(SaveMode.Overwrite)
            .jdbc(url,table,props)

        // 3. mysql数据和hive数据join
        // 将mysql的数据读取形成DataFrame，然后注册成为临时表
        val df = sqlContext
        .read
        .jdbc(url, table, Array("loc <= 'CHICAGO'", "loc > 'CHICAGO'"), props)
        df.registerTempTable("mysql_dept")
        sqlContext
        .sql("select a.*, b.loc,b.dname from db_emp.emp a join mysql_dept b on a.deptno=b.deptno")
        .registerTempTable("result_tmp01")

        // 3. 将数据写到HDFS上形成parquet格式的数据
        sqlContext
        .table("result_tmp01")
        .write
        .format("parquet")
        .mode(SaveMode.Overwrite)
        .save("/user/yushu/spark/data")
        //按部门分区保存在Hive表中
        sqlContext .table("result_tmp01") .write .format("parquet") .partitionBy("deptno") .mode(SaveMode.Overwrite) .saveAsTable("test01") 
    }
}
