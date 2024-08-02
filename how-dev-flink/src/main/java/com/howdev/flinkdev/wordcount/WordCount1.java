package com.howdev.flinkdev.wordcount;

import com.howdev.mock.stream.MockStreamSource;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

import java.util.Objects;

public class WordCount1 {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setString("rest.port","9001");

        // StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 创建带有WebUI的本地流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        System.out.println("Hello World");

        DataStream<Row> streamSource = env.addSource(new MockStreamSource.MyParallelRowStreamSource())
                // SQL模式下需要显式指明字段类型
                .returns(Types.ROW(Types.LONG , Types.LONG, Types.DOUBLE, Types.STRING, Types.STRING, Types.STRING));

        SingleOutputStreamOperator<String> locationOutputStream = streamSource.map(new MapFunction<Row, String>() {
            @Override
            public String map(Row value) throws Exception {
                return Objects.requireNonNull(value.getField(4)).toString();
            }
        });

        locationOutputStream.print();

        // 对单词进行统计
        DataStream<Tuple2<String, Integer>> wordCounts = locationOutputStream
                .flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
                        String[] words = value.split("\\s");
                        for (String word : words) {
                            out.collect(new Tuple2<>(word, 1));
                        }
                    }
                })
                .keyBy(0)
                .sum(1);

        wordCounts.print();

        env.execute("WordCount");

    }
}
