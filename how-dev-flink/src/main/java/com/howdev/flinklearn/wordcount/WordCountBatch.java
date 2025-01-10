package com.howdev.flinklearn.wordcount;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.operators.UnsortedGrouping;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

/**
 * WordCount 批处理
 */
public class WordCountBatch {
    public static void main(String[] args) throws Exception {
        // 1. 创建执行环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // 2. 从文件中读取数据
        DataSource<String> lineDataSource = env.readTextFile("how-dev-flink/data_file/com/howdev/flinklearn/wordcount/word_data.txt");

        // 3. 将数据切分、转换
        FlatMapOperator<String, Tuple2<String, Integer>> wordOneCntTupleDataSource = lineDataSource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                // 3.1 将数据切分
                String[] words = value.split(" ");
                // 3.2 将单词转换为Tuple2<String, Integer>
                for (String word : words) {
                    Tuple2<String, Integer> record = new Tuple2<>(word, 1);
                    // 3.3 使用Collector.collect()方法将数据输出到下游
                    out.collect(record);
                }
            }
        });

        // 4. 按照word分组
        UnsortedGrouping<Tuple2<String, Integer>> wordOneCntGroupDataSource = wordOneCntTupleDataSource.groupBy(0);

        // 5. 各分组内聚合
        AggregateOperator<Tuple2<String, Integer>> wordGroupSumDataSource = wordOneCntGroupDataSource.sum(1);

        // 6. 将结果输出到控制台
        wordGroupSumDataSource.print();

        /**
         * 执行结果（与流处理输出结果对比看看）
         * (Java,1)
         * (Spark,1)
         * (Flink,1)
         * (Python,1)
         * (Hello,6)
         * (Hadoop,1)
         * (Scala,1)
         */

    }
}
