package com.howdev.flinklearn.datastream.join;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import com.howdev.flinklearn.biz.domain.UserBrowsingRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.JoinFunction;
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

        /**
         * window join
         * (1)落在同一个时间窗口范围内才能匹配
         * (2)根据keyby的key，来进行匹配关联
         * (3)只能拿到匹配上的数据
         */
        DataStream<String> joinDataStream = userBrowsingRecordDataStream.join(orderRecordDataStream)
                .where(UserBrowsingRecord::getUserId)
                .equalTo(OrderRecord::getUserId)
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .apply(new JoinFunction<UserBrowsingRecord, OrderRecord, String>() {
                    @Override
                    public String join(UserBrowsingRecord first, OrderRecord second) throws Exception {
                        return first.toString() + "<----------->" + second.toString();
                    }
                });

        joinDataStream.print();
        env.execute();
    }
}
