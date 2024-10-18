package com.howdev.flinklearn.datastream.watermark;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WatermarkAllowLatenessLearn {
    public static void main(String[] args) throws Exception {
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);


        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        SingleOutputStreamOperator<OrderRecord> dataSource = env
                .socketTextStream("127.0.0.1", 9999)
                .map((MapFunction<String, OrderRecord>) value -> {
                    String[] splits = value.split(",");
                    return new OrderRecord(splits[0], splits[1], Double.valueOf(splits[2]), Long.valueOf(splits[3]));
                });

        // 定义WatermarkStrategy
        WatermarkStrategy<OrderRecord> watermarkStrategy = WatermarkStrategy
                // 指定watermark生成策略，乱序的等待3秒
                .<OrderRecord>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                // 指定时间戳分配器，从数据中提取
                .withTimestampAssigner(new SerializableTimestampAssigner<OrderRecord>() {
                    // 返回时间戳，要毫秒
                    @Override
                    public long extractTimestamp(OrderRecord element, long recordTimestamp) {
                        //log.info("数据={}, requestTimeStamp={}", element, recordTimestamp);
                        return element.getOrderTimestamp() * 1000;
                    }
                })
                .withIdleness(Duration.ofSeconds(2)); //空闲等待2秒

        SingleOutputStreamOperator<OrderRecord> watermarkedDataStream = dataSource
                .assignTimestampsAndWatermarks(watermarkStrategy);

        KeyedStream<OrderRecord, Tuple1<String>> keyedStream = watermarkedDataStream.keyBy(new KeySelector<OrderRecord, Tuple1<String>>() {
            @Override
            public  Tuple1<String> getKey(OrderRecord value) throws Exception {
                return Tuple1.of(value.getUserId());
            }
        });

        OutputTag<OrderRecord> latenessDataTag = new OutputTag<>("LATENESS_DATA", Types.POJO(OrderRecord.class));
        // 基于时间的
        WindowedStream<OrderRecord, Tuple1<String>, TimeWindow> windowStream = keyedStream
                // 使用事件时间语义的窗口
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                // 推迟2秒关窗，允许晚到2秒的数据
                .allowedLateness(Time.seconds(2))
                // 关窗后的迟到数据，放入侧输出流
                .sideOutputLateData(latenessDataTag);
        /**
         *
         *  1、乱序与迟到的区别
         *  乱序：数据的顺序乱了，时间小的比时间大的晚来
         *  迟到：数据的时间戳<当前的watermark
         * 2、乱序、迟到数据的处理
         *  1） watermark中指定 乱序等待时间
         *  2） 如果开窗，设置窗口允许迟到
         *   =》推迟关窗时间，在关窗之前，迟到数据来了，还能被窗口计算，来一条迟到数据触发一次计算
         *   =》关窗后，迟到数据不会被计算
         *  3） 关窗后的迟到数据，放入侧输出流
         *  如果 watermark等待3s，窗口允许迟到2s，为什么不直接 watermark 等待5s 或者 窗口允许迟到5s？
         *  =》 watermark等待时间不会设太大 ===》影响的计算延迟
         *  如果3s ==》窗口第一次触发计算和输出，135的数据来。13-3=10s
         *  如果5s ==》窗口第一次触发计算和输出，15s的数据来。 15-5=10s
         * =》 窗口允许迟到，是矿 大部分迟到数据的处理，尽量让结果准确
         * 如果只设置 允许迟到，那么 就会导致 频繁 重新输出
         *
         * TODO 改置经验
         * 1、watermark等待时间，设置一个不算特别大的，一般是秘级，在 乱序和 延迟 取舍
         * 2、设置一定的窗口允许迟到，只考虑大部分的迟到数据，极端小部分迟到很久的数据，不管
         * 3、极端小部分迟到很久的数据，放到侧输出流。获取到之后可以做各种处理
         */

        SingleOutputStreamOperator<String> processedDataStream = windowStream.process(new ProcessWindowFunction<OrderRecord, String, Tuple1<String>, TimeWindow>() {
            @Override
            public void process(Tuple1<String> key, Context context, Iterable<OrderRecord> elements, Collector<String> out) throws Exception {
                // 上下文中可以拿到很多信息
                long start = context.window().getStart();
                long end = context.window().getEnd();
                String formattedWindowStart = DateFormatUtils.format(start, "yyyy-MM-dd HH:mm:ss");
                String formattedWindowEnd = DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss");

                long count = elements.spliterator().estimateSize();

                List<String> users = new ArrayList<>();
                elements.forEach(user -> {
                    users.add(user.toString());
                });

                String formattedOutput =String.format("key=%s,windowStart=%s,windowEnd=%s 的窗口包含%d个元素 ===> %s", key, formattedWindowStart, formattedWindowEnd, count, users);

                out.collect(formattedOutput);
            }
        });

        processedDataStream.print();

        processedDataStream.getSideOutput(latenessDataTag).printToErr("LATENESS_DATA:");

        env.execute();
    }
}
