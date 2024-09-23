package com.howdev.flinklearn.split;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FilterSplitFlowLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(2);
        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        DataStreamSource<String> dataStreamSource = env.socketTextStream("127.0.0.1", 9999);

        /**
         * 使用filter实现分流
         * 缺点：同一个数据，要被分别多次处理，性能不好。（不推荐这种方式）
         */
        SingleOutputStreamOperator<String> evenDataStream = dataStreamSource.filter(value -> Integer.parseInt(value) % 2 == 0);
        SingleOutputStreamOperator<String> oddDataStream = dataStreamSource.filter(value -> Integer.parseInt(value) % 2 == 1);

        evenDataStream.print("偶数流");
        oddDataStream.print("奇数流");

        env.execute();
    }
}
