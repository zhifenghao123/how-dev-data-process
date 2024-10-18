package com.howdev.flinklearn.datastream.transformation;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SimpleTransformationLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        DataStreamSource<OrderRecord> userDataStreamSource = env.fromElements(
                new OrderRecord("u1", "iPhone", 5000.0, 1L),
                new OrderRecord("u2", "huawei", 3000.0,  3L),
                new OrderRecord("u2", "iPhone", 5000.0, 3L),
                new OrderRecord("u1", "iPhone", 5000.0, 4L),
                new OrderRecord("u3", "huawei", 2000.0, 5L),
                new OrderRecord("u2", "iPhone", 5000.0, 6L),
                new OrderRecord("u1", "iPhone", 5000.0, 5L),
                new OrderRecord("u1", "iPhone", 5000.0, 7L)
        );

        /**
         * 简单聚合算子：
         *  1. keyby 之后才能调用
         *  2. 分组内的聚合：对同一个key的数据进行聚合
         */
        KeyedStream<OrderRecord, String> keyedStream = userDataStreamSource.keyBy(OrderRecord::getUserId);

        // 位置索引方式只适用于Tuple类型，POJO类型不适用
        //SingleOutputStreamOperator<OrderRecord> salary = keyedStream.sum("orderAmount");
        //salary.print();

        /**
         * max与maxBy方法的区别：
         *  max：只会取比较字段的最大值，其他非比较字段取第一次的值
         *  maxBy：会取比较字段的最大值，同时其他非比较字段取 比较字段为最大值的记录的对应值
         */
        //keyedStream.max("orderAmount").print();
        keyedStream.maxBy("orderAmount").print();

        env.execute();
    }
}
