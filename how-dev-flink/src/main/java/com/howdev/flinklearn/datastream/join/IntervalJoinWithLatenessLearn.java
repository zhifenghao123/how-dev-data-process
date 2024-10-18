package com.howdev.flinklearn.datastream.join;

import com.howdev.flinklearn.biz.domain.UserBrowsingRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

public class IntervalJoinWithLatenessLearn {
    public static void main(String[] args) throws Exception {
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        SingleOutputStreamOperator<UserBrowsingRecord> userBrowsingRecordDataStream = env
                .socketTextStream("127.0.0.1", 9999)
                .map(new MapFunction<String, UserBrowsingRecord>() {
                    @Override
                    public UserBrowsingRecord map(String value) throws Exception {
                        String[] split = value.split(",");
                        return new UserBrowsingRecord(split[0], split[1], Double.valueOf(split[2]), Long.valueOf(split[3]));
                    }
                })
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<UserBrowsingRecord>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, recordTimestamp) -> element.getBrowsingTimestamp() * 1000L));


        SingleOutputStreamOperator<Tuple3<String, Integer, Long>> tuple3DataStream = env
                .socketTextStream("127.0.0.1", 8888)
                .map(new MapFunction<String, Tuple3<String, Integer, Long>>() {
                    @Override
                    public Tuple3<String, Integer, Long> map(String value) throws Exception {
                        String[] split = value.split(",");
                        return Tuple3.of(split[0], Integer.valueOf(split[1]), Long.valueOf(split[2]));
                    }
                })
                .assignTimestampsAndWatermarks(
                    WatermarkStrategy.<Tuple3<String, Integer, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                        .withTimestampAssigner((element, recordTimestamp) -> element.f2 * 1000L)
                );

        // (1)分别做keyby
        KeyedStream<UserBrowsingRecord, String> userKeyedStream = userBrowsingRecordDataStream.keyBy(UserBrowsingRecord::getUserId);
        KeyedStream<Tuple3<String, Integer, Long>, String> tuple2KeyedStream = tuple3DataStream.keyBy(tuple3 -> tuple3.f0);

        OutputTag<UserBrowsingRecord> userLateDataOutputTag = new OutputTag<>("9999-late", Types.POJO(UserBrowsingRecord.class));
        OutputTag<Tuple3<String, Integer, Long>> tupleLateDataOutputTag = new OutputTag<>("8888-late", Types.TUPLE(Types.STRING, Types.INT, Types.LONG));

        // (2)调用intervalJoin
        /**
         * TODO Interval join
         * （1）只支持事件时间
         * （2）指定上界、下界的偏移，负号代表时间往前，正号代表时间往后
         * （3）process中，只能处理 join上的数据
         * （4）两条流关联后的watermark，以两条流中最小的为准
         * （5）如果 当前数据的事件时间<当前的watermark，就是迟到数据，主流的process 不处理
         *      => between后，可以指定将 左流 或 右流 的迟到数据 放入侧输出流
         */
        SingleOutputStreamOperator<String> intervalJoinDataStream = userKeyedStream.intervalJoin(tuple2KeyedStream)
                .between(Time.seconds(-2), Time.seconds(2))
                .sideOutputLeftLateData(userLateDataOutputTag)
                .sideOutputRightLateData(tupleLateDataOutputTag)
                .process(new ProcessJoinFunction<UserBrowsingRecord, Tuple3<String, Integer, Long>, String>() {
                    // 两条流的数据匹配上，才会调用这个方法
                    @Override
                    public void processElement(UserBrowsingRecord left, Tuple3<String, Integer, Long> right, ProcessJoinFunction<UserBrowsingRecord, Tuple3<String, Integer, Long>, String>.Context ctx, Collector<String> out) throws Exception {
                        // 进入这个方法，是关联上的数据
                        out.collect(left.toString() + "<----------->" + right.toString());
                    }
                });

        intervalJoinDataStream.print("主流：");
        intervalJoinDataStream.getSideOutput(userLateDataOutputTag).print("左流迟到数据：");
        intervalJoinDataStream.getSideOutput(tupleLateDataOutputTag).print("右流迟到数据：");

        env.execute();
    }
}
