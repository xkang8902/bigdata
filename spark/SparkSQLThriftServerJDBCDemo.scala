object SparkSQLThriftServerJDBCDemo{
    def main(args: Array[String]): Unit = {
        // 1. 添加driver
        val driver = "org.apache.hive.jdbc.HiveDriver"
        Class.forName(driver)

        // 创建连接
        val url = "jdbc:hive2://bigdata-01.yushu.com:10000"
        val conn = DriverManager.getConnection(url, "gerry", "123456")

        // 执行sql
        conn.prepareStatement("use db_emp").execute()
        val pstmt = conn.prepareStatement("select * from dept a join emp b on a.deptno = b.deptno where b.sal > ?")
        pstmt.setInt(1, 2500)
        val rs = pstmt.executeQuery()
        while (rs.next()) {
            println(rs.getString("ename") + ":" + rs.getDouble("sal"))
        }

        // 关闭连接
        rs.close()
        pstmt.close()
        conn.close()
    }
}
