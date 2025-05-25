package com.howdev.flinklearn.tablesql;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import static org.apache.flink.table.api.Expressions.$;

public class FlinkSqlLearn {
    public static void main(String[] args) throws Exception {

        // 创建流执行环境
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建带有WebUI的本地流执行环境
        Configuration configuration = new Configuration();
        configuration.setString("rest.port", "9001");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        // Step1:创建表环境
        // 写法一：
/*        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);*/

        // 写法二：本身默认就是流模式
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Step2:创建表
        tableEnv.executeSql("CREATE TABLE order_record (\n" +
                " order_id BIGINT,\n" +
                " user_id BIGINT,\n" +
                " product_name STRING,\n" +
                " order_amount DOUBLE,\n" +
                " order_time_stamp AS cast(CURRENT_TIMESTAMP as timestamp(3))\n" +
                " ) WITH (\n" +
                " 'connector' = 'datagen',\n" +
                " 'rows-per-second' = '5',\n" +
                " 'fields.order_id.kind' = 'sequence',\n" +
                " 'fields.order_id.start' = '1',\n" +
                " 'fields.order_id.end' = '100000',\n" +
                " 'fields.user_id.min' = '1',\n" +
                " 'fields.user_id.max' = '10',\n" +
                " 'fields.product_name.length' = '5',\n" +
                " 'fields.order_amount.min' = '50',\n" +
                " 'fields.order_amount.max' = '5000'\n" +
                ");");


        tableEnv.executeSql("CREATE TABLE order_record_sink (\n" +
                " user_id BIGINT,\n" +
                " total_amount DOUBLE\n" +
                ") WITH (\n" +
                " 'connector' = 'print'\n" +
                ");");

        // Step3:执行查询
        // 方式一：使用SQL进行查询
        Table table = tableEnv.sqlQuery("SELECT user_id, SUM(order_amount) as total_amount FROM order_record WHERE order_amount > 100 group by user_id");
        // 把table对象注册成一个表，并指定表名
        tableEnv.createTemporaryView("tmp_order_record", table);
        //tableEnv.sqlQuery("SELECT * FROM tmp_order_record");


        // Step4:输出结果
        tableEnv.executeSql("INSERT INTO order_record_sink SELECT * FROM tmp_order_record");

    }
}
