import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{StringType, IntegerType, StructField, StructType}
import org.apache.spark.sql.{SaveMode, SQLContext}
import org.apache.spark.{SparkContext, SparkConf}
object ReadCSVSparkSQL {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("ReadCSVSparkSQL")
      .setMaster("local[*]")
      .set("spark.sql.shuffle.partitions", "4")
    val sc = SparkContext.getOrCreate(conf)
    val sqlContext = new HiveContext(sc)
    val time = System.currentTimeMillis()

    // 2. 读取csv文件然后形成DataFrame，然后将DataFrame注册成为临时表tmp_taxi
    // 列名称：id，lat, lon, time
    val schema = StructType(Array(
      StructField("id", IntegerType),
      StructField("lat", StringType),
      StructField("lon", StringType),
      StructField("time", StringType)
    ))
    val path = "datas/taxi.csv"
    val df = sqlContext
      .read
      .format("com.databricks.spark.csv")
      .schema(schema)
      .load(path)
    df.registerTempTable("tmp_taxi")
    // 3.1 获取需要进行分析的列字段(id、time)，并将time转换为小时
    sqlContext.sql(
      """
        |select id,substring(time,0,2) as hour
        |from tmp_taxi
      """.stripMargin
    ).registerTempTable("tmp01")
    // 3.2 统计各个出租车在各个时间段的载客次数
    sqlContext.sql(
      """
        |select id,hour,COUNT(1) as cnt
        |from tmp01
        |group by id,hour
      """.stripMargin).registerTempTable("tmp02")
    // 3.3 获取每个小时段的载客次数前10的出租车数据(先按照小时分组，然后对每组数据获取前10条出租车载客数据<最多的>) ---> 分组排序TopN --> row_number函数
    sqlContext.sql(
      """
        |select id,hour,cnt,
        |ROW_NUMBER() OVER(PARTITION BY hour ORDER BY cnt DESC) as rnk
        |FROM tmp02
      """.stripMargin).registerTempTable("tmp03")
    sqlContext.sql(
      """
        |select id,hour,cnt
        |from tmp03
        |where rnk<=10
      """.stripMargin).registerTempTable("tmp04")

    //4.将结果保存
    sqlContext.cacheTable("tmp04")
    sqlContext
      .table("tmp04")
      .map(row => {
      val id = row.getAs[Int]("id")
      val hour = row.getAs[String]("hour")
      val cnt = row.getAs[Long]("cnt")
      (id, hour, cnt)
    }).saveAsTextFile(s"result/csv/${time}/1")

    sqlContext
      .table("tmp04")
      .write
      .mode(SaveMode.Overwrite)
      .format("com.databricks.spark.csv")
      .save(s"result/csv/${time}/2")

    Thread.sleep(100000)
  }
}
