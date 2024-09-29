package com.howdev.flinklearn.datastream.transformation;

import com.howdev.flinklearn.datastream.dto.User;
import com.howdev.flinklearn.datastream.dto.UserGenerator;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SimpleTransformationLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        DataStreamSource<User> userDataStreamSource = env.fromElements(
                UserGenerator.generate(1L, "male", 20, 1000.0),
                UserGenerator.generate(2L, "female", 25, 2100.0),
                UserGenerator.generate(3L, "male", 22, 1200.0),
                UserGenerator.generate(4L, "female", 35, 2500.0),
                UserGenerator.generate(5L, "male", 30, 3000.0)
        );

        /**
         * 简单聚合算子：
         *  1. keyby 之后才能调用
         *  2. 分组内的聚合：对同一个key的数据进行聚合
         */
        KeyedStream<User, String> keyedStream = userDataStreamSource.keyBy(User::getGender);

        // 位置索引方式只适用于Tuple类型，POJO类型不适用
        //SingleOutputStreamOperator<User> salary = keyedStream.sum("salary");
        //salary.print();

        /**
         * max与maxBy方法的区别：
         *  max：只会取比较字段的最大值，其他非比较字段取第一次的值
         *  maxBy：会取比较字段的最大值，同时其他非比较字段取 比较字段为最大值的记录的对应值
         */
        //keyedStream.max("salary").print();
        keyedStream.maxBy("salary").print();

        env.execute();
    }
}
