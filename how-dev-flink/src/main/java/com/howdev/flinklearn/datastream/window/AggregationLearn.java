package com.howdev.flinklearn.datastream.window;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
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
public class AggregationLearn {
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
        WindowedStream<OrderRecord, Tuple2<String, String>, TimeWindow> windowStream = keyedStream.window(TumblingProcessingTimeWindows.of(Time.seconds(20)));//滚动窗口，窗口长度10s

        SingleOutputStreamOperator<String> aggregatedStream = windowStream.aggregate(new AggregateFunction<OrderRecord, Double, String>() {
            /**
             * 创建一个累加器，这就是为聚合创建了一个初始状态，每个聚合任务只会调用一次。
             * @return
             */
            @Override
            public Double createAccumulator() {
                System.out.println("调用createAccumulator方法");
                return 0D;
            }

            /**
             * 将输入的元素添加到累加器中。
             * @param value The value to add
             * @param accumulator The accumulator to add the value to
             * @return
             */
            @Override
            public Double add(OrderRecord value, Double accumulator) {
                System.out.println("调用add方法");
                return accumulator + value.getOrderAmount();
            }

            /**
             * 获取最终结果，窗口触发时输出
             * @param accumulator The accumulator of the aggregation
             * @return
             */
            @Override
            public String getResult(Double accumulator) {
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
            public Double merge(Double a, Double b) {
                System.out.println("调用merge方法");
                return null;
            }
        });

        aggregatedStream.print();

        env.execute();
    }
}
