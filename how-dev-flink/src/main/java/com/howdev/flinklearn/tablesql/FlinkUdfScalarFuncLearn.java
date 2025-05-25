package com.howdev.flinklearn.tablesql;

import com.howdev.flinklearn.biz.domain.OrderRecord;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.InputGroup;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.call;

public class FlinkUdfScalarFuncLearn {
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

        // 2.注册 自定义函数
        tableEnv.createFunction("HashFunction", HashFunction.class);

        // 3.调用自定义函数
        // 3.1 sql用法
//        tableEnv.sqlQuery("SELECT HashFunction(userId) from order_record")
//                .execute() // 调用了sql的execute，就不需要调用env的execute了
//                .print();

        // 3.1 table api用法
        orderRecordTable.select(call("HashFunction", $("userId")))
                .execute()
                .print();


    }

    // 1.定义 自定义函数的实现类
    public static class HashFunction extends ScalarFunction {
        // 接收任意类型的输入，返回int类型输出
        public int eval(@DataTypeHint(inputGroup = InputGroup.ANY) Object o) {
            return o.hashCode();
        }
    }
}
