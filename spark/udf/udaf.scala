import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._

/*
{"name":"Michael", "salary":3000}
{"name":"Andy", "salary":4500}
{"name":"Justin", "salary":3500}
{"name":"Berta", "salary":4000}
求平均工资
 */

class AverageSal extends UserDefinedAggregateFunction{
  // 输入数据
  override def inputSchema: StructType = StructType(StructField("salary", LongType) :: Nil)

  // 每一个分区中的 共享变量
  override def bufferSchema: StructType = StructType(StructField("sum", LongType) :: StructField("count", IntegerType) :: Nil)

  // 表示UDAF的输出类型
  override def dataType: DataType = DoubleType

  // 表示如果有相同的输入是否存在相同的输出，如果是则true
  override def deterministic: Boolean = true

  // 初始化每个分区中的 共享变量
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = 0L  // 就是sum
    buffer(1) = 0   // 就是count
  }

  // 每一个分区中的每一条数据  聚合的时候需要调用该方法
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    // 获取这一行中的工资，然后将工资加入到sum中
    buffer(0) = buffer.getLong(0) + input.getLong(0)
    // 将工资的个数加1
    buffer(1) = buffer.getInt(1) + 1
  }

  // 将每一个分区的输出合并，形成最后的数据
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    // 合并总的工资
    buffer1(0) = buffer1.getLong(0) + buffer2.getLong(0)
    // 合并总的工资个数
    buffer1(1) = buffer1.getInt(1) + buffer2.getInt(1)
  }

  // 给出计算结果
  override def evaluate(buffer: Row): Any = {
    // 取出总的工资 / 总工资个数
    buffer.getLong(0).toDouble / buffer.getInt(1)
  }
}

object AverageSal {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("udaf").setMaster("local[*]")
    val spark = SparkSession
      .builder()
      .config(sparkConf)
      .getOrCreate()
    val employee = spark.read.json("employee.json")

    employee.createOrReplaceTempView("employee")

    spark.udf.register("average", new AverageSal)

    spark.sql("select average(salary) from employee").show()

    spark.stop()
  }
}
