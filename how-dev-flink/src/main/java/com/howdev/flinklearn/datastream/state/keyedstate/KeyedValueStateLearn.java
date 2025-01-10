package com.howdev.flinklearn.datastream.state.keyedstate;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * 检测每个用户的订单金额，如果连续两次的金额查超过500，则输出提示
 */
public class KeyedValueStateLearn {
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
                    // 注意：使用普通变量，会导致变量在不同的并行任务中共享，会导致数据错乱，所以使用状态变量
                    ValueState<Double> lastOrderAmountState;

                    // TODO 2. 在open方法中初始化状态变量
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        lastOrderAmountState = getRuntimeContext().getState(new ValueStateDescriptor<>("lastOrderAmountState", Types.DOUBLE));
                    }

                    @Override
                    public void processElement(OrderRecord value, KeyedProcessFunction<String, OrderRecord, String>.Context ctx, Collector<String> out) throws Exception {
                        // （1）取出上一条数据的订单金额
                        Double lastOrderAmount = lastOrderAmountState.value() == null ? 0.0 : lastOrderAmountState.value();

                        // （2）判断当前订单金额和上一条数据的订单金额差值的绝对值，判断是否超过500
                        Double currentOrderAmount = value.getOrderAmount();
                        if (Math.abs(currentOrderAmount - lastOrderAmount) > 500) {
                            out.collect(String.format("用户%s的订单金额差超过500，当前订单金额为%s，上一条数据的订单金额为%s", value.getUserId(), currentOrderAmount, lastOrderAmount));
                        }

                        // （3）保存更新自己的订单金额
                        lastOrderAmountState.update(currentOrderAmount);
                    }
                });

        processedDataStream.print();

        env.execute();
    }
}
