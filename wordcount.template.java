//1.mrwordcount    ----->job---maptask--reducetask
    public class WordCountDemo{
        static class WordCountMapper extends Mapper<LongWritable,Text,Text,IntWritable>{
            @overwrite
            public void map(LongWritable key,Text value,Context context){
                String line = value.toString();
                String[] words = line.split(" ");
                for(String string : words){
                    context.write(new Text(string),new IntWritable(1));
                }
            }
        }
        static class WordCountReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
            @overwrite
            public void reduce(Text key,Iterable<IntWritable> values,Context context){
                int count = 0;
                for(IntWritable value:values){
                    count+=value.get();
                }
                context.write(key,new IntWritable(count));
            }
        }
        public static void main(String[] args){
            Configration conf = new Configraturation();
            Job wcjob = Job.getInstance();

            wcjob.setJarByClass(WordCountDemo.class);
            wcjob.setMapperClass(WordCountMapper.class);
            wcjob.setReducerClass(WordCountReducer.class);

            wcjob.setMapOutputKeyClass(Text.class);
            wcjob.setMapOutputValueClass(IntWritable.class);

            wcjob.setOutputKeyClass(Text.class);
            wcjob.setOutputValueClass(IntWritable.class);

            FileInputFormat.setInputPaths(wcjob,new Path("/"));
            FileOutputFormat.setOutputPath(wcjob,new Path("/");

            boolean res = wcjob.waitForCompletion(true);
            System.exit(res ? 0 : 1);
        }
    }
/** 
 * 2.stormwordcount     ----->topology---worker---executor
    spout: 
        BaseRichSpout      一个接口   4个方法
            open(Map conf,TopologyContext context,SpoutCollector collector)     数据源设置
            nextTuple()   处理逻辑
            declareOutputFields(OutputFieldsDeclare fieldsDeclare)    与nextTuple中emit的tuple（new values（“v1”，“v2”))对应的key   以供下游bolt根据字段获取对应的value
            fail(String msgid)     处理失败后重发的问题
    bolt: 两个接口  三个方法
        BaseRichBolt   BaseBasicBolt   
        prepare(Map conf,TopologyContext context,Collector collector)   
        nextTuple(Tuple input)   
        declareOutputFields()   
    可靠性设定：
        spout：
            1.emit时加msgid   2.重写fail()方法
        bolt:
            1.emit时添加锚定anchor    2.collector.ack(tuple)
*/
//   ReadFileSpout:
        public class ReadFileSpout extends BaseRichSpout{
            private SpoutCollector collector;
            public void open(Map conf,TopologyContext context,SpoutCollector collector){
                this.collector=collector;
            }
            public void nextTuple(){
                collector.emit(new Values("hello world storm spark hello"),"hello:world:storm:spark:hello");
            }
            public void fail(String msgid){
                collector.emit(new Values(msgid.replace(":"," ")));
            }
            public void declareOutputFields(OutputFieldsDeclare declare){
                declare.declareFields(new Fields("sentense"))
            }
        }
//    SentenseSplitBolt
        public class SentenseSplitBolt extends BaseRichBolt{
            private Collector collector;
            public void prepare(Map conf,TopologyContext context,Collector collector){
                this.collector = collector;
            }
            public void nextTuple(Tuple input){
                String line = input.getString(0);
                //String line = input.getStringByField(sentense);
                for(String word:line.split(" ")){
                    collector.emit(input,new Values(word,1));
                }
                collector.ack(input);
            }
            public void declareOutputFields(OutputFieldsDeclare declare){
                declare.declareFields(new Fields("word","num"));
            }
        }
//   WordCountBolt:
        public class WordCountBolt extends BaseRichBolt{
            private Collector collector;
            private Map<String,Integer> map;
            public void prepare(Map conf,TopologyContext context,Collector collector){
                this.collector=collector;
                map = new HashMap<String,Integer>();
            }
            public void nextTuple(Tuple input){
                String word = input.getStringByField("word");
                Integer num = input.getIntegerByField("num");
                if(map.containKey(word)){
                    map.put(word,map.get(word)+num);
                }else{
                    map.put(word,num);
                }
                System.out.println(map);

            }
        }
//    WordCountTopology:
        public class WordCountTopology{
            public void main(String[] args){
                TopologyBuilder builder = new TopologyBuilder();

                builder.setSpout("readfilespout",new ReadFileSpout(),1);
                builder.setBolt("sentensesplitbolt",new SentenseSplitBolt(),2).localorShuffleGrouping("readfilespout");
                builder.setBolt("wordcountbolt",new WordCountBolt(),4).localorShuffleGrouping("sentensesplitbolt");

                Config conf = new Config();
                conf.setDebug(false);
                if(args.length>0 && args!=null){
                    conf.setNumWorkers(2);
                    StormSubmiter.submitTopology(”wordcounttopology",conf,builder.createTopology());
                }else{
                    LocalCluster localCluster = new LocalCluster();
                    localCluster.submitTopology(“wordcounttopology",conf,builder.createTopology());
                }
                

            }
        }
//3.sparkTOPN:    ----->driver(jobs)--executor--task(taskset)
    obect TOPN{
        def main(args:Array[String]){
            val TOPN:Array[(String,Integer)] = new SparkContext(new SparkConf().setAppName("TOPN").setMaster("local(2)"))
                .textFile("/")          //得到的是RDD[String]
                .flatmap(_.split(" "))  //得到的是RDD[String]
                .map((_,1))             //得到的是RDD[(String,Integer)]
                .reducebykey(_._2+_._2)   //得到的是RDD[(String,Integer)]
                .sortby(_._2)
                .take(3)                    //得到的是array数组
                //.foreach(println)      foreach是RDD算子   不能作用于数组
                println(TOPN.tobuffer)  //数组打印需要将数组转换   即.toBuffer
        }
    }





    

