package com.howdev.flinklearn.datastream.transformation;

import com.howdev.flinklearn.biz.domain.User;
import com.howdev.flinklearn.biz.bo.UserGenerator;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class ReduceLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        DataStreamSource<User> userDataStreamSource = env.fromElements(
                UserGenerator.generate("male", 20, 1L),
                UserGenerator.generate("female", 25, 2L),
                UserGenerator.generate("male", 22, 3L),
                UserGenerator.generate("female", 35, 4L),
                UserGenerator.generate("male", 30, 5L)
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
        KeyedStream<User, String> keyedStream = userDataStreamSource.keyBy(User::getGender);

        SingleOutputStreamOperator<User> reducedStream = keyedStream.reduce(new ReduceFunction<User>() {
            @Override
            public User reduce(User value1, User value2) throws Exception {
                System.out.println("value1=" + value1.toString());
                System.out.println("value2=" + value2.toString());

                User user = new User();
                user.setGender(value1.getGender());
                user.setAge(value1.getAge());
                return user;
            }
        });

        reducedStream.print();

        env.execute();
    }
}
