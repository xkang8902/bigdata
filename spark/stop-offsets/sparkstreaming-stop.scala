问题一，如果kafka要新增分区，对于正在运行的实时流程序能否动态识别到？
    经过测试，是不能识别的，我推测使用createDirectStream创建流对象一旦创建就是不可变的，也就是说创建实例那一刻的分区数量，会一直使用直到流程序结束，就算中间kafka的分区数量扩展了，流程序也是不能识别到的。
    所以在扩展kafka分区前，一定要先把流程序给停掉，然后扩展完成后需要再次重启流程序。
问题二，如果需要重启，那么在自己管理offset时，如何才能识别到新增的分区？
    如果是我们自己管理offset时，一定要考虑到kafka扩展分区的情况，每次启动程序前都得检测下目前保存的偏移量里面的kafka的分区个数是否小于kafka实际元数据里面实际的分区个数，正常没扩展分区的情况下两个值应该是相等的，如果值不一致，就说明是kafka分区得到扩展了，所以我们的程序需要能够兼容这种情况。
    核心代码如下：
    //这个topic在zk里面最新的分区数量
        val lastest_partitions= ZkUtils.getPartitionsForTopics(zkClient,Seq(topic)).get(topic).get
        var offsets = offsetsRangesStr.split(",")//按逗号split成数组
            .map(s => s.split(":"))//按冒号拆分每个分区和偏移量
            .map { case Array(partitionStr, offsetStr) => (TopicAndPartition(topic, partitionStr.toInt) -> offsetStr.toLong) }//加工成最终的格式
            .toMap//返回一个Map
        //说明有分区扩展了
        if(offsets.size < lastest_partitions.size){
            //得到旧的所有分区序号
            val old_partitions=offsets.keys.map(p=>p.partition).toArray
            //通过做差集得出来多的分区数量数组
            val add_partitions=lastest_partitions.diff(old_partitions)
            if(add_partitions.size > 0){
                log.warn("发现kafka新增分区：" + add_partitions.mkString(","))
                add_partitions.foreach(partitionId=>{
                    offsets += (TopicAndPartition(topic,partitionId)->0)
                    log.warn("新增分区id：" + partitionId + "添加完毕....")
                })
            }
        }else{
            log.warn("没有发现新增的kafka分区：" + lastest_partitions.mkString(","))
        }

    上面的代码在每次启动程序时，都会检查当前我们自己管理的offset的分区数量与zk元数据里面实际的分区数量，如果不一致就会把新增的分区id给加到TopicAndPartition里面并放入到Map对象里面，这样在启动前就会传入到createDirectStream对象中，就能兼容新增的分区了。
问题三:spark streaming优雅关闭的策略还有那些？
    spark.streaming.stopGracefullyOnShutdown  默认为false 设置为true

    1.通过http暴露服务的核心代码：
        /****
        * 负责启动守护的jetty服务
        * @param port 对外暴露的端口号
        * @param ssc Stream上下文
        */
        def daemonHttpServer(port:Int,ssc: StreamingContext)={
            val server = new Server(port)
            val context = new ContextHandler();
            context.setContextPath( "/close" );
            context.setHandler(new CloseStreamHandler(ssc))
            server.setHandler(context)
            server.start()
        }

        /*** 负责接受http请求来优雅的关闭流
        * @param ssc  Stream上下文
        */
        class CloseStreamHandler(ssc:StreamingContext) extends AbstractHandler {
            override def handle(s: String, baseRequest: Request, req: HttpServletRequest, response: HttpServletResponse): Unit ={
                log.warn("开始关闭......")
                ssc.stop(true,true)//优雅的关闭
                response.setContentType("text/html; charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                val out = response.getWriter();
                out.println("close success");
                baseRequest.setHandled(true);
                log.warn("关闭成功.....")
            }
        }
    触发停止流程序
        在启动服务的机器上，执行下面封装的脚本：
        ## tx.log是提交spark任务后的输出log重定向的log 
        ## &> tx.log &  
        #!/bin/bash
        driver=`cat tx.log | grep ApplicationMaster | grep -Po '\d+.\d+.\d+.\d+'`
        echo $driver
        curl http://$driver:port/close/
        echo "stop finish"

    2.通过扫描HDFS文件的方式：
        def stopByMarkFile(ssc:StreamingContext):Unit= {
            val intervalMills = 10 * 1000 // 每隔10秒扫描一次消息是否存在
            var isStop = false
            val hdfs_file_path = "/spark/streaming/stop" //判断消息文件是否存在，如果存在就
            while (!isStop) {
                isStop = ssc.awaitTerminationOrTimeout(intervalMills)
                if (!isStop && isExistsMarkFile(hdfs_file_path)) {
                    log.warn("2秒后开始关闭sparstreaming程序.....")
                    Thread.sleep(2000)
                    ssc.stop(true, true)
                }
            }
        }
        def isExistsMarkFile(hdfs_file_path:String):Boolean={
            val conf = new Configuration()
            val path=new Path(hdfs_file_path)
            val fs =path.getFileSystem(conf);
            fs.exists(path)
        }
    触发停止流程
        找到一个拥有HDFS客户端机器，向HDFS上写入指定的文件：
        #生成文件后，10秒后程序就会自动停止
        hadoop fs -touch /spark/streaming/stop
        #下次启动前，需要清空这个文件，否则程序启动后就会停止
        hadoop fs -rm -r /spark/streaming/stop

所有代码已经同步更新到我的github上，有兴趣的朋友可以参考这个链接：https://github.com/Talefairy/sparkStreaming-offset-to-zk



