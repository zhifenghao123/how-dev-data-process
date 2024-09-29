package com.howdev.flinklearn.datastream.watermark;

import com.howdev.common.util.JacksonUtil;
import com.howdev.flinklearn.biz.domain.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
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
public class WatermarkOutOfOrdernessLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        SingleOutputStreamOperator<LogRecord> dataSource = env.socketTextStream("127.0.0.1", 9999)
                .map(line -> JacksonUtil.fromJson(line, LogRecord.class));

        /**
         * 乱序流中内置水位线设置
         * 由于乱序流中需要等待迟到数据到齐，所以必须设置一个固定量的延迟时间。
         * 这时生成水位线的时间戳，就是当前数据流中最大的时间戳减去延迟的结果，相当于把表调慢，当前时钟会滞后于数据的最大时间戳。
         * 调用 WatermarkStrategy. forBoundedOutoforderness()方法就可以实现。
         * 这个方法需要传入一个 maxOutOfOrderness 参数，表示“最大乱序程度”，它表示数据流中乱序数据时间戳的最大差值；
         * 如果我们能确定乱序程度，那么设置对应时间长度的延迟，就可以等到所有的乱序数据了
         *
         *
         *
         *
         * 内置的watermark的生成策略：
         * （1）都是周期性生成的：默认200ms
         * （2）有序流：watermark = 当前的最大事件时间 - 1ms
         * （2）乱序流：watermark = 当前的最大事件时间 - 延迟时间 - 1ms
         */
        // 定义WatermarkStrategy
        WatermarkStrategy<LogRecord> watermarkStrategy = WatermarkStrategy
                // 指定watermark生成策略，乱序的等待3秒
                .<LogRecord>forBoundedOutOfOrderness(Duration.ofSeconds(3))
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
