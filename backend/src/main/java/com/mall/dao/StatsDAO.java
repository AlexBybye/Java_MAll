package com.mall.dao;

import com.mall.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售统计数据访问对象 (Stats DAO)
 */
public class StatsDAO {

    /**
     * 获取每日销售额统计
     * @param startDate 开始日期 (格式: yyyy-MM-dd)
     * @param endDate 结束日期 (格式: yyyy-MM-dd)
     * @return 每日销售数据列表
     */
    public List<Map<String, Object>> getDailySales(String startDate, String endDate) {
        List<Map<String, Object>> dailySales = new ArrayList<>();
        String sql = "SELECT DATE(order_date) as sale_date, SUM(total_amount) as total_amount " +
                "FROM order_master " +
                "WHERE DATE(order_date) BETWEEN ? AND ? " +
                "GROUP BY DATE(order_date) " +
                "ORDER BY sale_date";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, startDate);
            ps.setString(2, endDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> sale = new HashMap<>();
                    sale.put("date", rs.getDate("sale_date"));
                    sale.put("amount", rs.getBigDecimal("total_amount"));
                    dailySales.add(sale);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dailySales;
    }

    /**
     * 获取月度销售额统计
     * @param year 年份 (格式: yyyy)
     * @return 月度销售数据列表
     */
    public List<Map<String, Object>> getMonthlySales(String year) {
        List<Map<String, Object>> monthlySales = new ArrayList<>();
        String sql = "SELECT MONTH(order_date) as month, SUM(total_amount) as total_amount " +
                "FROM order_master " +
                "WHERE YEAR(order_date) = ? " +
                "GROUP BY MONTH(order_date) " +
                "ORDER BY month";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> sale = new HashMap<>();
                    sale.put("month", rs.getInt("month"));
                    sale.put("amount", rs.getBigDecimal("total_amount"));
                    monthlySales.add(sale);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monthlySales;
    }

    /**
     * 获取热销商品统计
     * @param limit 商品数量限制
     * @return 热销商品列表
     */
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<Map<String, Object>> topProducts = new ArrayList<>();
        String sql = "SELECT p.id, p.name, SUM(oi.quantity) as total_quantity, " +
                "SUM(oi.price_at_purchase * oi.quantity) as total_amount " +
                "FROM order_item oi " +
                "JOIN product p ON oi.product_id = p.id " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_quantity DESC " +
                "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", rs.getInt("id"));
                    product.put("productName", rs.getString("name"));
                    product.put("totalQuantity", rs.getInt("total_quantity"));
                    product.put("totalAmount", rs.getBigDecimal("total_amount"));
                    topProducts.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topProducts;
    }

    /**
     * 获取订单状态统计
     * @return 订单状态统计列表
     */
    public List<Map<String, Object>> getOrderStatusStats() {
        List<Map<String, Object>> statusStats = new ArrayList<>();
        String sql = "SELECT order_status as status, COUNT(*) as order_count " +
                "FROM order_master " +
                "GROUP BY order_status";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("status", rs.getString("status"));
                stat.put("count", rs.getInt("order_count"));
                statusStats.add(stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statusStats;
    }
}