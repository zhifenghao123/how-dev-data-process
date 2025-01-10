package com.howdev.flinklearn.datastream.state.operatorstate;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单金额超过指定阈值就报警，其中阈值可以动态修改
 */
public class OperatoBroadcastStateLearn {
    public static void main(String[] args) throws Exception {
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(2);

        // 数据流
        SingleOutputStreamOperator<OrderRecord> orderRecordDataStream = env.socketTextStream("127.0.0.1", 9999)
                .map(new MapFunction<String, OrderRecord>() {
                    @Override
                    public OrderRecord map(String value) throws Exception {
                        String[] fields = value.split(",");
                        return new OrderRecord(fields[0], fields[1], Double.parseDouble(fields[2]), Long.parseLong(fields[3]));
                    }
                });

        // （1）定义配置流（用来广播配置）
        DataStreamSource<String> configDataStream = env.socketTextStream("127.0.0.1", 8888);
        // （2）将配置流广播
        MapStateDescriptor<String, String> mapStateDescriptor = new MapStateDescriptor<>("broadcast-state", Types.STRING, Types.STRING);
        BroadcastStream<String> configBroadcastStream = configDataStream.broadcast(mapStateDescriptor);

        // （3）将数据流和广播后的配置流connect
        BroadcastConnectedStream<OrderRecord, String> orderRecordBCS = orderRecordDataStream.connect(configBroadcastStream);


        SingleOutputStreamOperator<String> processedDataStream = orderRecordBCS.process(new BroadcastProcessFunction<OrderRecord, String, String>() {
            // 数据流的处理方法
            @Override
            public void processElement(OrderRecord value, BroadcastProcessFunction<OrderRecord, String, String>.ReadOnlyContext ctx, Collector<String> out) throws Exception {
                // （4）通过上下文获取广播状态，读取里面的数据
                ReadOnlyBroadcastState<String, String> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
                String thresholdConfig = broadcastState.get("threshold");
                // 这里要判断广播状态里是否有数据，因为刚启动时，可能是数据流的第一条数据先来
                Double threshold = thresholdConfig == null ? 0D : Double.parseDouble(thresholdConfig);
                if (value.getOrderAmount() > threshold) {
                    out.collect(String.format("用户%s的订单金额超过%s", value.getUserId(), threshold));
                }
            }


            // 广播后的配置流的处理方法
            @Override
            public void processBroadcastElement(String value, BroadcastProcessFunction<OrderRecord, String, String>.Context ctx, Collector<String> out) throws Exception {
                // （4）通过上下文获取广播状态，往里面写数据
                BroadcastState<String, String> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
                System.out.println("更新广播状态：threshold=" + value);
                broadcastState.put("threshold", value);
            }
        });


        processedDataStream.print();

        env.execute();
    }

}
