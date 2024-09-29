package com.howdev.flinklearn.datastream.source;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class CollectionSourceLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // DataStreamSource<String> dataStreamSource = env.fromCollection(Arrays.asList("hello", "world", "hello", "flink"));
        DataStreamSource<String> dataStreamSource = env.fromElements("hello", "world", "hello", "flink");


        dataStreamSource.print();

        env.execute();

    }
}
