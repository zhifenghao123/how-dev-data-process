package com.howdev.flinklearn.tablesql;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class FlinkStreamToTableLearn {
    public static void main(String[] args) throws Exception {

        // 创建流执行环境
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建带有WebUI的本地流执行环境
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStreamSource<OrderRecord> orderRecordDataStreamSource = env.fromElements(
                new OrderRecord("u1", "product1", 100.0, 1L),
                new OrderRecord("2L", "product2", 200.0, 2L),
                new OrderRecord("3L", "product1", 300.0, 3L)
        );


        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        Table orderRecordTable = tableEnv.fromDataStream(orderRecordDataStreamSource);
        tableEnv.createTemporaryView("order_record", orderRecordTable);

        // 流转表
        Table oderAmountGreater100Table = tableEnv.sqlQuery("SELECT * FROM order_record WHERE orderAmount > 100");
        Table groupByProductTable = tableEnv.sqlQuery("SELECT productName, COUNT(*) as product_count FROM order_record GROUP BY productName");

        // 表转流
        // 追加流
        DataStream<OrderRecord> orderRecordDataStream = tableEnv.toDataStream(oderAmountGreater100Table, OrderRecord.class);
        orderRecordDataStream.print("orderRecordDataStream");

        // 下面的代码报错：Table sink '*anonymous_datastream_sink$3*' doesn't support consuming update changes which is produced by node GroupAggregate(groupBy=[productName], select=[productName, COUNT(*) AS product_count])
        // DataStream<Row> groupByProductDataStream = tableEnv.toDataStream(groupByProductTable);
        // changelog 流
        DataStream<Row> groupByProductDataStream = tableEnv.toChangelogStream(groupByProductTable);
        groupByProductDataStream.print("groupByProductDataStream");




        // 只要代码中调用了DataStreamAPI，就需要调用execute()方法，否则不需要
        env.execute();
    }
}
