package com.howdev.flinklearn.datastream.window;

import com.howdev.common.util.JacksonUtil;
import com.howdev.flinklearn.biz.domain.LogRecord;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.AggregateFunction;
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

public class AggregateAndProcessLearn {
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
         * 增量聚合 Aggregate + 全窗口 process
         * 1、增量聚台函数处理数据：来一条计算一条
         * 2、窗口触发时，增量聚合的结果（只有一条）传递给 全窗口函数
         *3、经过全窗口函数的处理包装后，输出*
         * 结合两者的优点：
         * 1、增量聚合：来一条计算一条，存储中间的计算结果，占用的空间少
         * 2、全窗口函数：可以通过上下文 实现灵活的功能
         *
         * reduce 也一样的用法
         */
        SingleOutputStreamOperator<String> processedStream = windowStream.aggregate(new MyAggFunction(), new MyProcessWindowFunction());

        processedStream.print();

        env.execute();
    }



    public static class MyAggFunction implements AggregateFunction<LogRecord, Long, String> {
        /**
         * 创建一个累加器，这就是为聚合创建了一个初始状态，每个聚合任务只会调用一次。
         * @return
         */
        @Override
        public Long createAccumulator() {
            System.out.println("调用createAccumulator方法");
            return 0L;
        }

        /**
         * 将输入的元素添加到累加器中。
         * @param value The value to add
         * @param accumulator The accumulator to add the value to
         * @return
         */
        @Override
        public Long add(LogRecord value, Long accumulator) {
            System.out.println("调用add方法");
            return accumulator + value.getCost();
        }

        /**
         * 获取最终结果，窗口触发时输出
         * @param accumulator The accumulator of the aggregation
         * @return
         */
        @Override
        public String getResult(Long accumulator) {
            System.out.println("调用getResult方法");
            return accumulator.toString();
        }

        /**
         * 合并两个累加器，并将合并后的状态作为一个累加器返回。
         * ！！！只有会话窗口才会用到！！！
         * @param a An accumulator to merge
         * @param b Another accumulator to merge
         * @return
         */
        @Override
        public Long merge(Long a, Long b) {
            System.out.println("调用merge方法");
            return null;
        }
    }


    public static class MyProcessWindowFunction extends ProcessWindowFunction<String, String, Tuple3<String, String, String>, TimeWindow> {
        /**
         *
         * @param key The key for which this window is evaluated.
         * @param context The context in which the window is being evaluated.
         * @param elements The elements in the window being evaluated.
         * @param out A collector for emitting elements.
         * @throws Exception
         */
        @Override
        public void process(Tuple3<String, String, String> key, ProcessWindowFunction<String, String, Tuple3<String, String, String>, TimeWindow>.Context context, Iterable<String> elements, Collector<String> out) throws Exception {
            // 上下文中可以拿到很多信息
            long start = context.window().getStart();
            long end = context.window().getEnd();
            String formattedWindowStart = DateFormatUtils.format(start, "yyyy-MM-dd HH:mm:ss");
            String formattedWindowEnd = DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss");

            long count = elements.spliterator().estimateSize();

            String formattedOutput =String.format("key=%s,windowStart=%s,windowEnd=%s 的窗口包含%d个元素", key, formattedWindowStart, formattedWindowEnd, count);

            out.collect(formattedOutput);

        }
    }
}


