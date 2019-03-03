object RDD2DataFrameSparkSQL {
  def main(args: Array[String]): Unit = {
    // 1. 构建上下文
    val conf = new SparkConf()
      .setAppName("RDD2DataFrameSparkSQL")
      .setMaster("local[*]")
    // 由于SparkContext在JVM中只能存在一份，所以尽量不要使用new方式来创建
    val sc = SparkContext.getOrCreate(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    // 2. 方式一：反射的机制
    // alt + enter 快速导入变量的数据类型(鼠标放在变量后面)
    val rdd: RDD[Person] = sc.parallelize(Array(
      Person("gerry", 12),
      Person("小刘", 15),
      Person("小明", 18)
    ))
    val df = rdd.toDF() // 默认以case class的属性名称作为DataFrame的列名称
    df.show()
    // 也可以通过toDF的参数来给定列名称，但是要求给定的数组集合中的元素和原始的列数量一致
    rdd.toDF("c1", "c2").show()
    sc.parallelize(Array(
      ("gerry", 12),
      ("小刘", 15),
      ("小明", 18)
    )).toDF().show()

    // 方式二：利用明确给定的schema和rdd进行构建
    val rowRDD = rdd.map(p => {
      val name = p.name
      val age = p.age
      Row(name, age)//构建Row类型的RDD
    })
    val schema = StructType(Array(
      StructField("name", StringType),
      StructField("age", IntegerType)//构建Schema
    ))
    val df2 = sqlContext.createDataFrame(rowRDD, schema)
    df2.show()


    // DataFrame -> RDD
    val rdd1: RDD[Row] = df2.rdd
    val rdd2: RDD[(String, Int)] = df2.map(row => {
      // Row中的数据是没有数据类型的
      val name = row.getAs[String]("name")
      val age = row.getAs[Int]("age")
      (name, age)
    })
    rdd2.foreachPartition(iter => iter.foreach(println))
  }
}

case class Person(name: String, age: Int)
