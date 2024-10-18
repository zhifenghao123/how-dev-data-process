package com.howdev.flinklearn.datastream.window;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 *（1）触发器和移除器
 * 框架定义好的几个窗口，如滚动窗口、滑动窗口、会话窗口，都已经内置了触发器和移除器。一般不需要自定义实现
 *  1）触发器（Trigger）
 * 触发器主要是用来控制窗口什么时候触发计算。
 * 所谓的“触发计算”，本质上就是执行窗口函数，所以可以认为是计算得到结果并输出的过程。1
 * 基于 WindowedStream 调用.trigger0方法，就可以传入一个自定义的窗口触发器：
 *  stream.keyBy(...)
 *      .window(...)
 *      •trigger(new MyTrigger())
 *  2）移除器（Evictor)
 * 移除器主要用来定义移除某些数据的逻辑。
 * 基于WindowedStream 调用.evictor()方法，就可以传入一个自定义的移除器（Evictor）。Evictor是一个接口，不同的窗口类型都有各自预实现的移除器。1
 *  stream.keyBy(...)
 *      .window(...)
 *      .evictor(new MyEvictor())
 *
 *（2）以时间类型的滚动窗口为例，分析原理
 *  1）窗口什么时候触发输出？
 *  时间进展 >= 窗口的最大时间戳（end - 1ms）
 *  2）窗口是怎么划分的？
 *    代码： TumblingProcessingTimeWindows#assignWindows
 *             -> TimeWindow#getWindowStartWithOffset
 *             remainder = (timestamp - offset) % windowSize;
 *             (13s -0 ) % 10 = 3
 *             (27s -0 ) % 10 = 7
 *             // handle both positive and negative cases
 *             if (remainder < 0) {
 *                  return timestamp - (remainder + windowSize);
 *              } else {
 *                  return timestamp - remainder;
 *                  13 - 3 = 10
 *                  27 - 7 = 20
 *              }
 *
 *  start= 向下取整，取窗口长度的整数倍
 *  end = start + 窗口长度
 *  窗口左闭右开==》属于本窗口的 最大时间戳 = end - 1ms
 *
 *（3）窗口的生命周期？
 *  创建：属于本窗口的第一条数据来的时候，现new的，放入一个singleton单例的集合中
 *  销毁（关窗）：时间进展 >= 窗口的最大时间戳（end - 1ms）+ 允许迟到的时间（默认0）
 *
 *
 *
 *
 *
 */
public class TimeWindowLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据:1,M,20,180,1
        SingleOutputStreamOperator<OrderRecord> dataSource = env
                .socketTextStream("127.0.0.1", 9999)
                .map((MapFunction<String, OrderRecord>) value -> {
                    String[] splits = value.split(",");
                    return new OrderRecord(splits[0], splits[1], Double.valueOf(splits[2]), Long.valueOf(splits[3]));
                });

        KeyedStream<OrderRecord, String> keyedStream = dataSource.keyBy(OrderRecord::getUserId);

        // 基于时间的
        //滚动窗口，窗口长度10s
        WindowedStream<OrderRecord, String, TimeWindow> windowStream = keyedStream.window(TumblingProcessingTimeWindows.of(Time.seconds(10)));
        //滑动窗口，窗口长度10s，滑动步长5s
        //WindowedStream<OrderRecord, String, TimeWindow> windowStream = keyedStream.window(SlidingProcessingTimeWindows.of(Time.seconds(10), Time.seconds(5)));
        //会话窗口，超时间隔5s
        //WindowedStream<OrderRecord, String, TimeWindow> windowStream = keyedStream.window(ProcessingTimeSessionWindows.withGap(Time.seconds(5)));
        //会话窗口，动态超时间隔，每条数据到来，都会更新超时时间
//        WindowedStream<OrderRecord, String, TimeWindow> windowStream = keyedStream.window(ProcessingTimeSessionWindows.withDynamicGap(new SessionWindowTimeGapExtractor<OrderRecord>() {
//            @Override
//            public long extract(OrderRecord element) {
//                // 提取数据的age值作为会话超时时间
//                return element.getOrderAmount() * 1000;
//            }
//        }));




        SingleOutputStreamOperator<String> processedStream = windowStream.process(new ProcessWindowFunction<OrderRecord, String, String, TimeWindow>() {
            /**
             *
             * @param key The key for which this window is evaluated.
             * @param context The context in which the window is being evaluated.
             * @param elements The elements in the window being evaluated.
             * @param out A collector for emitting elements.
             * @throws Exception
             */
            @Override
            public void process(String key, ProcessWindowFunction<OrderRecord, String, String, TimeWindow>.Context context, Iterable<OrderRecord> elements, Collector<String> out) throws Exception {
                // 上下文中可以拿到很多信息
                long start = context.window().getStart();
                long end = context.window().getEnd();
                String formattedWindowStart = DateFormatUtils.format(start, "yyyy-MM-dd HH:mm:ss");
                String formattedWindowEnd = DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss");

                List<String> users = new ArrayList<>();
                elements.forEach(user -> {
                    users.add(user.toString());
                });
                long count = elements.spliterator().estimateSize();

                String formattedOutput =String.format("key=%s,windowStart=%s,windowEnd=%s 的窗口包含%d个元素 ===> %s", key, formattedWindowStart, formattedWindowEnd, count, users);

                out.collect(formattedOutput);

            }
        });

        processedStream.print();

        env.execute();
    }
}
