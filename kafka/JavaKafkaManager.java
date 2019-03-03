package org.apache.spark.streaming.kafka;

import kafka.common.TopicAndPartition;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import org.apache.spark.SparkException;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.mutable.ArrayBuffer;
import scala.util.Either;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author wei
 * @date 10/24/17
 */
public class JavaKafkaManager implements Serializable{

    private scala.collection.immutable.Map kafkaParams;
    private KafkaCluster kafkaCluster;

    public JavaKafkaManager(Map kafkaParams) {
        //TODO
        this.kafkaParams = toScalaImmutableMap(kafkaParams);
        kafkaCluster = new KafkaCluster(this.kafkaParams);
    }

    public JavaInputDStream  createDirectStream(
                                   JavaStreamingContext jssc,
                                   Map kafkaParams,
                                   Set topics) throws SparkException {

        String groupId = kafkaParams.get("group.id");

        // 在zookeeper上读取offsets前先根据实际情况更新offsets
        setOrUpdateOffsets(topics, groupId);

        //从zookeeper上读取offset开始消费message
        //TODO
        scala.collection.immutable.Set immutableTopics = JavaConversions.asScalaSet(topics).toSet();
        Either, scala.collection.immutable.Set> partitionsE
                = kafkaCluster.getPartitions(immutableTopics);

        if (partitionsE.isLeft()){
            throw new SparkException("get kafka partition failed: ${partitionsE.left.get}");
        }
        Either.RightProjection, scala.collection.immutable.Set>
                partitions = partitionsE.right();
        Either, scala.collection.immutable.Map> consumerOffsetsE
                = kafkaCluster.getConsumerOffsets(groupId, partitions.get());

        if (consumerOffsetsE.isLeft()){
            throw new SparkException("get kafka consumer offsets failed: ${consumerOffsetsE.left.get}");
        }
        scala.collection.immutable.Map
                consumerOffsetsTemp = consumerOffsetsE.right().get();
        Map consumerOffsets = JavaConversions.mapAsJavaMap(consumerOffsetsTemp);

        Map consumerOffsetsLong = new HashMap();
        for (TopicAndPartition key: consumerOffsets.keySet()){
            consumerOffsetsLong.put(key, (Long)consumerOffsets.get(key));
        }

        JavaInputDStream message = KafkaUtils.createDirectStream(
                jssc,
                String.class,
                String.class,
                StringDecoder.class,
                StringDecoder.class,
                String.class,
                kafkaParams,
                consumerOffsetsLong,
                new Function, String>() {
                    @Override
                    public String call(MessageAndMetadata v) throws Exception {
                        return v.message();
                    }
                });

        return message;
    }

    /**
     * 创建数据流前，根据实际消费情况更新消费offsets
     * @param topics
     * @param groupId
     */
    private void setOrUpdateOffsets(Set topics, String groupId) throws SparkException {
        for (String topic: topics){
            boolean hasConsumed = true;
            HashSet topicSet = new HashSet<>();
            topicSet.add(topic);
            scala.collection.immutable.Set immutableTopic = JavaConversions.asScalaSet(topicSet).toSet();
            Either, scala.collection.immutable.Set>
                    partitionsE = kafkaCluster.getPartitions(immutableTopic);

            if (partitionsE.isLeft()){
                throw new SparkException("get kafka partition failed: ${partitionsE.left.get}");
            }
            scala.collection.immutable.Set partitions = partitionsE.right().get();
            Either, scala.collection.immutable.Map>
                    consumerOffsetsE = kafkaCluster.getConsumerOffsets(groupId, partitions);

            if (consumerOffsetsE.isLeft()){
                hasConsumed = false;
            }

            if (hasConsumed){// 消费过
                /**
                 * 如果streaming程序执行的时候出现kafka.common.OffsetOutOfRangeException，
                 * 说明zk上保存的offsets已经过时了，即kafka的定时清理策略已经将包含该offsets的文件删除。
                 * 针对这种情况，只要判断一下zk上的consumerOffsets和earliestLeaderOffsets的大小，
                 * 如果consumerOffsets比earliestLeaderOffsets还小的话，说明consumerOffsets已过时,
                 * 这时把consumerOffsets更新为earliestLeaderOffsets
                 */
                Either, scala.collection.immutable.Map>
                        earliestLeaderOffsetsE = kafkaCluster.getEarliestLeaderOffsets(partitions);
                if (earliestLeaderOffsetsE.isLeft()){
                    throw new SparkException("get earliest leader offsets failed: ${earliestLeaderOffsetsE.left.get}");
                }

                scala.collection.immutable.Map
                        earliestLeaderOffsets = earliestLeaderOffsetsE.right().get();
                scala.collection.immutable.Map
                        consumerOffsets = consumerOffsetsE.right().get();

                // 可能只是存在部分分区consumerOffsets过时，所以只更新过时分区的consumerOffsets为earliestLeaderOffsets
                HashMap offsets = new HashMap<>();
                Map
                        topicAndPartitionObjectMap = JavaConversions.mapAsJavaMap(consumerOffsets);
                for (TopicAndPartition key: topicAndPartitionObjectMap.keySet()){
                    Long n = (Long) topicAndPartitionObjectMap.get(key);
                    long earliestLeaderOffset = earliestLeaderOffsets.get(key).get().offset();
                    if (n < earliestLeaderOffset){
                        System.out.println("consumer group:"
                                + groupId + ",topic:"
                                + key.topic() + ",partition:" + key.partition()
                                + " offsets已经过时，更新为" + earliestLeaderOffset);
                        offsets.put(key, earliestLeaderOffset);
                    }
                }
                if (!offsets.isEmpty()){
                    //TODO
                    scala.collection.immutable.Map
                            topicAndPartitionLongMap = toScalaImmutableMap(offsets);
                    kafkaCluster.setConsumerOffsets(groupId, topicAndPartitionLongMap);

                }

            }else{// 没有消费过
                String offsetReset = kafkaParams.get("auto.offset.reset").get().toLowerCase();
                scala.collection.immutable.Map leaderOffsets = null;
                if ("smallest".equals(offsetReset)){
                    Either, scala.collection.immutable.Map>
                            leaderOffsetsE = kafkaCluster.getEarliestLeaderOffsets(partitions);
                    if (leaderOffsetsE.isLeft()) {
                        throw new SparkException("get earliest leader offsets failed: ${leaderOffsetsE.left.get}");
                    }
                    leaderOffsets = leaderOffsetsE.right().get();
                }else {
                    Either, scala.collection.immutable.Map>
                            latestLeaderOffsetsE = kafkaCluster.getLatestLeaderOffsets(partitions);
                    if (latestLeaderOffsetsE.isLeft()){
                        throw new SparkException("get latest leader offsets failed: ${leaderOffsetsE.left.get}");
                    }
                    leaderOffsets = latestLeaderOffsetsE.right().get();
                }
                Map
                        topicAndPartitionLeaderOffsetMap = JavaConversions.mapAsJavaMap(leaderOffsets);
                Map offsets = new HashMap<>();
                for (TopicAndPartition key: topicAndPartitionLeaderOffsetMap.keySet()){
                    KafkaCluster.LeaderOffset offset = topicAndPartitionLeaderOffsetMap.get(key);
                    long offset1 = offset.offset();
                    offsets.put(key, offset1);
                }

                //TODO
                scala.collection.immutable.Map
                        immutableOffsets = toScalaImmutableMap(offsets);
                kafkaCluster.setConsumerOffsets(groupId,immutableOffsets);
            }

        }


    }

