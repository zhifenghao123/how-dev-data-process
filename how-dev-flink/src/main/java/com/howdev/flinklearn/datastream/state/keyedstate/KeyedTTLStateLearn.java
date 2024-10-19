package com.howdev.flinklearn.datastream.state.keyedstate;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
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
public class KeyedTTLStateLearn {
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

                    ValueState<Double> lastOrderAmountState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.seconds(5))
                                // 创建和写入的时候会更新过期时间
                                //.setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                                .setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite)
                                // 过期数据不可见，不会被访问到
                                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                                .build();

                        ValueStateDescriptor<Double> valueStateDescriptor = new ValueStateDescriptor<>("lastOrderAmountState", Types.DOUBLE);
                        valueStateDescriptor.enableTimeToLive(ttlConfig);
                        lastOrderAmountState = getRuntimeContext().getState(valueStateDescriptor);
                    }

                    @Override
                    public void processElement(OrderRecord value, KeyedProcessFunction<String, OrderRecord, String>.Context ctx, Collector<String> out) throws Exception {
                        // 先获取状态值，打印
                        Double lastOrderAmount = lastOrderAmountState.value();
                        out.collect(value.getUserId() + " 上一次的订单金额是：" + lastOrderAmount);
                        // 更新状态值
                        //lastOrderAmountState.update(value.getOrderAmount());
                        if (value.getOrderAmount() > 1000) {
                            lastOrderAmountState.update(value.getOrderAmount());
                        }

                    }
                });

        processedDataStream.print();

        env.execute();
    }
}
