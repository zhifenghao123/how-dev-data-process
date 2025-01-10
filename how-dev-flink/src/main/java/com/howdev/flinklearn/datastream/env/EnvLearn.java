package com.howdev.flinklearn.datastream.env;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class EnvLearn {
    public static void main(String[] args) throws Exception {
        // 修改设置一些环境变量
        Configuration configuration = new Configuration();
        configuration.setString("rest.port","9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);

        // Flink流批一体：代码api是一套，通过这个参数执行批或者流，默认为流
        // 一般不在代码写死，提交时，参数指定：-Dexcution.runtime-mode=BATCH
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        DataStreamSource<String> lineDataStreamSource = env.readTextFile("how-dev-flink/data_file/com/howdev/flinklearn/wordcount/word_data.txt");

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

        wordGroupSumDataSource.print();

        /**
         * 需要注意的是，写完输出（sink）操作并不代表程序已经结束。因为当main()方法被调用时，其实只是定义了作业的每个执行操作，然后添加到数据流图中；
         * 这时并没有真正处理数据,因为数据可能还没来。
         * Flink 是由事件驱动的，只有等到数据到来，才会触发真正的计算，这也被称为“延迟执行”或“懒执行”。
         * 所以我们需要显式地调用执行环境的 execute（）方法，来触发程序执行。execute方法将一直等待作业完成，然后返回一个执行结果（JobExecutionResult）。
         * 总结：
         * 1、默认 env.execute（）触发一个fhink job：
         *      一个main方法可以调用多个execute，但是没意义，指定到第一个就会阻塞住
         *  2、env.executeAsync（），异步触发，不阻塞
         *      =>—个main方法里 executeAsync（）个数 = 生成的flink job数
         * 3、思考；
         * yarn-application 集群，提交一次，葉群里会有几个fLink job？
         *      =》 取快于 调用Jn个 executeAsync（）
         *      =》耐应 application群里，会有n个job
         *      =》对应 Jobmanager当中，会有 n个 JobMaste
         */
        env.execute();


        // env.executeAsync(); 可以在一个main中提交两个任务，但不建议
    }
}
