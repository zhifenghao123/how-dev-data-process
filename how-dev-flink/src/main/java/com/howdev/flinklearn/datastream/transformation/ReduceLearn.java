package com.howdev.flinklearn.datastream.transformation;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class ReduceLearn {
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
         * reduce：
         *  1. keyby 之后才能调用
         *  2. 输入类型等于输出类型，类型不能变
         *  3. 每个key的第一条数据来的时候，不会执行reduce方法，直接输出，存起来
         *  4. reduce()方法中的两个参数：
         *      value1:之前的计算结果，存储的状态
         *      value2:当前来的数据
         */
        KeyedStream<OrderRecord, String> keyedStream = userDataStreamSource.keyBy(OrderRecord::getUserId);

        SingleOutputStreamOperator<OrderRecord> reducedStream = keyedStream.reduce(new ReduceFunction<OrderRecord>() {
            @Override
            public OrderRecord reduce(OrderRecord value1, OrderRecord value2) throws Exception {
                System.out.println("value1=" + value1.toString());
                System.out.println("value2=" + value2.toString());

                OrderRecord orderRecord = new OrderRecord();
                orderRecord.setUserId(value1.getUserId());
                orderRecord.setProductName(value1.getProductName());
                orderRecord.setOrderAmount(value1.getOrderAmount() + value2.getOrderAmount());
                return orderRecord;
            }
        });

        reducedStream.print();

        env.execute();
    }
}
