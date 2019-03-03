//自定义窗口函数：
import java.util.UUID

import org.apache.spark.sql.{Column, Row}
import org.apache.spark.sql.catalyst.expressions.{Add, AggregateWindowFunction, AttributeReference, Expression, If, IsNotNull, LessThanOrEqual, Literal, ScalaUDF, Subtract}
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String

object MyUDWF {
  val defaultMaxSessionLengthms = 3600 * 1000
  case class SessionUDWF(timestamp:Expression, session:Expression,
                         sessionWindow:Expression = Literal(defaultMaxSessionLengthms)) 
        extends AggregateWindowFunction {   self: Product =>

        override def children: Seq[Expression] = Seq(timestamp, session)
        override def dataType: DataType = StringType

        protected val zero = Literal( 0L )
        protected val nullString = Literal(null:String)
        /**
        在内部需要维护的状态数据
            当前的session ID
            当前session的最后活动事件的时间戳
        */
        protected val curentSession = AttributeReference("currentSession", StringType, nullable = true)()
        protected val previousTs = AttributeReference("lastTs", LongType, nullable = false)()
        //状态保存在 Seq[AttributeReference]中
        override val aggBufferAttributes: Seq[AttributeReference] =  curentSession  :: previousTs :: Nil
        //重写 initialValues方法进行初始化
        //initialize with no session, zero previous timestamp
        override val initialValues: Seq[Expression] =  nullString :: zero :: Nil
        //updateExpressions 函数针对每一行数据都会调用
        //if a session is already assigned, keep it, otherwise, assign one
        override val updateExpressions: Seq[Expression] =
            If(IsNotNull(session), session, assignSession) ::
            timestamp ::
            Nil
        //just return the current session in the buffer
        override val evaluateExpression: Expression = aggBufferAttributes(0)
        //assign session: if previous timestamp was longer than interval,
        //new session, otherwise, keep current.
        protected val assignSession =  If(LessThanOrEqual(Subtract(timestamp, aggBufferAttributes(1)), sessionWindow),
            aggBufferAttributes(0), 
            ScalaUDF( createNewSession, StringType, children = Nil))

        override def prettyName: String = "makeSession"
    }
    //this is invoked whenever we need to create a new session id. you can use your own logic, here we create UUIDs.
    protected val  createNewSession = () => org.apache.spark.unsafe.types.UTF8String.fromString(UUID.randomUUID().toString)

    def calculateSession(ts:Column,sess:Column): Column = withExpr { SessionUDWF(ts.expr,sess.expr, Literal(defaultMaxSessionLengthms)) }
    def calculateSession(ts:Column,sess:Column, sessionWindow:Column): Column = withExpr { SessionUDWF(ts.expr,sess.expr, sessionWindow.expr) }

    private def withExpr(expr: Expression): Column = new Column(expr)
}
//测试类
import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{SQLContext, functions => f}
import org.scalatest.FlatSpec

case class UserActivityData(user:String, ts:Long, session:String)

class CustomWindowFunctionTest extends FlatSpec with SharedSparkContext  {

  val st = System.currentTimeMillis()
  val one_minute = 60 * 1000

  val d = Array[UserActivityData](
    UserActivityData("user1",  st, "f237e656-1e53-4a24-9ad5-2b4576a4125d"),
    UserActivityData("user2",  st +   5*one_minute, null),
    UserActivityData("user1",  st +  10*one_minute, null),
    UserActivityData("user1",  st +  15*one_minute, null),
    UserActivityData("user2",  st +  15*one_minute, null),
    UserActivityData("user1",  st + 140*one_minute, null),
    UserActivityData("user1",  st + 160*one_minute, null))

  "a CustomWindowFunction" should "correctly create a session " in {
    val sqlContext = new SQLContext(sc)
    val df = sqlContext.createDataFrame(sc.parallelize(d))

    val specs = Window.partitionBy(f.col("user")).orderBy(f.col("ts").asc)
    val res = df.withColumn( "newsession", MyUDWF.calculateSession(f.col("ts"), f.col("session")) over specs)

    df.show(20)
    res.show(20, false)

    // there should be 3 sessions
    assert( res.groupBy(f.col("newsession")).agg(f.count("newsession")).count() == 3)

  }
  it should "be able to use the window size as a parameter" in {
    val sqlContext = new SQLContext(sc)
    val df = sqlContext.createDataFrame(sc.parallelize(d))

    val specs = Window.partitionBy(f.col("user")).orderBy(f.col("ts").asc)
    val res = df.withColumn( "newsession", MyUDWF.calculateSession(f.col("ts"), f.col("session"), f.lit(one_minute)) over specs)

    df.show(20)
    res.show(20, false)

    // there should be 3 sessions
    assert( res.groupBy(f.col("newsession")).agg(f.count("newsession")).count() == 7)

  }
  it should "be able to work without initial session" in {
    val sqlContext = new SQLContext(sc)
    val df = sqlContext.createDataFrame(sc.parallelize(d.drop(1))) // drop first

    val specs = Window.partitionBy(f.col("user")).orderBy(f.col("ts").asc)
    val res = df.withColumn( "newsession", MyUDWF.calculateSession(f.col("ts"), f.col("session")) over specs)

    df.show(20)
    res.show(20, false)

    // there should be 3 sessions
    assert( res.groupBy(f.col("newsession")).agg(f.count("newsession")).count() == 3)

  }

}