    /**
     * 更新zookeeper上的消费offsets
     * @param rdd
     */
    public void updateZKOffsets(JavaRDD rdd){
        String groupId = kafkaParams.get("group.id").get();

        OffsetRange[] offsetRanges = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
        for (OffsetRange offset: offsetRanges){
            TopicAndPartition topicAndPartition = new TopicAndPartition(offset.topic(), offset.partition());
            Map offsets = new HashMap<>();
            offsets.put(topicAndPartition, offset.untilOffset());
            Either, scala.collection.immutable.Map>
                    o = kafkaCluster.setConsumerOffsets(groupId, toScalaImmutableMap(offsets));
            if (o.isLeft()){
                System.out.println("Error updating the offset to Kafka cluster: ${o.left.get}");
            }

        }
    }

    /**
     * java Map convert immutable.Map
     * @param javaMap
     * @param 
     * @param 
     * @return
     */
    private static  scala.collection.immutable.Map toScalaImmutableMap(java.util.Map javaMap) {
        final java.util.List> list = new java.util.ArrayList<>(javaMap.size());
        for (final java.util.Map.Entry entry : javaMap.entrySet()) {
            list.add(scala.Tuple2.apply(entry.getKey(), entry.getValue()));
        }
        final scala.collection.Seq> seq = scala.collection.JavaConverters.asScalaBufferConverter(list).asScala().toSeq();
        return (scala.collection.immutable.Map) scala.collection.immutable.Map$.MODULE$.apply(seq);
    }
}
import org.apache.spark.SparkConf;
import org.apache.spark.SparkException;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.JavaKafkaManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by weiw\ on 10/24/17.
 */
public class KafkaManagerDemo {

    public static void main(String[] args) throws SparkException, InterruptedException {

        SparkConf sparkConf = new SparkConf().setAppName(KafkaManagerDemo.class.getName());
        sparkConf.setMaster("local[3]");
        sparkConf.set("spark.streaming.kafka.maxRatePerPartition", "5");
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
        JavaStreamingContext javaStreamingContext =
                new JavaStreamingContext(javaSparkContext, Durations.seconds(5));
        javaStreamingContext.sparkContext().setLogLevel("WARN");

        String brokers = "localhost:9092";
        String topics = "finance_test2";
        String groupId = "test22";

        HashSet topcisSet = new HashSet<>();
        topcisSet.add(topics);

        Map kafkaParams = new HashMap<>();
        kafkaParams.put("metadata.broker.list", brokers);
        kafkaParams.put("group.id", groupId);
        kafkaParams.put("auto.offset.reset", "smallest");

        JavaKafkaManager javaKafkaManager = new JavaKafkaManager(kafkaParams);
        JavaInputDStream message
                = javaKafkaManager.createDirectStream(javaStreamingContext, kafkaParams, topcisSet);


        message.transform(new Function, JavaRDD>() {
            @Override
            public JavaRDD call(JavaRDD v1) throws Exception {
                return v1;
            }
        }).foreachRDD(new VoidFunction>() {
            @Override
            public void call(JavaRDD rdd) throws Exception {
                System.out.println(rdd);
                if (!rdd.isEmpty()){
                    rdd.foreach(new VoidFunction() {
                        @Override
                        public void call(String r) throws Exception {
                            System.out.println(r);
                        }
                    });

                    javaKafkaManager.updateZKOffsets(rdd);
                }
            }
        });

        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();

    }
}