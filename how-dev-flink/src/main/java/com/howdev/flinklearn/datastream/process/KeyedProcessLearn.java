package com.howdev.flinklearn.datastream.process;

import com.howdev.flinklearn.biz.domain.UserBrowsingRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

public class KeyedProcessLearn {
    public static void main(String[] args) throws Exception {
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        SingleOutputStreamOperator<UserBrowsingRecord> userDataStream = env
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


        KeyedStream<UserBrowsingRecord, String> userKeyedDataStream = userDataStream.keyBy(user -> user.getUserId());

        /**
         * 定时器：
         * 1、keyed才有
         * 2、事件时间定时器，迪过watermark来触发的watermark >= 注册的时间
         * 注意：watermark = 当前最大事件时间 - 等待时间 -1ms，因为-1ms，所以会推迟一条数据
         * 比如，5s的定时器，watermark=85-35 -Ims =4999MS， 不会触发5s
         * 3、在process中获取当前watermark，显示的是上一次的watermark
             * =》因为process还没接收到这条数据矿应生成的新watermark）
         */
        SingleOutputStreamOperator<String> processed = userKeyedDataStream.process(new KeyedProcessFunction<String, UserBrowsingRecord, String>() {
            @Override
            public void processElement(UserBrowsingRecord value, KeyedProcessFunction<String, UserBrowsingRecord, String>.Context ctx, Collector<String> out) throws Exception {
                String currentKey = ctx.getCurrentKey();
                // 数据中提取出来的事件时间
                Long timestamp = ctx.timestamp();
                // 当前时间进展：处理时间-->当前系统时间，事件时间--->当前watermark
                Long processingTime = ctx.timerService().currentProcessingTime();
                // 当前水位线
                Long watermark = ctx.timerService().currentWatermark();
                System.out.println("currentKey:" + currentKey + "当前数据的事件时间是：" + timestamp + "当前数据的处理时间是：" + processingTime + "当前水位线是：" + watermark);
                // 定时器
                TimerService timerService = ctx.timerService();
                // 注册定时器:事件时间
//                timerService.registerEventTimeTimer(5000L);
//                System.out.println("currentKey:" + currentKey + "当前数据的事件时间是：" + timestamp + "注册了一个5s后的定时器");
                // 删除定时器:时间时间
                //timerService.deleteEventTimeTimer(5000L);
                // 注册定时器:处理时间
                //timerService.registerProcessingTimeTimer(5000L);
                //System.out.println("currentKey:" + currentKey + "当前数据的处理时间是：" + processingTime + "注册了一个5s后的定时器");
                // 删除定时器:处理时间
                //timerService.deleteProcessingTimeTimer(timestamp);
            }

            // 时间进展到定时器注册的时间，触发定时器执行该方法
            @Override
            public void onTimer(long timestamp, KeyedProcessFunction<String, UserBrowsingRecord, String>.OnTimerContext ctx, Collector<String> out) throws Exception {
                super.onTimer(timestamp, ctx, out);
                String currentKey = ctx.getCurrentKey();
                System.out.println("currentKey" + currentKey + "现在时间是：" + timestamp + "，定时器触发了");
            }
        });

        processed.print();

        env.execute("KeyedProcessLearn");
    }
}
