package com.howdev.flinklearn.wordcount;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * WordCount 有界流处理
 */
public class WordCountBoundedStream {
    public static void main(String[] args) throws Exception {
        // 1. 创建流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. 读取数据
        DataStreamSource<String> lineDataStreamSource = env.readTextFile("how-dev-flink/data_file/com/howdev/flinklearn/wordcount/word_data.txt");

        // 3. 处理数据:切分、转换、分组、聚合
        SingleOutputStreamOperator<Tuple2<String, Integer>> wordOneCntTupleDataSource = lineDataStreamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                String[] words = value.split(" ");
                for (String word : words) {
                    out.collect(new Tuple2<>(word, 1));
                }
            }
        });

        KeyedStream<Tuple2<String, Integer>, String> wordOneCntKeyedStream = wordOneCntTupleDataSource.keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
            @Override
            public String getKey(Tuple2<String, Integer> value) throws Exception {
                return value.f0;
            }
        });

        SingleOutputStreamOperator<Tuple2<String, Integer>> wordGroupSumDataSource = wordOneCntKeyedStream.sum(1);

        // 4. 输出数据
        wordGroupSumDataSource.print();

        // 5. 执行
        env.execute();

        /**
         * 执行结果【来一条处理一条】（与批处理输出结果对比看看）
         * 8> (Java,1)
         * 10> (Spark,1)
         * 1> (Hello,1)
         * 1> (Hello,2)
         * 1> (Hadoop,1)
         * 1> (Hello,3)
         * 1> (Hello,4)
         * 10> (Python,1)
         * 1> (Hello,5)
         * 1> (Hello,6)
         * 10> (Flink,1)
         * 9> (Scala,1)
         */
    }
}
