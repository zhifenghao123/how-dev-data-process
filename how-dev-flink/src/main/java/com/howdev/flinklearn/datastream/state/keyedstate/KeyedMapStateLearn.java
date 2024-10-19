package com.howdev.flinklearn.datastream.state.keyedstate;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 实时检测每个用户购买的每种产品的数量
 */
public class KeyedMapStateLearn {
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

                            MapState<String, Integer> productToCountMapState;

                            @Override
                            public void open(Configuration parameters) throws Exception {
                                super.open(parameters);
                                productToCountMapState = getRuntimeContext().getMapState(new MapStateDescriptor<>("productToCountMapState", Types.STRING, Types.INT));
                            }

                            @Override
                            public void processElement(OrderRecord value, KeyedProcessFunction<String, OrderRecord, String>.Context ctx, Collector<String> out) throws Exception {
                                String productName = value.getProductName();
                                if (productToCountMapState.contains(productName)) {
                                    Integer count = productToCountMapState.get(productName);
                                    productToCountMapState.put(productName, count + 1);
                                } else {
                                    productToCountMapState.put(productName, 1);
                                }

                                // 输出当前用户购买的各个产品的数量情况
                                StringBuilder productToCountBuilder = new StringBuilder();
                                for (Map.Entry<String, Integer> entry : productToCountMapState.entries()) {
                                    productToCountBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
                                }
                                out.collect(String.format("用户%s ,购买的各个产品的数量情况：\n %s", value.getUserId(), productToCountBuilder));
                            }
                        }
                );

        processedDataStream.print();

        env.execute();
    }
}
