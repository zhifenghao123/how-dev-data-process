package com.howdev.flinklearn.watermark;

import com.howdev.common.util.JacksonUtil;
import com.howdev.mock.dto.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

@Slf4j
public class WatermarkCustomizeLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        SingleOutputStreamOperator<LogRecord> dataSource = env.socketTextStream("127.0.0.1", 9999)
                .map(line -> JacksonUtil.fromJson(line, LogRecord.class));

        // 默认是200毫秒
        env.getConfig().setAutoWatermarkInterval(2000);

        // 定义WatermarkStrategy
        WatermarkStrategy<LogRecord> watermarkStrategy = WatermarkStrategy
//                .<LogRecord>forGenerator(new WatermarkGeneratorSupplier<LogRecord>() {
//                    @Override
//                    public WatermarkGenerator<LogRecord> createWatermarkGenerator(Context context) {
//                        return new WatermarkGenerator<LogRecord>() {
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
                .<LogRecord>forGenerator((WatermarkGeneratorSupplier<LogRecord>) context -> new WatermarkGenerator<LogRecord>() {
                    private long maxOutOfOrderness = 3000;
                    //private long currentMaxTimestamp = Long.MIN_VALUE + maxOutOfOrderness + 1;
                    private long currentMaxTimestamp = Long.MIN_VALUE + maxOutOfOrderness;

                    @Override
                    public void onEvent(LogRecord event, long eventTimestamp, WatermarkOutput output) {
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
                .withTimestampAssigner(new SerializableTimestampAssigner<LogRecord>() {
                    // 返回时间戳，要毫秒
                    @Override
                    public long extractTimestamp(LogRecord element, long recordTimestamp) {
                        log.info("数据={}, requestTimeStamp={}", element, recordTimestamp);
                        return element.getRequestTimeStamp();
                    }
                });
        SingleOutputStreamOperator<LogRecord> watermarkedDataStream = dataSource.assignTimestampsAndWatermarks(watermarkStrategy);

        KeyedStream<LogRecord, Tuple3<String, String, String>> keyedStream = watermarkedDataStream.keyBy(new KeySelector<LogRecord, Tuple3<String, String, String>>() {
            @Override
            public  Tuple3<String, String, String> getKey(LogRecord value) throws Exception {
                return Tuple3.of(value.getService(), value.getMethod(), value.getReturnCode());
            }
        });

        // 基于时间的
        WindowedStream<LogRecord, Tuple3<String, String, String>, TimeWindow> windowStream = keyedStream
                // 使用事件时间语义的窗口
                .window(TumblingEventTimeWindows.of(Time.seconds(10)));
        SingleOutputStreamOperator<String> processedDataStream = windowStream.process(new ProcessWindowFunction<LogRecord, String, Tuple3<String, String, String>, TimeWindow>() {
            @Override
            public void process(Tuple3<String, String, String> key, Context context, Iterable<LogRecord> elements, Collector<String> out) throws Exception {
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
