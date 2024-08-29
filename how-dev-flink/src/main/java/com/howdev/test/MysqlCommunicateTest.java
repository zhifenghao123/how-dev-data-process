package com.howdev.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlCommunicateTest {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // 加载MySQL JDBC驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 建立数据库连接
            String url = "jdbc:mysql://localhost:3306/hao_db_1?serverTimezone=UTC"; // 修改为你的数据库URL
            String user = "root"; // 修改为你的数据库用户名
            String password = "root_123"; // 修改为你的数据库密码
            conn = DriverManager.getConnection(url, user, password);

            // 执行SQL查询
            stmt = conn.createStatement();
            String sql = "SELECT * FROM transaction_0"; // 修改为你的SQL查询
            rs = stmt.executeQuery(sql);

            // 处理结果集
            while (rs.next()) {
                // 假设你的表有两列，分别是id和userId
                long id = rs.getLong("id");
                long userId = rs.getLong("userId");
                System.out.println("ID: " + id + ", userId: " + userId);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC驱动未找到，请确保已添加到类路径中。");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
