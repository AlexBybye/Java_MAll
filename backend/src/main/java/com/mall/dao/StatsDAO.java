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
     * 获取统计概览数据
     * @return 统计概览数据
     */
    public Map<String, Object> getStatsOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 获取商品总数
        String productSql = "SELECT COUNT(*) as total FROM product WHERE is_deleted = 0";
        // 获取订单总数
        String orderSql = "SELECT COUNT(*) as total FROM order_master";
        // 获取总收入
        String revenueSql = "SELECT SUM(total_amount) as total FROM order_master";
        // 获取待处理订单数
        String pendingSql = "SELECT COUNT(*) as total FROM order_master WHERE order_status = 'PENDING'";
        
        try (Connection conn = DBUtil.getConnection()) {
            // 获取商品总数
            try (PreparedStatement ps = conn.prepareStatement(productSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.put("totalProducts", rs.getInt("total"));
                } else {
                    overview.put("totalProducts", 0);
                }
            }
            
            // 获取订单总数
            try (PreparedStatement ps = conn.prepareStatement(orderSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.put("totalOrders", rs.getInt("total"));
                } else {
                    overview.put("totalOrders", 0);
                }
            }
            
            // 获取总收入
            try (PreparedStatement ps = conn.prepareStatement(revenueSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getBigDecimal("total") != null) {
                    overview.put("totalRevenue", rs.getBigDecimal("total"));
                } else {
                    overview.put("totalRevenue", 0);
                }
            }
            
            // 获取待处理订单数
            try (PreparedStatement ps = conn.prepareStatement(pendingSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.put("pendingOrders", rs.getInt("total"));
                } else {
                    overview.put("pendingOrders", 0);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return overview;
    }

    /**
     * 获取每日销售额统计
     * @param startDate 开始日期 (格式: yyyy-MM-dd)
     * @param endDate 结束日期 (格式: yyyy-MM-dd)
     * @return 每日销售数据列表
     */
    public List<Map<String, Object>> getDailySales(String startDate, String endDate) {
        List<Map<String, Object>> dailySales = new ArrayList<>();
        String sql = "SELECT DATE(order_date) as sale_date, SUM(total_amount) as sales_amount, COUNT(*) as order_count " +
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
                    sale.put("salesAmount", rs.getBigDecimal("sales_amount"));
                    sale.put("orderCount", rs.getInt("order_count"));
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
        String sql = "SELECT MONTH(order_date) as month, SUM(total_amount) as sales_amount, COUNT(*) as order_count " +
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
                    sale.put("salesAmount", rs.getBigDecimal("sales_amount"));
                    sale.put("orderCount", rs.getInt("order_count"));
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
        String sql = "SELECT p.id, p.name, SUM(oi.quantity) as total_sales, " +
                "SUM(oi.price_at_purchase * oi.quantity) as total_revenue " +
                "FROM order_item oi " +
                "JOIN product p ON oi.product_id = p.id " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_sales DESC " +
                "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", rs.getInt("id"));
                    product.put("productName", rs.getString("name"));
                    product.put("totalSales", rs.getInt("total_sales"));
                    product.put("totalRevenue", rs.getBigDecimal("total_revenue"));
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
        
        // 先获取所有订单总数
        String totalSql = "SELECT COUNT(*) as total FROM order_master";
        int totalOrders = 0;
        
        try (Connection conn = DBUtil.getConnection()) {
            // 获取总订单数
            try (PreparedStatement ps = conn.prepareStatement(totalSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalOrders = rs.getInt("total");
                }
            }
            
            // 获取各状态订单数
            String statusSql = "SELECT order_status as order_status, COUNT(*) as count " +
                    "FROM order_master " +
                    "GROUP BY order_status";
            
            try (PreparedStatement ps = conn.prepareStatement(statusSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("orderStatus", rs.getString("order_status"));
                    stat.put("count", rs.getInt("count"));
                    
                    // 计算百分比
                    double percentage = totalOrders > 0 ? (double) rs.getInt("count") / totalOrders * 100 : 0;
                    stat.put("percentage", percentage);
                    
                    statusStats.add(stat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statusStats;
    }
}