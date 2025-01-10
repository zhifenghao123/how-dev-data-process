package com.howdev.flinklearn.datastream.state.operatorstate;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 在map算子中计算数据的条数
 */
public class OperatorListStateLearn {
    public static void main(String[] args) throws Exception {
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(2);

        SingleOutputStreamOperator<OrderRecord> orderRecordDataStream = env.socketTextStream("127.0.0.1", 9999)
                .map(new MyMapFunction())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<OrderRecord>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, recordTimestamp) -> element.getOrderTimestamp()));


        SingleOutputStreamOperator<String> processedDataStream = orderRecordDataStream.keyBy(OrderRecord::getUserId)
                .process(new KeyedProcessFunction<String, OrderRecord, String>() {
                    // TODO 1. 定义状态变量
                   ListState<Double> orderAmountListState;

                    // TODO 2. 在open方法中初始化状态变量
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        orderAmountListState = getRuntimeContext().getListState(new ListStateDescriptor<>("orderAmountListState", Types.DOUBLE));
                    }

                    @Override
                    public void processElement(OrderRecord value, KeyedProcessFunction<String, OrderRecord, String>.Context ctx, Collector<String> out) throws Exception {
                        //(1) 来一条，就存储到listState中
                        orderAmountListState.add(value.getOrderAmount());

                        // (2)从listState中获取数据，并排序，取前3个
                        Iterable<Double> orderAmountListStateIter = orderAmountListState.get();
                        List<Double> orderAmountList = new ArrayList<>();
                        for (Double orderAmount : orderAmountListStateIter) {
                            orderAmountList.add(orderAmount);
                        }
                        // （3）降序排序
                        orderAmountList.sort((o1, o2) -> Double.compare(o2, o1));

                        // （4）取前3个(list中的个数一定是连续变大的，一超过3个，就删除最后一个)
                        if(orderAmountList.size() > 3) {
                            // 将最后一个删除（第4个）
                            orderAmountList.remove(3);
                        }

                        // （5）输出
                        out.collect(value.getUserId() + "最大的三个订单金额：" + orderAmountList);

                        // (6)更新状态
                        orderAmountListState.update(orderAmountList);

                    }
                });

        processedDataStream.print();

        env.execute();
    }

    // TODO1:实现CheckpointedFunction接口
    static class MyMapFunction implements MapFunction<String, OrderRecord>, CheckpointedFunction {
        private Long count = 0L;
        private ListState<Long> listState;
        @Override
        public OrderRecord map(String value) throws Exception {
            String[] fields = value.split(",");
            // 本地变量count自增1
            count++;
            System.out.println("count:" + count);
            return new OrderRecord(fields[0], fields[1], Double.parseDouble(fields[2]), Long.parseLong(fields[3]));
        }

        // 本地变量持久化：将本地变量拷贝到算子状态中，只有开启了checkpoint，才会调用snapshotState方法。
        @Override
        public void snapshotState(FunctionSnapshotContext context) throws Exception {
            System.out.println("snapshotState...");
            // （1）清空算子状态，避免被其他的干扰
            listState.clear();
            // （2）将本地变量拷贝到算子状态中
            listState.add(count);
        }

        // 初始化本地变量。 程序恢复时，从算子状态中恢复本地变量，每个子任务调用一次
        @Override
        public void initializeState(FunctionInitializationContext context) throws Exception {
            System.out.println("initializeState...");
            // （3）从上下文获取（初始化）算子状态
            /**
             * 算子状态中，List 与 unionkiat的区别：
             * 并行度改变时，怎么重新分配状态
             * 1、List状态：轮询均分 给 新的 并行子任务
             * 2、wnionkist状态：原先的事个子任务的状态，台并成一份完整的。会把 整的列表 广播给 新的并行子任务（每人一份完整的）
             */
            listState = context
                    .getOperatorStateStore()
                    //.getListState(new ListStateDescriptor<>("listState", Types.LONG));
                    .getUnionListState(new ListStateDescriptor<>("listState", Types.LONG));



            // （4）从算子状态中恢复本地变量
            if (context.isRestored()) {
                for (Long count : listState.get()) {
                    this.count += count;
                }
            }
        }
    }
}
