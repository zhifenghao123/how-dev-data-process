package com.howdev.flinklearn.datastream.state.keyedstate;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.AggregatingState;
import org.apache.flink.api.common.state.AggregatingStateDescriptor;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * 实时检测每个用户的平均购买金额
 */
public class KeyedAggregateStateLearn {
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

                           AggregatingState<Double, Double> aggregatingState;

                            @Override
                            public void open(Configuration parameters) throws Exception {
                                super.open(parameters);
                                aggregatingState = getRuntimeContext().getAggregatingState(
                                        new AggregatingStateDescriptor<Double, Tuple2<Integer, Double>, Double>("aggregatingState", new AggregateFunction<Double, Tuple2<Integer, Double>, Double>() {
                                            @Override
                                            public Tuple2<Integer, Double> createAccumulator() {
                                                return Tuple2.of(0, 0.0);
                                            }

                                            @Override
                                            public Tuple2<Integer, Double> add(Double value, Tuple2<Integer, Double> accumulator) {
                                                return Tuple2.of(accumulator.f0 + 1, accumulator.f1 + value);
                                            }

                                            @Override
                                            public Double getResult(Tuple2<Integer, Double> accumulator) {
                                                return accumulator.f1 / accumulator.f0;
                                            }

                                            @Override
                                            public Tuple2<Integer, Double> merge(Tuple2<Integer, Double> a, Tuple2<Integer, Double> b) {
                                                return null;
                                            }
                                        }, Types.TUPLE(Types.INT, Types.DOUBLE))
                                );
                            }

                            @Override
                            public void processElement(OrderRecord value, KeyedProcessFunction<String, OrderRecord, String>.Context ctx, Collector<String> out) throws Exception {
                                aggregatingState.add(value.getOrderAmount());
                                out.collect(String.format("用户%s 购买的平均金额： %f", value.getUserId(), aggregatingState.get()));
                            }
                        }
                );

        processedDataStream.print();

        env.execute();
    }
}
