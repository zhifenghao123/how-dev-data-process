package com.howdev.flinklearn.transformation;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class UdfRichFunctionLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Integer> dataStreamSource = env.fromElements(1, 2, 3, 4, 5);

        /**
         * RichXXXFunction:富函数
         *  1.多了生命周期管理方法：
         *      open()：在每个子任务启动时调用一次。常常用于初始化一些状态，比如连接数据库，创建线程池等。
         *      close():在每个子任务结束退出时调用一次。（如果是flink任务异常挂掉，则不会调用； 如果是正常调用cancel，则可以调用close）
         *  2.多了 RuntimeContext运行时上下文对象，可以获取子任务编号、名称等等
         *
         */
        SingleOutputStreamOperator<Integer> mappedDataStream = dataStreamSource.map(new RichMapFunction<Integer, Integer>() {
            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                RuntimeContext runtimeContext = getRuntimeContext();
                int indexOfThisSubtask = runtimeContext.getIndexOfThisSubtask();
                String taskNameWithSubtasks = runtimeContext.getTaskNameWithSubtasks();
                System.out.println("open，子任务编号=" + indexOfThisSubtask + "启动， 子任务名称=" + taskNameWithSubtasks);
            }

            @Override
            public void close() throws Exception {
                RuntimeContext runtimeContext = getRuntimeContext();
                int indexOfThisSubtask = runtimeContext.getIndexOfThisSubtask();
                String taskNameWithSubtasks = runtimeContext.getTaskNameWithSubtasks();
                System.out.println("close，子任务编号=" + indexOfThisSubtask + "启动， 子任务名称=" + taskNameWithSubtasks);
                super.close();
            }

            @Override
            public Integer map(Integer value) throws Exception {
                return value * 100;
            }
        });


        mappedDataStream.print();

        env.execute();
    }
}
