/*
UDF只能实现一进一出的操作，如果需要实现多进一出，则需要实现UDAF
1、UDF函数可以直接应用于select语句，对查询结构做格式化处理后，再输出内容。
2、编写UDF函数的时候需要注意一下几点：
    a）自定义UDF需要继承org.apache.hadoop.hive.ql.UDF
    b）需要实现evaluate函数。
    c）evaluate函数支持重载。

以下是两个数求和函数的UDF。evaluate函数代表两个整型数据相加，两个浮点型数据相加，可变长数据相加
Hive的UDF开发只需要重构UDF类的evaluate函数即可。例：
*/   
    import org.apache.hadoop.hive.ql.exec.UDF;  
    
    public final class Add extends UDF {  
    public Integer evaluate(Integer a, Integer b) {  
        if (null == a || null == b) {  
            return null;  
        } 
        return a + b;  
    }  
    
    public Double evaluate(Double a, Double b) {  
        if (a == null || b == null)  
            return null;  
            return a + b;  
        }  
    
    public Integer evaluate(Integer... a) {  
        int total = 0;  
        for (int i = 0; i < a.length; i++)  
            if (a[i] != null)  
                total += a[i];  
                return total;  
            }  
    } 
/*
使用步骤
    a）把程序打包放到目标机器上去；
    b）进入hive客户端，添加jar包：hive>add jar /run/jar/udf_test.jar;
    c）创建临时函数：hive>CREATE TEMPORARY FUNCTION add_example AS 'hive.udf.Add';
    d）查询HQL语句：
        SELECT add_example(8, 9) FROM scores;
        SELECT add_example(scores.math, scores.art) FROM scores;
        SELECT add_example(6, 7, 8, 6.8) FROM scores;
    e）销毁临时函数：hive> DROP TEMPORARY FUNCTION add_example;

细节在使用UDF的时候，会自动进行类型转换，例如：
    SELECT add_example(8,9.1) FROM scores;
*/