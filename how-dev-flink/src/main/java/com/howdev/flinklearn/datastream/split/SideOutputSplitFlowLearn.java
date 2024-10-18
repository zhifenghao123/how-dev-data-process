package com.howdev.flinklearn.datastream.split;

import com.howdev.flinklearn.biz.domain.UserBrowsingRecord;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class SideOutputSplitFlowLearn {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(2);
        // 可以使用 'nc -lk 9999' 监听9999端口，并发送数据
        DataStreamSource<String> dataStreamSource = env.socketTextStream("127.0.0.1", 9999);

        SingleOutputStreamOperator<UserBrowsingRecord> userDataStream = dataStreamSource.map(new MapFunction<String, UserBrowsingRecord>() {
            @Override
            public UserBrowsingRecord map(String value) throws Exception {
                String[] split = value.split(",");
                return new UserBrowsingRecord(split[0], split[1], Double.valueOf(split[2]), Long.valueOf(split[3]));
            }
        });

        /**
         * 使用侧输出流实现分流：
         *  1. 使用process算子
         *  2. 定义OutputTag对象
         *  3. 调用ctx.output方法，将数据输出到侧输出流
         *  4. 通过主流获取侧输出流
         */
        OutputTag<UserBrowsingRecord> invalidGenderTag = new OutputTag<>("INVALID_GENDER", Types.POJO(UserBrowsingRecord.class));
        SingleOutputStreamOperator<UserBrowsingRecord> processedDataStream = userDataStream.process(new ProcessFunction<UserBrowsingRecord, UserBrowsingRecord>() {
            @Override
            public void processElement(UserBrowsingRecord value, Context ctx, Collector<UserBrowsingRecord> out) throws Exception {
                String productName = value.getProductName();

                if (productName == null || productName.isEmpty()) {
                    ctx.output(invalidGenderTag, value);
                } else {
                    out.collect(value);
                }

            }
        });

        // 打印主输出流
        processedDataStream.print("主流：");
        // 打印侧输出流
        processedDataStream.getSideOutput(invalidGenderTag).print("无效产品-侧输出流：");

        env.execute();
    }
}
