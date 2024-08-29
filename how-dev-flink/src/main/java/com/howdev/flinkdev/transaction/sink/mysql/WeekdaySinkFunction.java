package com.howdev.flinkdev.transaction.sink.mysql;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.types.Row;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeekdaySinkFunction extends RichSinkFunction<Row> {

    private transient Connection connection;
    private final String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/hao_db_1?user=root&password=root_123";
    private final String[] tableNames = {"transaction_0", "transaction_1", "transaction_2", "transaction_3", "transaction_4", "transaction_5", "transaction_6"};

    @Override
    public void open(Configuration configuration) throws Exception {
        super.open(configuration);

        // 增加连接超时时间，例如设置为10秒
        DriverManager.setLoginTimeout(10);

        this.connection = DriverManager.getConnection(jdbcUrl);
        // 注意：在生产环境中，建议使用连接池来管理数据库连接
    }

    @Override
    public void invoke(Row value, Context context) throws Exception {
        String occurTimeStr = (String) value.getField(6);
        System.out.println(occurTimeStr);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 解析字符串为LocalDateTime
        LocalDateTime occurTime = LocalDateTime.parse(occurTimeStr, formatter);

        int dayOfWeek = occurTime.getDayOfWeek().getValue() - 1;

        // 检查是否有效的工作日
        if (dayOfWeek < 0 || dayOfWeek >= tableNames.length) {
            throw new IllegalArgumentException("Day of week out of range");
        }

        // 获取表名
        String tableName = tableNames[dayOfWeek];

        // 准备 SQL 语句并执行
        String insertSQL = "INSERT INTO " + tableName + " (userId, amount, type, balance, occurredLocation, occurredTime) VALUES (?, ?, ?, ?, ?, ?)";

        System.out.println(value);
        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            // 注意：这里也需要类型转换
            stmt.setLong(1, (Long) value.getField(1));
            stmt.setDouble(2, (Double) value.getField(2));
            stmt.setString(3, (String) value.getField(3));
            stmt.setDouble(4, (Double) value.getField(4));
            stmt.setString(5, (String) value.getField(5));


            // 注意：LocalDateTime 需要转换为 Timestamp
            stmt.setTimestamp(6, Timestamp.valueOf(occurTime));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert into database", e);
        }
    }


    @Override
    public void close() throws Exception {
        super.close();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}