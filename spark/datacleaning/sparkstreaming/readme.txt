1）需求
    统计今天到目前为止的访问量
    统计今天到目前为止从搜索引擎过来的课程的访问量

2）开发环境与技术选型
    IDEA+maven
    flume+kafka+HBase

3）安装配置 HBase
    下载、解压、配置环境变量
    配置文件
    conf/hbase-env.sh
        修改JAVA_HOME
        export HBASE_MANAGES_ZK=false

    conf/hbase-site.xml
        <configuration>
            <property>
                <name>hbase.rootdir</name>
                <value>hdfs://localhost:8020/hbase</value>
            </property>
            <property>
                <name>hbase.cluster.distributed</name>
                <value>true</value>
            </property>
            <property>
                <name>hbase.zookeeper.quorum</name>
                <value>localhost:2181</value>
            </property> 
        </configuration>

    conf/regionservers
        localhost

4）HBase 建表

    // 1 启动hbase
    start-hbase.sh
    // 2 启动shell
    hbase shell
    // 3 建表
    create 'course_clickcount','info'
    create 'course_search_clickcount','info'
    // 4 查看数据表
    list
    // 5 查看数据表信息
    describe 'course_clickcount'
    // 6 查看表数据
    scan 'course_clickcount'

5) 运行测试
1）启动 zookeeper
    zkServer.sh start

2）启动 HDFS
    start-dfs.sh
    start-yarn.sh

3）启动 kafka
    kafka-server-start.sh -daemon $KAFKA_HOME/config/server.properties $

4）启动 flume
    flume-ng agent \
    --conf $FLUME_HOME/conf \
    --conf-file $FLUME_HOME/conf/streaming_project2.conf \
    --name exec-memory-kafka \
    -Dflume.root.logger=INFO,console

5）运行日志生成器
    python3 generate_log.py

6）运行spark程序
    spark-submit \
    --class com.lihaogn.sparkProject.main.SparkStreamingApp \
    --master local[5] \
    --name SparkStreamingApp \
    --jars /Users/Mac/software/spark-streaming-kafka-0-8-assembly_2.11-2.2.0.jar,$(echo /Users/Mac/app/hbase-1.2.0-cdh5.7.0/lib/*.jar | tr ' ' ',') \
    /Users/Mac/my-lib/Kafka-train-1.0.jar \
    localhost:2181 test test_topic 1


