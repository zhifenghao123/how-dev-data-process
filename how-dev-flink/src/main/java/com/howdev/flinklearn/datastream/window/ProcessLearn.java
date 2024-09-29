package com.howdev.flinklearn.datastream.window;

import com.howdev.common.util.JacksonUtil;
import com.howdev.flinklearn.biz.domain.LogRecord;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class ProcessLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        SingleOutputStreamOperator<LogRecord> dataSource = env.socketTextStream("127.0.0.1", 9999)
                .map(line -> JacksonUtil.fromJson(line, LogRecord.class));

        KeyedStream<LogRecord, Tuple3<String, String, String>> keyedStream = dataSource.keyBy(new KeySelector<LogRecord, Tuple3<String, String, String>>() {
            @Override
            public Tuple3<String, String, String> getKey(LogRecord value) throws Exception {
                return Tuple3.of(value.getService(), value.getMethod(), value.getReturnCode());
            }
        });



        // 基于时间的
        WindowedStream<LogRecord, Tuple3<String, String, String>, TimeWindow> windowStream = keyedStream.window(TumblingProcessingTimeWindows.of(Time.seconds(20)));//滚动窗口，窗口长度10s

        /**
         * 全窗口函数：需要先收集窗口中的数据，并在内部缓存起来，等到窗口要结束的时候，再取出数据计算
         */

        // WindowFunction是老版本的通用窗口函数，我们可以基于WindowedStream调用apply方法，传入一个WindowFunction实现类，
        // 不过 WindowFunction 能提供的上下文信息较少，也没有更高级的功能。事实上，它的作用可以被 ProcessWindowFunction 全覆盖，所以之后可能会逐渐弃用。
//        SingleOutputStreamOperator<String> aggregatedStream = windowStream
//                .apply(new WindowFunction<LogRecord, String, Tuple3<String, String, String>, TimeWindow>() {
//                    /**
//                     *
//                     * @param key The key for which this window is evaluated.
//                     * @param window The window that is being evaluated.
//                     * @param input The elements in the window being evaluated.
//                     * @param out A collector for emitting elements.
//                     * @throws Exception
//                     */
//                    @Override
//                    public void apply(Tuple3<String, String, String> key, TimeWindow window, Iterable<LogRecord> input, Collector<String> out) throws Exception {
//
//                    }
//        });

        SingleOutputStreamOperator<String> processedStream = windowStream.process(new ProcessWindowFunction<LogRecord, String, Tuple3<String, String, String>, TimeWindow>() {
            /**
             *
             * @param key The key for which this window is evaluated.
             * @param context The context in which the window is being evaluated.
             * @param elements The elements in the window being evaluated.
             * @param out A collector for emitting elements.
             * @throws Exception
             */
            @Override
            public void process(Tuple3<String, String, String> key, ProcessWindowFunction<LogRecord, String, Tuple3<String, String, String>, TimeWindow>.Context context, Iterable<LogRecord> elements, Collector<String> out) throws Exception {
                // 上下文中可以拿到很多信息
                long start = context.window().getStart();
                long end = context.window().getEnd();
                String formattedWindowStart = DateFormatUtils.format(start, "yyyy-MM-dd HH:mm:ss");
                String formattedWindowEnd = DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss");

                long count = elements.spliterator().estimateSize();

                String formattedOutput =String.format("key=%s,windowStart=%s,windowEnd=%s 的窗口包含%d个元素", key, formattedWindowStart, formattedWindowEnd, count);

                out.collect(formattedOutput);

            }
        });

        processedStream.print();

        env.execute();
    }
}
