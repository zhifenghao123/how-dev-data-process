package com.howdev.flinklearn.datastream.combine;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class UnionLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Integer> dataStreamSource1 = env.fromElements(1, 2, 3, 4);
        DataStreamSource<Integer> dataStreamSource2 = env.fromElements(10, 20, 30, 40);
        DataStreamSource<String> dataStreamSource3 = env.fromElements("100", "200", "300", "400");

        //DataStream<Integer> unionedDataStream = dataStreamSource1.union(dataStreamSource2).union(dataStreamSource3.map(Integer::parseInt));
        DataStream<Integer> unionedDataStream = dataStreamSource1.union(dataStreamSource2, dataStreamSource3.map(Integer::parseInt));

        unionedDataStream.print();

        env.execute();

    }
}
