package com.howdev.flinklearn.datastream.window;

import com.howdev.common.util.JacksonUtil;
import com.howdev.mock.dto.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

@Slf4j
public class ReduceLearn {
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
        WindowedStream<LogRecord, Tuple3<String, String, String>, TimeWindow> windowStream = keyedStream.window(TumblingProcessingTimeWindows.of(Time.seconds(10)));//滚动窗口，窗口长度10s

        /**
         *
         * 窗口的reduce：
         *  1.相同key的第一条数据来的时候，不会调用reduce方法
         *  2.增量聚合：来一条数据，就会计算一次，但是不会输出
         *  3.在窗口触发的时候，才会输出窗口内最终计算结果
         */
        SingleOutputStreamOperator<LogRecord> reducedStream = windowStream.reduce(new ReduceFunction<LogRecord>() {
            @Override
            public LogRecord reduce(LogRecord value1, LogRecord value2) throws Exception {
                log.info("调用reduce方法, value1:{},value2:{}", value1, value2);
                LogRecord logRecord = new LogRecord();
                logRecord.setService(value1.getService());
                logRecord.setMethod(value1.getMethod());
                logRecord.setReturnCode(value1.getReturnCode());
                logRecord.setCost(value1.getCost() + value2.getCost());
                return logRecord;
            }
        });

        reducedStream.print();

        env.execute();
    }
}
