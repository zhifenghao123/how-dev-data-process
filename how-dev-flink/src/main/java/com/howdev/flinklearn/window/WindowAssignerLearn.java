package com.howdev.flinklearn.window;

import com.howdev.common.util.JacksonUtil;
import com.howdev.mock.dto.LogRecord;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

public class WindowAssignerLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        SingleOutputStreamOperator<LogRecord> dataSource = env.socketTextStream("127.0.0.1", 9999)
                .map(line -> JacksonUtil.fromJson(line, LogRecord.class));

        KeyedStream<LogRecord, Tuple2<String, String>> keyedStream = dataSource.keyBy(new KeySelector<LogRecord, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> getKey(LogRecord value) throws Exception {
                return Tuple2.of(value.getService(), value.getMethod());
            }
        });


        /**
         * 按键分区（Keyed）和非按键分区（Non-Keyed）
         * 在定义窗口操作之前，首先需要确定，到底是基于按键分区（Keyed）的数据流KeyedStream 来开窗，还是直接在没有按键分区的 DataStream 上开窗。
         * 也就是说，在调用窗口算子之前，是否有 keyBy操作。
         * （1） 按键分区窗口（Keyed Windows）
         *  经过按键分区 keyBy 操作后，数据流会按照 key 被分为多条逻辑流 （logical streams），这就是 KeyedStream。
         * 基于 KeyedStream 进行窗口操作时，窗口计算会在多个并行子任务上同时执行。相同 key 的数据会被发送到同一个并行子任务，而窗口操作会基于每个 key 进行单独的处理。
         * 所以可以认为每个key 上都定义了一组窗口，各自独立地进行统计计算。
         *  在代码实现上，我们需要先对 DataStream 调用.keyBy()进行按键分区，然后再调用.window()定义窗口。
         *
         * （2） 非按键分区（Non-Keyed Windows）
         * 如果没有进行 keyBy，那么原始的 DataStream 就不会分成多条逻辑流。这时窗口逻辑只能在一个任务（task）上执行，就相当于并行度变成了1。
         * 在代码中，直接基于 DataStream调用.windowAll()定义窗口。4stream.windowAll(...)
         */

        /**
         * 1.指定窗口分配器：指定用哪一种窗口？ 时间OR计数； 滚动、滑动、会话；
         *
         */
        //dataSource.windowAll();

        // 基于时间的
        WindowedStream<LogRecord, Tuple2<String, String>, TimeWindow> windowStream = keyedStream.window(TumblingProcessingTimeWindows.of(Time.seconds(10)));//滚动窗口，窗口长度10s
        //WindowedStream<LogRecord, Tuple2<String, String>, TimeWindow> windowedStream = keyedStream.window(SlidingProcessingTimeWindows.of(Time.seconds(1), Time.seconds(10)));//滑动窗口，窗口长度1分钟，滑动步长10s
        //WindowedStream<LogRecord, Tuple2<String, String>, TimeWindow> windowedStream = keyedStream.window(ProcessingTimeSessionWindows.withGap(Time.seconds(10)));//会话窗口，超时间隔10s

        // 基于计数的
        //WindowedStream<LogRecord, Tuple2<String, String>, GlobalWindow> windowedStream = keyedStream.countWindow(5);//滚动窗口，窗口长度为5个
        //WindowedStream<LogRecord, Tuple2<String, String>, GlobalWindow> windowedStream = keyedStream.countWindow(5, 2);//滑动窗口，窗口长度为5个，滑动步长为2个
        //WindowedStream<LogRecord, Tuple2<String, String>, GlobalWindow> windowedStream = keyedStream.window(GlobalWindows.create());//全局窗口，技术窗口的底层用的就是这个，需要自定义的时候才使用
        /**
         * 2.指定窗口函数：窗口内数据的计算逻辑
         */
        /**
         * 增量聚合：来一条数据，计算一条数据；窗口出发的时候，输出计算结果。 主要有两种：reduce() 和aggregate()
         * 全窗口函数：数据来了不计算，存储起来；窗口触发的时候，计算存储的数据，并输出结果。主要有process()
         */

        SingleOutputStreamOperator<LogRecord> outputStreamOperator = windowStream.sum("cost");
        outputStreamOperator.print("window-sum:");

        env.execute();
    }
}
