package com.howdev.flinklearn.datastream.state.keyedstate;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 实时检测每个用户的订单金额，输出每个用户最高的三个订单金额
 */
public class KeyedListStateLearn {
    public static void main(String[] args) throws Exception {
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        SingleOutputStreamOperator<OrderRecord> orderRecordDataStream = env.socketTextStream("127.0.0.1", 9999)
                .map(new MapFunction<String, OrderRecord>() {
                    @Override
                    public OrderRecord map(String value) throws Exception {
                        String[] fields = value.split(",");
                        return new OrderRecord(fields[0], fields[1], Double.parseDouble(fields[2]), Long.parseLong(fields[3]));
                    }
                })
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
}
