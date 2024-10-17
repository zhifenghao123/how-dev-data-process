package com.howdev.flinklearn.datastream.join;

import com.howdev.flinklearn.biz.bo.UserGenerator;
import com.howdev.flinklearn.biz.domain.User;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

public class WindowJoinLearn {
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
                Tuple3.of("(0, 10]", 9, 11L),
                Tuple3.of("(30, 40]", 30, 7L)
        ).assignTimestampsAndWatermarks(
                WatermarkStrategy.<Tuple3<String, Integer, Long>>forMonotonousTimestamps()
                        .withTimestampAssigner((element, recordTimestamp) -> element.f2 * 1000L)
        );

        /**
         * window join
         * (1)落在同一个时间窗口范围内才能匹配
         * (2)根据keyby的key，来进行匹配关联
         * (3)只能拿到匹配上的数据
         */
        DataStream<String> joinDataStream = userDataStream.join(tuple2DataStream)
                .where(User::getAge)
                .equalTo(tuple3 -> tuple3.f1)
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .apply(new JoinFunction<User, Tuple3<String, Integer, Long>, String>() {
                    @Override
                    public String join(User first, Tuple3<String, Integer, Long> second) throws Exception {
                        return first.toString() + "<----------->" + second.toString();
                    }
                });

        joinDataStream.print();

        env.execute();
    }
}
