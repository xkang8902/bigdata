/*继承org.apache.hadoop.hive.ql.udf.generic.GenericUDTF,实现initialize, process, close三个方法。

UDTF首先会调用initialize方法，此方法返回UDTF的返回行的信息（返回个数，类型）。
初始化完成后，会调用process方法,真正的处理过程在process函数中，在process中，每一次forward()调用产生一行；如果产生多列可以将多个列的值放在一个数组中，然后将该数组传入到forward()函数。
最后close()方法调用，对需要清理的方法进行清理。
下面是我写的一个用来切分json是数据，见main函数。供参考：
*/
 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
 
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
 
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
 
public class Redis_Gmp extends GenericUDTF{
 
	@Override
	public void close() throws HiveException {
		// TODO Auto-generated method stub
		
	}
 
	@Override
	public void process(Object[] arg0) throws HiveException {
		
		String input = arg0[0].toString();
		String[] split = input.split(",\\[");
		
		String content_id = split[0].replace("(", "");
		String json_str = split[1].replace(")", "");
		json_str = "["+json_str;
		
		JSONArray json_arr = JSONArray.parseArray(json_str);
		for(int i =0 ;i < json_arr.size();i++){
			
			String[] result = new String[5];
			result[0] = content_id;
			JSONObject ele = json_arr.getJSONObject(i);
			Set<String> ks = ele.keySet();
			for(String k : ks){
				result[1] = k;
			}
			result[2] = ele.getJSONObject(result[1]).getString("click");
			result[3] = ele.getJSONObject(result[1]).getString("impression");
			result[4] = ele.getJSONObject(result[1]).getString("ctr");
			forward(result);
			//System.out.println(result[0] + " " + result[1] + " " + result[2] +" "+result[3]+" "+result[4]);
		}
		
	}
	
	@Override
	public StructObjectInspector initialize(ObjectInspector[] args)
			throws UDFArgumentException {
		if (args.length != 1) {
			throw new UDFArgumentLengthException(
					"Redis_Gmp takes only one argument");
		}
		if (args[0].getCategory() != ObjectInspector.Category.PRIMITIVE) {
			throw new UDFArgumentException(
					"Redis_Gmp takes string as a parameter");
		}
 
		ArrayList<String> fieldNames = new ArrayList<String>();
		ArrayList<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>();
		fieldNames.add("content_id");
		fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
		fieldNames.add("app");
		fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
		fieldNames.add("click");
		fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
		fieldNames.add("impression");
		fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
		fieldNames.add("ctr");
		fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
 
		return ObjectInspectorFactory.getStandardStructObjectInspector(
				fieldNames, fieldOIs);
	}
	
	public static void main(String[] args) throws HiveException {
		
		//(119962233,[{"-1":{"impression":150,"click":0.3438084,"ctr":0.006597938}},{"coolpad":{"impression":56,"click":0.3438084,"ctr":0.018344998}},{"emui":{"impression":64,"click":0,"ctr":0}}])
		//(98337176,[{"-1":{"impression":167,"click":0.9933209,"ctr":0.02424849}},{"ali":{"impression":163,"click":0.9933209,"ctr":0.025131164}}])
		
		Redis_Gmp redis_gmp = new Redis_Gmp();
		
		String s1 = "(98337176,[{\"-1\":{\"impression\":167,\"click\":0.9933209,\"ctr\":0.02424849}},{\"ali\":{\"impression\":163,\"click\":0.9933209,\"ctr\":0.025131164}}])";
		String s2 = "(119962233,[{\"-1\":{\"impression\":150,\"click\":0.3438084,\"ctr\":0.006597938}},{\"coolpad\":{\"impression\":56,\"click\":0.3438084,\"ctr\":0.018344998}},{\"emui\":{\"impression\":64,\"click\":0,\"ctr\":0}}])";
		
		Object[] arg0 = new Object[]{s2};
		
		redis_gmp.process(arg0);
		
		
	}
 
	
	
}
/*
使用方法
UDTF有两种使用方法，一种直接放到select后面，一种和lateral view一起使用。

1：直接select中使用
    select explode_map(properties) as (col1,col2) from src;
    不可以添加其他字段使用

    select a, explode_map(properties) as (col1,col2) from src
    不可以嵌套调用

    select explode_map(explode_map(properties)) from src
    不可以和group by/cluster by/distribute by/sort by一起使用

    select explode_map(properties) as (col1,col2) from src group by col1, col2
2：和lateral view一起使用
    select src.id, mytable.col1, mytable.col2 from src lateral view explode_map(properties) mytable as col1, col2;

    create external table if not exists db_offline.tmp_spark_streaming_redis_gmp(  
    data string
    )  
    partitioned by (statdate string)
    location '/inveno-projects/article-gmp-sparkstreaming/data/redis';
    
    
    create table if not exists db_offline.s_spark_streaming_redis_gmp(  
    content_id string,
    app string,
    click string,
    impression string,
    ctr string
    )  
    partitioned by (statdate string);
    add jar hdfs://inveno/third_party_jars/fastjson-1.2.4.jar;  
    create temporary function redis_gmp as 'com.inveno.udtf.Redis_Gmp' using jar 'hdfs://inveno/hive-udf/redis_gmp.jar';
    
    insert overwrite table db_offline.s_spark_streaming_redis_gmp partition(statdate='${min}')
    select content_id,app,click,impression,ctr
    from db_offline.tmp_spark_streaming_redis_gmp
    lateral view redis_gmp(data) json as content_id,app,click,impression,ctr
	where statdate = '${min}'
	*/
