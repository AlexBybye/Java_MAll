package com.mall.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接工具类
 */
public class DBUtil {

    // 数据库连接信息
    private static final String URL = "jdbc:mysql://localhost:3306/mall_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // 替换为你的 MySQL 用户名
    private static final String PASSWORD = "Mimashi1"; // 替换为你的 MySQL 密码

    // 静态代码块，在类加载时加载数据库驱动
    static {
        try {
            // 注册驱动（MySQL 8 之后可以省略，但写上更稳健）
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("MySQL Driver not found!");
        }
    }

    /**
     * 获取数据库连接
     * @return Connection 数据库连接对象
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 关闭数据库连接资源
     * @param conn 连接对象
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}