package com.howdev.flinklearn.datastream.watermark;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WatermarkOutOfOrdernessAndAllowWindowLatenessAndProcessWindowLearn {
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
                        log.info("数据={}, requestTimeStamp={}", element, recordTimestamp);
                        return element.getOrderTimestamp()*1000;
                    }
                })
                .withIdleness(Duration.ofSeconds(2)); //空闲等待2秒

        SingleOutputStreamOperator<OrderRecord> watermarkedDataStream = dataSource
                .assignTimestampsAndWatermarks(watermarkStrategy);

        KeyedStream<OrderRecord, Tuple2<String, String>> keyedStream = watermarkedDataStream
                .keyBy(new KeySelector<OrderRecord, Tuple2<String, String>>() {
                    @Override
                    public Tuple2<String, String> getKey(OrderRecord value) {
                        return new Tuple2<>(value.getUserId(), value.getProductName());
                    }
                });

        WindowedStream<OrderRecord, Tuple2<String, String>, TimeWindow> windowWindowedStream = keyedStream
                // 使用事件时间语义的窗口
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                // 推迟2秒关窗，允许晚到2秒的数据
                // TODO: 尤其注意这里会对触发ProcessWindowFunction的执行有着影响，具体为：
                // 当水位达到窗口结束时间是时会触发执行一次；然后只要有一条延迟数据在允许延迟的时间内，且数据的水位线位于窗口结束时间前，则会触发一次ProcessWindowFunction
                .allowedLateness(Time.seconds(2));

        SingleOutputStreamOperator<String> aggregatedDataStream = windowWindowedStream.aggregate(new MyAggFunction(), new MyProcessWindowFunction());
        aggregatedDataStream.print("最终结果：");

        env.execute();
    }

    public static class MyAggFunction implements AggregateFunction<OrderRecord, OrderAmountStatistic, OrderAmountStatistic> {
        /**
         * 创建一个累加器，这就是为聚合创建了一个初始状态，每个聚合任务只会调用一次。
         *
         * @return
         */
        @Override
        public OrderAmountStatistic createAccumulator() {
            System.out.println("调用createAccumulator方法");
            OrderAmountStatistic orderAmountStatistic = new OrderAmountStatistic();
            orderAmountStatistic.orderTimestamps = new ArrayList<>();
            orderAmountStatistic.orderAmounts = new ArrayList<>();
            orderAmountStatistic.totalAmount = 0d;
            return orderAmountStatistic;
        }

        /**
         * 将输入的元素添加到累加器中。
         *
         * @param value       The value to add
         * @param accumulator The accumulator to add the value to
         * @return
         */
        @Override
        public OrderAmountStatistic add(OrderRecord value, OrderAmountStatistic accumulator) {
            System.out.printf("调用add方法（触发本次执行的数据：%s）%n", value.toString());
            accumulator.orderTimestamps.add(value.getOrderTimestamp());
            accumulator.orderAmounts.add(value.getOrderAmount());
            accumulator.totalAmount += value.getOrderAmount();
            return accumulator;
        }

        /**
         * 获取最终结果，窗口触发时输出
         *
         * @param accumulator The accumulator of the aggregation
         * @return
         */
        @Override
        public OrderAmountStatistic getResult(OrderAmountStatistic accumulator) {
            System.out.println("调用getResult方法");
            return accumulator;
        }

        /**
         * 合并两个累加器，并将合并后的状态作为一个累加器返回。
         * ！！！只有会话窗口才会用到！！！
         *
         * @param a An accumulator to merge
         * @param b Another accumulator to merge
         * @return
         */
        @Override
        public OrderAmountStatistic merge(OrderAmountStatistic a, OrderAmountStatistic b) {
            System.out.println("调用merge方法");
            return null;
        }
    }


    public static class MyProcessWindowFunction extends ProcessWindowFunction<OrderAmountStatistic, String, Tuple2<String, String>, TimeWindow> {
        /**
         * @param key      The key for which this window is evaluated.
         * @param context  The context in which the window is being evaluated.
         * @param elements The elements in the window being evaluated.
         * @param out      A collector for emitting elements.
         * @throws Exception
         */
        @Override
        public void process(Tuple2<String, String> key, ProcessWindowFunction<OrderAmountStatistic, String, Tuple2<String, String>, TimeWindow>.Context context, Iterable<OrderAmountStatistic> elements, Collector<String> out) throws Exception {
            // 上下文中可以拿到很多信息
            long start = context.window().getStart();
            long end = context.window().getEnd();
            String formattedWindowStart = DateFormatUtils.format(start, "yyyy-MM-dd HH:mm:ss");
            String formattedWindowEnd = DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss");


            long currentWatermark = context.currentWatermark();
            String formattedCurrentWatermark = DateFormatUtils.format(currentWatermark, "yyyy-MM-dd HH:mm:ss");

            long count = elements.spliterator().estimateSize();
            OrderAmountStatistic processResult = elements.iterator().next();

            String formattedOutput = String.format("key=%s,windowStart=%s,windowEnd=%s 的窗口包含%d个元素. \n" +
                    "窗口计算结果：%s.\n" + "当前水位线=%d(%s)"
                    , key, formattedWindowStart, formattedWindowEnd, count, processResult.toString(), currentWatermark, formattedCurrentWatermark);

            out.collect(formattedOutput);

        }
    }

    @Data
    static class OrderAmountStatistic {
        List<Long> orderTimestamps;
        List<Double> orderAmounts;
        Double totalAmount;


    }
}
