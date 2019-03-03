#例1：
spark-submit
#运行方式
–master yarn-cluster
#指定driver端得到核数
–driver-cores 4
#指定driver端的内存
–driver-memory 2G
#指定excutor的个数
–num-executors 10
#指定每个executor的个数
–executor-cores 2
#指定每个executor的内存
–executor-memory 4G
#指定堆外内存
–conf spark.yarn.executor.memoryOverhead=4096
#指定是否自动释放内存
–conf spark.streaming.unpersist=true
#指定GC
–conf spark.executor.extraJavaOptions=-XX:+UseConcMarkSweepGC
#指定冷启动最大拉取量，每个分区每秒
–conf spark.streaming.kafka.maxRatePerPartition=3000
-driver
#指定用到的jar包  driver 和 executor都需要的jar包
–jars /opt/Bigdata/AdxTestLib/weidaihong/kafka-clients-0.10.0.1.jar,/opt/Bigdata /AdxTestLib/weidaihong/spark-streaming-kafka-0-10_2.11-2.2.0.jar,/opt/Bigdata/AdxTestLib/weidaihong/fastjson-1.2.47.jar,/opt/Bigdata/AdxTestLib/weidaihong/config-1.3.3.jar,/opt/Bigdata/AdxTestLib/weidaihong/commons-dbutils-1.7.jar,/opt/Bigdata/AdxTestLib/weidaihong/c3p0-0.9.1.2.jar
#指定运行程序的类名以及jar包
–class streaming.app.Dwd2DwsMonitor /opt/Bigdata/AdxTestLib/weidaihong/Delivery.jar
#需要传入的参数
“adxDwdRateStockingLog” “adxDwsRateStockingLog” “false”

#例2：
#!/bin/sh
TaskName="funnel"
UserName="hadoop"
cd `dirname $0`
nohup sudo -u ${UserName} /data/bigdata/spark/bin/spark-submit \
-name ${TaskName} \
-class FunnelMain \
-master yarn \
-deploy-mode cluster \
-executor-memory 2G \
-num-executors 3 \
-conf spark.streaming.backpressure.enabled=true \
-conf spark.streaming.backpressure.initialRate=1000 \
#将文件添加到executor工作目录
-files /data/apps/funnel/app/conf/conf.properties \   
/data/apps/funnel/app/target/apphadoop-1-jar-with-dependencies.jar conf.properties >>../log/${TaskName}.log 2>&1 &
exit 0



