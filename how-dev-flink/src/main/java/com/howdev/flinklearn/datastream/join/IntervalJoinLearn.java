package com.howdev.flinklearn.datastream.join;

import com.howdev.flinklearn.biz.bo.UserGenerator;
import com.howdev.flinklearn.biz.domain.User;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class IntervalJoinLearn {
    public static void main(String[] args) throws Exception {
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        SingleOutputStreamOperator<User> userDataStream = env.fromElements(
                        UserGenerator.generate("M", 9, 1L),
                        UserGenerator.generate("F", 25, 2L),
                        UserGenerator.generate("M", 22, 3L),
                        UserGenerator.generate("F", 35, 4L),
                        UserGenerator.generate("M", 30, 5L)
                )
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<User>forMonotonousTimestamps()
                                .withTimestampAssigner((element, recordTimestamp) -> element.getRegisterTimeStamp() * 1000L));


        SingleOutputStreamOperator<Tuple3<String, Integer, Long>> tuple2DataStream = env.fromElements(
                Tuple3.of("(0, 10]", 9, 1L),
                Tuple3.of("(10, 20]", 25, 1L),
                Tuple3.of("(20, 30]", 25, 2L),
                Tuple3.of("(20, 30]", 22, 3L),
                Tuple3.of("(30, 40]", 35, 4L),
                Tuple3.of("(0, 10]", 9, 10L),
                Tuple3.of("(0, 10]", 9, 3L),
                Tuple3.of("(30, 40]", 30, 8L)
        ).assignTimestampsAndWatermarks(
                WatermarkStrategy.<Tuple3<String, Integer, Long>>forMonotonousTimestamps()
                        .withTimestampAssigner((element, recordTimestamp) -> element.f2 * 1000L)
        );

        // (1)分别做keyby
        KeyedStream<User, Integer> userKeyedStream = userDataStream.keyBy(User::getAge);
        KeyedStream<Tuple3<String, Integer, Long>, Integer> tuple2KeyedStream = tuple2DataStream.keyBy(tuple3 -> tuple3.f1);
        // (2)调用intervalJoin
        SingleOutputStreamOperator<String> intervalJoinDataStream = userKeyedStream.intervalJoin(tuple2KeyedStream)
                .between(Time.seconds(-2), Time.seconds(2))
                .process(new ProcessJoinFunction<User, Tuple3<String, Integer, Long>, String>() {
                    // 两条流的数据匹配上，才会调用这个方法
                    @Override
                    public void processElement(User left, Tuple3<String, Integer, Long> right, ProcessJoinFunction<User, Tuple3<String, Integer, Long>, String>.Context ctx, Collector<String> out) throws Exception {
                        // 进入这个方法，是关联上的数据
                        out.collect(left.toString() + "<----------->" + right.toString());
                    }
                });

        intervalJoinDataStream.print();

        env.execute();
    }
}
