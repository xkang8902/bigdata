注册自定义函数的三种方式:
    1、create function xxx as '自定义类的全路径';
    2、create temporary function xxx as '自定义类的全路径'; //临时自定义函数,重启hive之后不再发挥作用
    3、create function xxx as '' using jar 'hdfs://mycluster/xxx.jar'; //jar在hdfs上的位置
