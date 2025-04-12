package com.howdev.flinklearn.tablesql;

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

import static org.apache.flink.table.api.Expressions.$;

public class FlinkUdfAggFuncLearn {
    public static void main(String[] args) throws Exception {

        // 创建流执行环境
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建带有WebUI的本地流执行环境
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStreamSource<Tuple3<String, Integer, Integer>> scoreWeightDataStreamSource = env.fromElements(
               Tuple3.of("zs", 90, 2),
                Tuple3.of("li", 82, 3),
                Tuple3.of("zs", 77, 1),
                Tuple3.of("zs", 79, 2),
                Tuple3.of("li", 95, 2)
        );


        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        Table scoreWeightTable = tableEnv
                .fromDataStream(scoreWeightDataStreamSource, $("f0").as("username"), $("f1").as("score"), $("f2").as("weight"));
        tableEnv.createTemporaryView("score_weight", scoreWeightTable);

        // 2.注册 自定义函数
        tableEnv.createFunction("WeightedAvg", WeightedAvg.class);

        // 3.调用自定义函数
        // 3.1 sql用法

        tableEnv
                .sqlQuery("SELECT username, WeightedAvg(score, weight) from score_weight group by username")
                .execute() // 调用了sql的execute，就不需要调用env的execute了
                .print();

        // 3.1 table api用法
//        orderRecordTable.select(call("HashFunction", $("userId")))
//                .execute()
//                .print();


    }

    // 1.定义 自定义函数的实现类
    public static class WeightedAvg extends AggregateFunction<Double, Tuple2<Integer, Integer>> {

        @Override
        public Double getValue(Tuple2<Integer, Integer> accumulator) {
            return accumulator.f0 * 1D / accumulator.f1;
        }

        @Override
        public Tuple2<Integer, Integer> createAccumulator() {
            return Tuple2.of(0, 0);
        }

        /**
         * 累加器的计算方法（加权平均值），每来一条数据都会调用一次
         * @param accumulator
         * @param score
         * @param weight
         */
        public void accumulate(Tuple2<Integer, Integer> accumulator, Integer score, Integer weight) {
            accumulator.f0 += score * weight;
            accumulator.f1 += weight;
        }
    }
}