#其他重要参数：
#Spark提供三个位置用来配置系统：
    #Spark属性：控制大部分的应用程序参数，可以用SparkConf对象或者Java系统属性设置
    #环境变量：可以通过每个节点的conf/spark-env.sh脚本设置。例如IP地址、端口等信息
    #日志配置：可以通过log4j.properties配置

    #运行环境：
    #spark.driver.extraClassPath                     (none)                      附加到driver的classpath的额外的classpath实体。
    #spark.driver.extraJavaOptions                   (none)                      传递给driver的JVM选项字符串。例如GC设置或者其它日志设置。注意，在这个选项中设置Spark属性或者堆大小是不合法的。Spark属性需要用--driver-class-path设置。
    #spark.executor.extraClassPath                   (none)                      附加到executors的classpath的额外的classpath实体。这个设置存在的主要目的是Spark与旧版本的向后兼容问题。用户一般不用设置这个选项
    #spark.executor.extraJavaOptions                 (none)                      传递给executors的JVM选项字符串。例如GC设置或者其它日志设置。注意，在这个选项中设置Spark属性或者堆大小是不合法的。Spark属性需要用SparkConf对象或者spark-submit脚本用到的spark-defaults.conf文件设置。堆内存可以通过spark.executor.memory设置
    #spark.executor.logs.rolling.time.interval       daily                       executor日志滚动的时间间隔。默认情况下没有开启。合法的值是daily, hourly, minutely以及任意的秒。
    
    #shuffle:
    #spark.shuffle.blockTransferService              netty                       实现用来在executor直接传递shuffle和缓存块。有两种可用的实现：netty和nio。基于netty的块传递在具有相同的效率情况下更简单
    #spark.reducer.maxMbInFlight                     48                          从递归任务中同时获取的map输出数据的最大大小（mb）。因为每一个输出都需要我们创建一个缓存用来接收，这个设置代表每个任务固定的内存上限，所以除非你有更大的内存，将其设置小一点
    #spark.shuffle.consolidateFiles                  false                       如果设置为”true”，在shuffle期间，合并的中间文件将会被创建。创建更少的文件可以提供文件系统的shuffle的效 率。这些shuffle都伴随着大量递归任务。当用ext4和dfs文件系统时，推荐设置为”true”。在ext3中，因为文件系统的限制，这个选项可 能机器（大于8核）降低效率
    #spark.shuffle.file.buffer.kb                    32                          每个shuffle文件输出流内存内缓存的大小，单位是kb。这个缓存减少了创建只中间shuffle文件中磁盘搜索和系统访问的数量
    #spark.shuffle.io.maxRetries                     3                           Netty only，自动重试次数
    #spark.shuffle.io.retryWait                      5                           Netty only
    #spark.shuffle.manager                           sort                        它的实现用于shuffle数据。有两种可用的实现：sort和hash。基于sort的shuffle有更高的内存使用率
    #spark.shuffle.memoryFraction                    0.2                         如果spark.shuffle.spill为 true，shuffle中聚合和合并组操作使用的java堆内存占总内存的比重。在任何时候，shuffles使用的所有内存内maps的集合大小都受 这个限制的约束。超过这个限制，spilling数据将会保存到磁盘上。如果spilling太过频繁，考虑增大这个值
    #spark.shuffle.spill                             true                        如果设置为”true”，通过将多出的数据写入磁盘来限制内存数。通过spark.shuffle.memoryFraction来指定spilling的阈值
    #spark.shuffle.spill.compress                    true                        在shuffle时，是否将spilling的数据压缩。压缩算法通过spark.io.compression.codec指定。
    
    #Spark UI:
    #spark.eventLog.compress                         false                       是否压缩事件日志。需要spark.eventLog.enabled为true
    #spark.eventLog.dir                              file:///tmp/spark-events    Spark事件日志记录的基本目录。在这个基本目录下，Spark为每个应用程序创建一个子目录。各个应用程序记录日志到直到的目录。用户可能想设置这为统一的地点，像HDFS一样，所以历史文件可以通过历史服务器读取
    #spark.eventLog.enabled                          false                       是否记录Spark的事件日志。这在应用程序完成后，重新构造web UI是有用的
    #spark.ui.killEnabled                            true                        在web UI中杀死stage和相应的job
    #spark.ui.port                                   4040                        你的应用程序dashboard的端口。显示内存和工作量数据
    #spark.ui.retainedJobs                           1000                        在垃圾回收之前，Spark UI和状态API记住的job数
    #spark.ui.retainedStages                         1000                        在垃圾回收之前，Spark UI和状态API记住的stage数

    #压缩和序列化:
    #spark.broadcast.compress                        true                        在发送广播变量之前是否压缩它
    #spark.io.compression.codec                      snappy                      压缩诸如RDD分区、广播变量、shuffle输出等内部数据的编码解码器。默认情况下，Spark提供了三种选择：lz4、lzf和snappy，你也可以用完整的类名来制定。
        #spark.io.compression.lz4.block.size             32768                       LZ4压缩中用到的块大小。降低这个块的大小也会降低shuffle内存使用率
        #spark.io.compression.snappy.block.size          32768                       Snappy压缩中用到的块大小。降低这个块的大小也会降低shuffle内存使用率
    #spark.kryo.registrator                          (none)                      如果你用Kryo序列化，设置这个类去注册你的自定义类。如果你需要用自定义的方式注册你的类，那么这个属性是有用的。否则spark.kryo.classesToRegister会更简单。它应该设置一个继承自KryoRegistrator的类
    #spark.kryoserializer.buffer.max.mb              64                          Kryo序列化缓存允许的最大值。这个值必须大于你尝试序列化的对象
    #spark.rdd.compress                              true                        是否压缩序列化的RDD分区。在花费一些额外的CPU时间的同时节省大量的空间
    #spark.serializer                                org.apache.spark.serializer.JavaSerializer      序列化对象使用的类。默认的Java序列化类可以序列化任何可序列化的java对象但是它很慢。所有我们建议用org.apache.spark.serializer.KryoSerializer

    #运行时行为:
    #spark.cleaner.ttl                               (infinite)                  spark记录任何元数据（stages生成、task生成等）的持续时间。定期清理可以确保将超期的元数据丢弃，这在运行长时间任务是很有用的，如运行7*24的sparkstreaming任务。RDD持久化在内存中的超期数据也会被清理
    #spark.default.parallelism                       本地模式：机器核数；Mesos：8；其他：max(executor的core，2)      如果用户不设置，系统使用集群中运行shuffle操作的默认任务数（groupByKey、 reduceByKey等）
    #spark.storage.memoryFraction                    0.6                         Spark内存缓存的堆大小占用总内存比例，该值不能大于老年代内存大小，默认值为0.6，但是，如果你手动设置老年代大小，你可以增加该值

    #调度相关属性:
    #spark.task.maxFailures                          4                           Task的最大重试次数
    #spark.scheduler.mode                            FIFO                        Spark的任务调度模式，还有一种Fair模式  推荐Fair
        #spark.scheduler.allocation.file             pool配置文件路径
        #spark.scheduler.pool                        设置的poolID
    #spark.speculation                               False                       以下几个参数是关于Spark推测执行机制的相关参数。此参数设定是否使用推测执行机制，如果设置为true则spark使用推测执行机制，对于Stage中拖后腿的Task在其他节点中重新启动，并将最先完成的Task的计算结果最为最终结果
        #spark.speculation.interval                  100                         Spark多长时间进行检查task运行状态用以推测，以毫秒为单位
        #spark.speculation.quantile                                              推测启动前，Stage必须要完成总Task的百分比
        #spark.speculation.multiplier                1.5                         比已完成Task的运行速度中位数慢多少倍才启用推测
    #spark.locality.wait                             3000                        以下几个参数是关于Spark数据本地性的。本参数是以毫秒为单位启动本地数据task的等待时间，如果超出就启动下一本地优先级别 的task。该设置同样可以应用到各优先级别的本地性之间（本地进程 -> 本地节点 -> 本地机架 -> 任意节点 ），当然，也可以通过spark.locality.wait.node等参数设置不同优先级别的本地性
        #spark.locality.wait.process                 spark.locality.wait         本地进程级别的本地等待时间
        #spark.locality.wait.node                    spark.locality.wait         本地节点级别的本地等待时间
        #spark.locality.wait.rack                    spark.locality.wait         本地机架级别的本地等待时间
    #spark.scheduler.revive.interval                 1000                        复活重新获取资源的Task的最长时间间隔（毫秒），发生在Task因为本地资源不足而将资源分配给其他Task运行后进入等待时间，如果这个等待时间内重新获取足够的资源就继续计算

    #Dynamic Allocation:
    #spark.dynamicAllocation.enabled                 false                       是否开启动态资源搜集

    #Spark Streaming:
    #spark.streaming.blockInterval                   200                         在这个时间间隔（ms）内，通过Spark Streaming receivers接收的数据在保存到Spark之前，chunk为数据块。推荐的最小值为50ms
    #spark.streaming.receiver.maxRate                infinite                    每秒钟每个receiver将接收的数据的最大记录数。有效的情况下，每个流将消耗至少这个数目的记录。设置这个配置为0或者-1将会不作限制
    #spark.streaming.receiver.writeAheadLogs.enable  false                       Enable write ahead logs for receivers. All the input data received through receivers will be saved to write ahead logs that will allow it to be recovered after driver failures
    #spark.streaming.unpersist                       true                        强制通过Spark Streaming生成并持久化的RDD自动从Spark内存中非持久化。通过Spark Streaming接收的原始输入数据也将清除。设置这个属性为false允许流应用程序访问原始数据和持久化RDD，因为它们没有被自动清除。但是它会 造成更高的内存花费
    #spark.streaming.stopGracefullyOnShutdown        fasle                       true / false;确保在kill任务时，能够处理完最后一批数据，再关闭程序，不会发生强制kill导致数据处理中断，没处理完的数据丢失
    #spark.streaming.backpressure.enabled            false                       true / false; 开启后spark自动根据系统负载选择最优消费速率
        #spark.streaming.backpressure.initialRate    默认直接读取所有               限制第一次批处理应该消费的数据，因为程序冷启动 队列里面有大量积压，防止第一次全部读取，造成系统阻塞
    #spark.streaming.kafka.maxRatePerPartition       默认直接读取所有               限制每秒每个消费线程读取每个kafka分区最大的数据量

    #SparkSql:
    #spark.sql.shuffle.partitions                    整数值                       设置spark-sql程序的并行度
    #spark.sql.codegen                               false                       当它设置为true时，Spark SQL会把每条查询的语句在运行时编译为java的二进制代码。这有什么作用呢？它可以提高大型查询的性能，但是如果进行小规模的查询的时候反而会变慢，就是说直接用查询反而比将它编译成为java的二进制代码快。所以在优化这个选项的时候要视情况而定。
    #spark.sql.inMemoryColumnStorage.compressed      false                       它的作用是自动对内存中的列式存储进行压缩
    #spark.sql.inMemoryColumnStorage.batchSize       1000                        这个参数代表的是列式缓存时的每个批处理的大小。如果将这个值调大可能会导致内存不够的异常，所以在设置这个的参数的时候得注意你的内存大小


    #环境变量:
    #当Spark安装时，conf/spark-env.sh默认是不存在的。你可以复制conf/spark-env.sh.template创建它。可以在spark-env.sh中设置如下变量：
    #环境变量	                含义
    #JAVA_HOME               Java安装的路径
    #PYSPARK_PYTHON          PySpark用到的Python二进制执行文件路径
    #SPARK_LOCAL_IP          机器绑定的IP地址
    #SPARK_PUBLIC_DNS        你Spark应用程序通知给其他机器的主机名

    #配置日志:
    #Spark用log4j logging。你可以通过在conf目录下添加log4j.properties文件来配置。一种方法是复制log4j.properties.template文件。




