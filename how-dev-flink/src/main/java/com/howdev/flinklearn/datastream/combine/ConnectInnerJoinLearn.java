package com.howdev.flinklearn.datastream.combine;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectInnerJoinLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //env.setParallelism(1);

        DataStreamSource<Tuple2<Integer, String>> dataStreamSource1 = env.fromElements(
                Tuple2.of(1, "hhh"),
                Tuple2.of(1, "hh2"),
                Tuple2.of(2, "aaa"),
                Tuple2.of(3, "ooo"));

        DataStreamSource<Tuple3<Integer, String, Integer>> dataStreamSource2 = env.fromElements(
                Tuple3.of(1, "hhh", 10),
                Tuple3.of(1, "hhh", 15),
                Tuple3.of(2, "aaa", 20),
                Tuple3.of(3, "ooo", 30));

        // 连接两个流
        //ConnectedStreams<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>> connectedStreams = dataStreamSource1.connect(dataStreamSource2);
        // 多并行度下，需要根据关联条件keyby，才能保证key相同的数据到一起去
        ConnectedStreams<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>> connectedStreams = dataStreamSource1.connect(dataStreamSource2)
                .keyBy(data1 -> data1.f0, data2 -> data2.f0);

        /**
         * 实现相互匹配的效果：
         * （1）两条流，哪个流的数据先来，不确定
         * （2）每条流有数据来，就存到一个变量中
         * （3）每条流有数据来的时候，除了存变量中，不知道对方是否有匹配的数据，所以要去另一条流存的变量中查找是否有匹配的数据
         */
        SingleOutputStreamOperator<String> processedDataStream = connectedStreams.process(new CoProcessFunction<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>, String>() {
            Map<Integer, List<Tuple2<Integer, String>>> cache1 = new HashMap<>();
            Map<Integer, List<Tuple3<Integer, String, Integer>>> cache2 = new HashMap<>();

            @Override
            public void processElement1(Tuple2<Integer, String> value, CoProcessFunction<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>, String>.Context ctx, Collector<String> out) throws Exception {
                Integer id = value.f0;
                // 数据来了，就先将数据存储到cache1中
                if (cache1.containsKey(id)) {
                    cache1.get(id).add(value);
                } else {
                    List<Tuple2<Integer, String>> list = new ArrayList<>();
                    list.add(value);
                    cache1.put(id, list);
                }

                // 再去cache2中查找是否有匹配的数据,如果有就存到out中
                if (cache2.containsKey(id)) {
                    List<Tuple3<Integer, String, Integer>> list = cache2.get(id);
                    for (Tuple3<Integer, String, Integer> tuple3 : list) {
                        out.collect("s1:" + value + "<-------->" + "s2:" + tuple3);
                    }
                }
            }

            @Override
            public void processElement2(Tuple3<Integer, String, Integer> value, CoProcessFunction<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>, String>.Context ctx, Collector<String> out) throws Exception {
                Integer id = value.f0;
                // 数据来了，就先将数据存储到cache2中
                if (cache2.containsKey(id)) {
                    cache2.get(id).add(value);
                } else {
                    List<Tuple3<Integer, String, Integer>> list = new ArrayList<>();
                    list.add(value);
                    cache2.put(id, list);
                }

                // 再去cache1中查找是否有匹配的数据,如果有就存到out中
                if (cache1.containsKey(id)) {
                    List<Tuple2<Integer, String>> list = cache1.get(id);
                    for (Tuple2<Integer, String> tuple2 : list) {
                        out.collect("s1:" + tuple2 + "<-------->" + "s2:" + value);
                    }
                }
            }
        });

        processedDataStream.print();

        env.execute();
    }
}
