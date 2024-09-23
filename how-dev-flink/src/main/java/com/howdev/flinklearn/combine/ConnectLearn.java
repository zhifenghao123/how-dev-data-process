package com.howdev.flinklearn.combine;

import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;

public class ConnectLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Integer> dataStreamSource1 = env.fromElements(1, 2, 3, 4);
        DataStreamSource<Integer> dataStreamSource2 = env.fromElements(10, 20, 30, 40);
        DataStreamSource<String> dataStreamSource3 = env.fromElements("100", "200", "300", "400");

        /**
         * 使用connect方法连接两个数据流：
         * （1）一次只能连接两个流，且两个流的数据类型可以不一样
         * （2）连接后，可以调用map、flatMap、process方法，但是各自处理各自的
         *
         *
         */
        ConnectedStreams<Integer, String> connectedDataStream = dataStreamSource1.connect(dataStreamSource3);


        SingleOutputStreamOperator<String> mappedDataStream = connectedDataStream.map(new CoMapFunction<Integer, String, String>() {
            @Override
            public String map1(Integer value) throws Exception {
                return value.toString();
            }

            @Override
            public String map2(String value) throws Exception {
                return value;
            }
        });

        mappedDataStream.print();

        env.execute();

    }
}
