package com.howdev.flinkdev.log.main;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

@Slf4j
public class Test {
    public static void main(String[] args) throws Exception {

        // 创建流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建数据源
        DataStream<Tuple2<String, Integer>> dataStream = env.fromElements(
                new Tuple2<>("1000", 1),
                new Tuple2<>("1000", 1),
                new Tuple2<>("1000", 1),
                new Tuple2<>("1000", 1),
                new Tuple2<>("1001", 2)
        );

        // 使用 reduce 操作进行分组和累加
        DataStream<Tuple2<String, Integer>> reduceStream =
                dataStream
                        .keyBy(value -> value.f0) // 按键分组
                        .reduce((value1, value2) -> { // 对每个键的值进行累加
                            return Tuple2.of(value1.f0, value1.f1 + value2.f1);
                        });

        SingleOutputStreamOperator<Row> mapDataStream = reduceStream.map(new MapFunction<Tuple2<String, Integer>, Row>() {
            @Override
            public Row map(Tuple2<String, Integer> value) throws Exception {
                return Row.of(value.f0, value.f1);
            }
        });
        // 打印结果
        mapDataStream.print("reduceStream"); // 这将打印出每个键的最终累加值，但可能不是按顺序的

        // 执行作业
        env.execute("Test2");

    }
}

