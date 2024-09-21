package com.howdev.flinklearn.wordcount;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * WordCount 无界流处理
 */
public class WordCountUnboundedStream {
    public static void main(String[] args) throws Exception {
        // 1. 创建流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. 读取数据
        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        DataStreamSource<String> lineDataStreamSource = env.socketTextStream("127.0.0.1", 9999);

        // 3. 处理数据:切分、转换、分组、聚合
        /**
         * Flink 还具有一个类型提取系统，可以分析函数的输入和返回类型，自动获取类型信息，从而获得对应的序列化器和反序列化器。
         * 但是，由于 Java 中泛型擦除的存在，在某些特殊情况下（比如Lambda 表达式中），自动提取的信息是不够精细的——只告诉 Flink 当前
         * 的元素由“船头、船身、船尾”构成，根本无法重建出“大船”的模样；这时就需要显式地提供类型信息，才能使应用程序正常工作或提高其性能。
         * 因为对于 flatMap里传入的Lambda 表达式，系统只能推断出返回的是 Tuple2 类型，而无法得到 Tuple2<String，Long>。
         * 只有显式地告诉系统当前的返回类型，才能正确地解析出完整数据。
         */
        SingleOutputStreamOperator<Tuple2<String, Integer>> wordGroupSumDataSource = lineDataStreamSource
                .flatMap(
                        (String value, Collector<Tuple2<String, Integer>> out) -> {
                            String[] words = value.split(" ");
                            for (String word : words) {
                                out.collect(new Tuple2<>(word, 1));
                            }
                        }
                )
                .returns(Types.TUPLE(Types.STRING, Types.INT))
                .keyBy(value -> value.f0)
                .sum(1);

        // 4. 输出数据
        wordGroupSumDataSource.print();

        // 5. 执行
        env.execute();

    }
}
