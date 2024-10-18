package com.howdev.flinklearn.datastream.join;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import com.howdev.flinklearn.biz.domain.UserBrowsingRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class IntervalJoinLearn {
    public static void main(String[] args) throws Exception {
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        SingleOutputStreamOperator<UserBrowsingRecord> userBrowsingRecordDataStream = env.fromElements(
                        new UserBrowsingRecord("u1", "iPhone", 5000.0, 1L),
                        new UserBrowsingRecord("u1", "huawei", 3000.0, 2L),
                        new UserBrowsingRecord("u2", "xiaomi", 2000.0, 3L),
                        new UserBrowsingRecord("u3", "huawei", 2000.0, 4L),
                        new UserBrowsingRecord("u2", "huawei", 2000.0, 5L)
                )
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<UserBrowsingRecord>forMonotonousTimestamps()
                                .withTimestampAssigner((element, recordTimestamp) -> element.getBrowsingTimestamp() * 1000L)
                );


        SingleOutputStreamOperator<OrderRecord> orderRecordDataStream = env.fromElements(
                new OrderRecord("u1", "iPhone", 5000.0, 1L),
                new OrderRecord("u2", "huawei", 3000.0,  3L),
                new OrderRecord("u2", "iPhone", 5000.0, 3L),
                new OrderRecord("u1", "iPhone", 5000.0, 4L),
                new OrderRecord("u3", "huawei", 2000.0, 5L),
                new OrderRecord("u2", "iPhone", 5000.0, 6L),
                new OrderRecord("u1", "iPhone", 5000.0, 5L),
                new OrderRecord("u1", "iPhone", 5000.0, 7L)
                ).assignTimestampsAndWatermarks(
                WatermarkStrategy.<OrderRecord>forMonotonousTimestamps()
                        .withTimestampAssigner((element, recordTimestamp) -> element.getOrderTimestamp() * 1000L)
        );

        // (1)分别做keyby
        KeyedStream<UserBrowsingRecord, String> userBrowsingRecordStringKeyedStream = userBrowsingRecordDataStream.keyBy(UserBrowsingRecord::getUserId);
        KeyedStream<OrderRecord, String> orderRecordStringKeyedStream = orderRecordDataStream.keyBy(OrderRecord::getUserId);

        // (2)调用intervalJoin
        SingleOutputStreamOperator<String> intervalJoinDataStream = userBrowsingRecordStringKeyedStream.intervalJoin(orderRecordStringKeyedStream)
                .between(Time.seconds(-2), Time.seconds(2))
                .process(new ProcessJoinFunction<UserBrowsingRecord, OrderRecord, String>() {
                    // 两条流的数据匹配上，才会调用这个方法
                    @Override
                    public void processElement(UserBrowsingRecord left, OrderRecord right, ProcessJoinFunction<UserBrowsingRecord, OrderRecord, String>.Context ctx, Collector<String> out) throws Exception {
                        // 进入这个方法，是关联上的数据
                        out.collect(left.toString() + "<----------->" + right.toString());
                    }
                });

        intervalJoinDataStream.print("intervalJoinDataStream:");

        env.execute();
    }
}
