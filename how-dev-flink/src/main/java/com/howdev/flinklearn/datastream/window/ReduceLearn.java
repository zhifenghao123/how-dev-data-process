package com.howdev.flinklearn.datastream.window;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
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
        SingleOutputStreamOperator<OrderRecord> dataSource = env
                .socketTextStream("127.0.0.1", 9999)
                .map((MapFunction<String, OrderRecord>) value -> {
                    String[] splits = value.split(",");
                    return new OrderRecord(splits[0], splits[1], Double.valueOf(splits[2]), Long.valueOf(splits[3]));
                });

        KeyedStream<OrderRecord, Tuple2<String, String>> keyedStream = dataSource.keyBy(new KeySelector<OrderRecord, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> getKey(OrderRecord value) throws Exception {
                return Tuple2.of(value.getUserId(), value.getProductName());
            }
        });



        // 基于时间的
        WindowedStream<OrderRecord, Tuple2<String, String>, TimeWindow> windowStream = keyedStream.window(TumblingProcessingTimeWindows.of(Time.seconds(10)));//滚动窗口，窗口长度10s

        /**
         *
         * 窗口的reduce：
         *  1.相同key的第一条数据来的时候，不会调用reduce方法
         *  2.增量聚合：来一条数据，就会计算一次，但是不会输出
         *  3.在窗口触发的时候，才会输出窗口内最终计算结果
         */
        SingleOutputStreamOperator<OrderRecord> reducedStream = windowStream.reduce(new ReduceFunction<OrderRecord>() {
            @Override
            public OrderRecord reduce(OrderRecord value1, OrderRecord value2) throws Exception {
                log.info("调用reduce方法, value1:{},value2:{}", value1, value2);
                OrderRecord logRecord = new OrderRecord();
                logRecord.setUserId(value1.getUserId());
                logRecord.setProductName(value1.getProductName());
                logRecord.setOrderAmount(value1.getOrderAmount() + value2.getOrderAmount());
                return logRecord;
            }
        });

        reducedStream.print();

        env.execute();
    }
}
