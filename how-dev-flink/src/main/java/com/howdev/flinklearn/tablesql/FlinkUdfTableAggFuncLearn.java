package com.howdev.flinklearn.tablesql;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.table.functions.TableAggregateFunction;
import org.apache.flink.util.Collector;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.call;

public class FlinkUdfTableAggFuncLearn {
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
                Tuple3.of("li", 95, 2),
                Tuple3.of("zs", 86, 2),
                Tuple3.of("li", 85, 2)
        );


        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        Table scorTable = tableEnv
                .fromDataStream(scoreWeightDataStreamSource, $("f0").as("username"), $("f1").as("score"));
        tableEnv.createTemporaryView("score", scorTable);

        // 2.注册 自定义函数
        tableEnv.createFunction("Top2", Top2.class);

        // 3.调用自定义函数
        // table api用法(只能使用table api)
        scorTable
                .groupBy($("username"))
                .flatAggregate(
                        call("Top2", $("score")).as("value", "rank")
                )
                .select($("username"), $("value"), $("rank"))
                .execute()
                .print();


    }

    // 1.定义 自定义函数的实现类
    // 返回类型：（数值，排名）
    // 累加器类型：（第一大的数，第二大的数）
    public static class Top2 extends TableAggregateFunction<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>{

        @Override
        public Tuple2<Integer, Integer> createAccumulator() {
            return Tuple2.of(0, 0);
        }

        /**
         * 每来一个数据都会调用一次：比较大小，更新最大的两个数到acc中
         * @param accumulator
         * @param score
         */
        public void accumulate(Tuple2<Integer, Integer> accumulator, Integer score) {
            if (score > accumulator.f0) {
                // 新来的变第一，原来的第一变第二
                accumulator.f1 = accumulator.f0;
                accumulator.f0 = score;
            } else if (score > accumulator.f1) {
                // 新来的变第二，原来的第二不要了
                accumulator.f1 = score;

            }

        }

        /**
         * 输出结果：（输出，排名） 最大的两条
         * @param accumulator
         * @param collector
         */
        public void emitValue(Tuple2<Integer, Integer> accumulator, Collector<Tuple2<Integer, Integer>> collector) {
            if (accumulator.f0 != 0) {
                collector.collect(Tuple2.of(accumulator.f0, 1));
            }
            if (accumulator.f1 != 0) {
                collector.collect(Tuple2.of(accumulator.f1, 2));
            }
        }
    }
}
