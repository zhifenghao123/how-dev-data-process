package com.howdev.flinklearn.datastream.watermark;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

@Slf4j
public class WatermarkCustomizeLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        SingleOutputStreamOperator<OrderRecord> dataSource = env.socketTextStream("127.0.0.1", 9999)
                .map((MapFunction<String, OrderRecord>) value -> {
                    String[] splits = value.split(",");
                    return new OrderRecord(splits[0], splits[1], Double.valueOf(splits[2]), Long.valueOf(splits[3]));
                });

        // 默认是200毫秒
        env.getConfig().setAutoWatermarkInterval(2000);

        // 定义WatermarkStrategy
        WatermarkStrategy<OrderRecord> watermarkStrategy = WatermarkStrategy
//                .<OrderRecord>forGenerator(new WatermarkGeneratorSupplier<OrderRecord>() {
//                    @Override
//                    public WatermarkGenerator<OrderRecord> createWatermarkGenerator(Context context) {
//                        return new WatermarkGenerator<OrderRecord>() {
//                            private long maxOutOfOrderness = 3000;
//                            //private long currentMaxTimestamp = Long.MIN_VALUE + maxOutOfOrderness + 1;
//                            private long currentMaxTimestamp = Long.MIN_VALUE + maxOutOfOrderness;
//
//                            @Override
//                            public void onEvent(LogRecord event, long eventTimestamp, WatermarkOutput output) {
//                                Math.max(currentMaxTimestamp, eventTimestamp);
//                            }
//
//                            @Override
//                            public void onPeriodicEmit(WatermarkOutput output) {
//                                //output.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness -1));
//                                output.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness));
//
//                            }
//
//                        };
//                    }
//                })
                .<OrderRecord>forGenerator((WatermarkGeneratorSupplier<OrderRecord>) context -> new WatermarkGenerator<OrderRecord>() {
                    private long maxOutOfOrderness = 3000;
                    //private long currentMaxTimestamp = Long.MIN_VALUE + maxOutOfOrderness + 1;
                    private long currentMaxTimestamp = Long.MIN_VALUE + maxOutOfOrderness;

                    @Override
                    public void onEvent(OrderRecord event, long eventTimestamp, WatermarkOutput output) {
                        currentMaxTimestamp = Math.max(currentMaxTimestamp, eventTimestamp);
                        System.out.println("调用了WatermarkGenerator#onEvent()方法，获取目前为止的最大时间戳：" + currentMaxTimestamp);

                    }

                    @Override
                    public void onPeriodicEmit(WatermarkOutput output) {
                        //output.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness -1));
                        output.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness));
                        System.out.println("调用了WatermarkGenerator#onPeriodicEmit()方法，生成watermark：" + (currentMaxTimestamp - maxOutOfOrderness));

                    }

                })
                // 指定时间戳分配器，从数据中提取
                .withTimestampAssigner(new SerializableTimestampAssigner<OrderRecord>() {
                    // 返回时间戳，要毫秒
                    @Override
                    public long extractTimestamp(OrderRecord element, long recordTimestamp) {
                        log.info("数据={}, requestTimeStamp={}", element, recordTimestamp);
                        return element.getOrderTimestamp();
                    }
                });
        SingleOutputStreamOperator<OrderRecord> watermarkedDataStream = dataSource.assignTimestampsAndWatermarks(watermarkStrategy);

        KeyedStream<OrderRecord, Tuple2<String, String>> keyedStream = watermarkedDataStream.keyBy(new KeySelector<OrderRecord, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> getKey(OrderRecord value) throws Exception {
                return Tuple2.of(value.getUserId(), value.getProductName());
            }
        });

        // 基于时间的
        WindowedStream<OrderRecord, Tuple2<String, String>, TimeWindow> windowStream = keyedStream
                // 使用事件时间语义的窗口
                .window(TumblingEventTimeWindows.of(Time.seconds(10)));
        SingleOutputStreamOperator<String> processedDataStream = windowStream.process(new ProcessWindowFunction<OrderRecord, String, Tuple2<String, String>, TimeWindow>() {
            @Override
            public void process(Tuple2<String, String> key, Context context, Iterable<OrderRecord> elements, Collector<String> out) throws Exception {
                // 上下文中可以拿到很多信息
                long start = context.window().getStart();
                long end = context.window().getEnd();
                String formattedWindowStart = DateFormatUtils.format(start, "yyyy-MM-dd HH:mm:ss");
                String formattedWindowEnd = DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss");

                long count = elements.spliterator().estimateSize();

                String formattedOutput = String.format("key=%s,windowStart=%s,windowEnd=%s 的窗口包含%d个元素", key, formattedWindowStart, formattedWindowEnd, count);

                out.collect(formattedOutput);
            }
        });

        processedDataStream.print();

        env.execute();
    }
}
