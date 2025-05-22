package com.howdev.flinkdev.flink.sink.mysql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.types.Row;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeekdaySinkFunction2 extends RichSinkFunction<Row> {

    private BasicDataSource dataSource;
    private final String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/hao_db_1?user=root&password=root_123";
    private final String[] tableNames = {"transaction_0", "transaction_1", "transaction_2", "transaction_3", "transaction_4", "transaction_5", "transaction_6", "transaction_7"};

    @Override
    public void open(Configuration configuration) throws Exception {
        super.open(configuration);
        // 初始化BasicDataSource配置
        dataSource = new BasicDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername("root");
        dataSource.setPassword("root_123");
        // 设置连接池最大连接数
        dataSource.setMaxTotal(10);
        // 设置最小空闲连接数
        dataSource.setMinIdle(5);
        // 设置最大空闲连接数
        dataSource.setMaxIdle(10);
        // 设置连接超时时间(毫秒)，这里使用try-catch来处理不支持setLoginTimeout的问题
        try {
            dataSource.setLoginTimeout(10);
        } catch (UnsupportedOperationException e) {
            // 处理不支持setLoginTimeout的情况，例如记录日志或使用其他方式设置超时
            System.err.println("BasicDataSource不支持setLoginTimeout方法。");
        }
    }

    @Override
    public void invoke(Row value, Context context) throws Exception {
        String occurTimeStr = (String) value.getField(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime occurTime = LocalDateTime.parse(occurTimeStr, formatter);

        System.out.println("DayOfWeek:" + occurTime.getDayOfWeek().getValue());
        int dayOfWeek = occurTime.getDayOfWeek().getValue();
        if (dayOfWeek <= 0 || dayOfWeek > 7) {
            throw new IllegalArgumentException("Day of week out of range");
        }

        String tableName = tableNames[dayOfWeek];
        String insertSQL = "INSERT INTO " + tableName + " (userId, amount, type, balance, occurredLocation, occurredTime) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
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
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static void main(String[] args) {
        String[] tableNames = {"transaction_0", "transaction_1", "transaction_2", "transaction_3", "transaction_4", "transaction_5", "transaction_6", "transaction_7"};

        String occurTimeStr = "2024-08-04 20:19:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime occurTime = LocalDateTime.parse(occurTimeStr, formatter);

        System.out.println("DayOfWeek:" + occurTime.getDayOfWeek().getValue());
        int dayOfWeek = occurTime.getDayOfWeek().getValue();
        if (dayOfWeek <= 0 || dayOfWeek > 7) {
            throw new IllegalArgumentException("Day of week out of range");
        }

        String tableName = tableNames[dayOfWeek];
        System.out.println(tableName);
    }
}
