package com.howdev.flinklearn.datastream.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class KafkaSourceLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("127.0.0.1:9092")
                .setGroupId("flink-kafka-source-learn")
                .setTopics("flink-kafka-topoc")
                .setValueOnlyDeserializer(new SimpleStringSchema())
                /**
                 * kafka 消费者的参数,auto.reset.offset:
                 *      earliest: 如果有offset，则从offset开始消费，否则从最早开始消费
                 *      latest: 如果有offset，则从offset开始消费，否则从最新开始消费
                 *
                 * flink的KafkaSource消费策略：OffsetsInitializer，默认是earliest
                 *      earliest: 一定从最早开始消费
                 *      latest: 一定从最新开始消费
                 */
                .setStartingOffsets(OffsetsInitializer.latest())
                .build();



        DataStreamSource<String> dataStreamSource = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "KafkaSource");

        dataStreamSource.print();

        env.execute("KafkaSourceLearn");
    }
}
