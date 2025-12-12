package com.mall.dao;

import com.mall.model.Customer;
import com.mall.util.DBUtil; // 确保你已经创建了 com.mall.util.DBUtil
import java.sql.*;

/**
 * 顾客数据访问对象 (Data Access Object)
 */
public class CustomerDAO {
    public String getEmailById(int customerId) {
        String sql = "SELECT email FROM customer WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 检查用户名是否已存在
     */
    public boolean isUsernameExist(String username) {
        String sql = "SELECT COUNT(*) FROM customer WHERE username = ?";
        // 使用 try-with-resources 自动关闭连接
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 注册新顾客
     */
    public boolean registerCustomer(Customer customer) {
        String sql = "INSERT INTO customer (username, password, email, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getUsername());
            ps.setString(2, customer.getPassword());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Customer loginCustomer(String username, String password) {
        // 实际项目中，password 应该是加密后的哈希值进行比对
        String sql = "SELECT id, username, password, email, phone FROM customer WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    // 【⚠️ 警告】：这里是明文密码比对，实际生产中需使用 BCrypt 等加密算法
                    if (storedPassword.equals(password)) {
                        Customer customer = new Customer();
                        customer.setId(rs.getInt("id"));
                        customer.setUsername(rs.getString("username"));
                        customer.setEmail(rs.getString("email"));
                        customer.setPhone(rs.getString("phone"));
                        // 不返回密码给前端
                        return customer;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}

