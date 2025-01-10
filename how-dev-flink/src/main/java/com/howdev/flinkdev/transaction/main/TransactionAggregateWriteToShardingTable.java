package com.howdev.flinkdev.transaction.main;

import com.howdev.flinkdev.transaction.sink.mysql.WeekdaySinkFunction2;
import com.howdev.flinkdev.transaction.biz.mock.MockStreamSource;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

public class TransactionAggregateWriteToShardingTable {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setString("rest.port","9001");

        // StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 创建带有WebUI的本地流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStream<Row> streamSource = env.addSource(new MockStreamSource.MyParallelRowStreamSource())
                // SQL模式下需要显式指明字段类型
                .returns(Types.ROW(Types.LONG , Types.LONG, Types.DOUBLE, Types.STRING, Types.STRING, Types.STRING));

        //streamSource.addSink(new WeekdaySinkFunction());
        streamSource.addSink(new WeekdaySinkFunction2());

        String jobName = TransactionAggregateWriteToShardingTable.class.getSimpleName();
        env.execute(jobName);

    }
}
