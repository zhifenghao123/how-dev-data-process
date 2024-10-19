package com.howdev.flinklearn.datastream.state.keyedstate;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Map;

/**
 * 实时检测每个用户购买的每种产品的金额之和
 */
public class KeyedReducingStateLearn {
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
                .process(
                        new KeyedProcessFunction<String, OrderRecord, String>() {

                           ReducingState<Double> amountReducingState;

                            @Override
                            public void open(Configuration parameters) throws Exception {
                                super.open(parameters);
                                amountReducingState = getRuntimeContext().getReducingState(
                                        new ReducingStateDescriptor<Double>(
                                                "amountReducingState",
                                                new ReduceFunction<Double>() {
                                                    @Override
                                                    public Double reduce(Double value1, Double value2) throws Exception {
                                                        return value1 + value2;
                                                    }
                                                },
                                                Types.DOUBLE));
                            }

                            @Override
                            public void processElement(OrderRecord value, KeyedProcessFunction<String, OrderRecord, String>.Context ctx, Collector<String> out) throws Exception {
                                amountReducingState.add(value.getOrderAmount());
                                Double amountSum = amountReducingState.get();
                                out.collect(String.format("用户%s 购买的金额和： %f", value.getUserId(), amountSum));
                            }
                        }
                );

        processedDataStream.print();

        env.execute();
    }
}
