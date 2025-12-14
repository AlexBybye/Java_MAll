package com.mall.dao;

import com.mall.model.OrderMaster;
import com.mall.model.OrderItem;
import com.mall.util.DBUtil;
import java.sql.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 订单数据访问对象 (Order DAO)
 * 核心方法：createOrder() 涉及数据库事务
 */
public class OrderDAO {

    // private final ProductDAO productDAO = new ProductDAO(); // 冗余：未在代码中使用，可以删除
    // private final CartDAO cartDAO = new CartDAO(); // 冗余：在 createOrder 中已替换为直接 SQL 操作，可以删除

    /**
     * 创建订单的核心方法：包含事务处理，确保数据一致性。
     */
    public int createOrder(OrderMaster order, List<OrderItem> items, List<Integer> cartItemIds) {

        Connection conn = null;
        int orderId = -1;

        System.out.println("[DAO T0] OrderDAO.createOrder 开始执行。");

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // ⭐ 开启事务
            System.out.println("[DAO T1] 事务已开启，开始步骤 1: 插入 order_master...");

            // --- 步骤 1: 插入订单主表 (order_master) ---
            String masterSql = "INSERT INTO order_master (customer_id, total_amount, shipping_address, order_status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement masterPs = conn.prepareStatement(masterSql, Statement.RETURN_GENERATED_KEYS)) {
                masterPs.setInt(1, order.getCustomerId());
                masterPs.setBigDecimal(2, order.getTotalAmount());
                masterPs.setString(3, order.getShippingAddress());
                masterPs.setString(4, "PENDING");

                if (masterPs.executeUpdate() == 0) {
                    throw new SQLException("创建订单主表失败。");
                }

                try (ResultSet rs = masterPs.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    } else {
                        throw new SQLException("获取订单ID失败。");
                    }
                }
            }
            System.out.println("[DAO T2] 步骤 1 完成。订单ID: " + orderId + "。开始步骤 2: 插入 order_item...");


            // --- 步骤 2: 插入订单详情表 (order_item) ---
            String itemSql = "INSERT INTO order_item (order_id, product_id, product_name, price_at_purchase, quantity) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement itemPs = conn.prepareStatement(itemSql)) {
                for (OrderItem item : items) {
                    itemPs.setInt(1, orderId);
                    itemPs.setInt(2, item.getProductId());
                    itemPs.setString(3, item.getProductName());
                    itemPs.setBigDecimal(4, item.getPriceAtOrder());
                    itemPs.setInt(5, item.getQuantity());
                    itemPs.addBatch();
                }
                itemPs.executeBatch();
            }
            System.out.println("[DAO T3] 步骤 2 完成。开始步骤 3: 扣减库存...");

            // --- 步骤 3: 扣减商品库存 (product) ---
            String stockSql = "UPDATE product SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
            try (PreparedStatement stockPs = conn.prepareStatement(stockSql)) {
                for (OrderItem item : items) {
                    stockPs.setInt(1, item.getQuantity());
                    stockPs.setInt(2, item.getProductId());
                    stockPs.setInt(3, item.getQuantity());
                    stockPs.addBatch();
                }

                int[] updateCounts = stockPs.executeBatch();
                for (int count : updateCounts) {
                    if (count == 0) {
                        // 如果某次更新影响行数为0，则可能是库存不足或商品ID错误。
                        throw new SQLException("库存不足或商品不存在，操作失败。");
                    }
                }
            }
            System.out.println("[DAO T4] 步骤 3 完成。开始步骤 4: 清空购物车...");


            // --- 步骤 4: 清空购物车项 (已修复：使用当前连接进行批处理删除，确保事务原子性) ---
            if (!cartItemIds.isEmpty()) {
                String deleteCartSql = "DELETE FROM cart WHERE id = ?";
                try (PreparedStatement deleteCartPs = conn.prepareStatement(deleteCartSql)) {
                    for (int cartId : cartItemIds) {
                        deleteCartPs.setInt(1, cartId);
                        deleteCartPs.addBatch();
                    }

                    int[] deleteCounts = deleteCartPs.executeBatch();
                    for (int count : deleteCounts) {
                        if (count == 0) {
                            // 理论上 cartItemIds 来自数据库，应该都能删除。如果不能，则抛出异常回滚。
                            throw new SQLException("清理购物车项失败: 购物车项可能已被移除或ID错误。");
                        }
                    }
                }
            }
            System.out.println("[DAO T5] 步骤 4 完成。尝试提交事务...");


            conn.commit(); // ⭐ 事务提交
            System.out.println("[DAO T6] 事务提交成功。返回订单ID。");
            return orderId;

        } catch (SQLException e) {
            // ⭐ 强制日志和回滚加固
            e.printStackTrace();

            System.err.println("❌ 订单创建事务失败！尝试回滚...");

            if (conn != null) {
                try {
                    conn.rollback(); // 发生异常时事务回滚
                    System.err.println("✅ 事务回滚成功。");
                } catch (SQLException ex) {
                    System.err.println("⚠️ 警告：事务回滚失败！");
                    ex.printStackTrace();
                }
            }

            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取订单的订单项列表 (DO NOT CHANGE)
     */
    public List<Map<String, Object>> getOrderItemsByOrderId(int orderId) {
        // ... (方法体不变)
        List<Map<String, Object>> items = new ArrayList<>();
        String sql = "SELECT oi.item_id, oi.order_id, oi.product_id, p.name as product_name, oi.price_at_purchase, oi.quantity " +
                "FROM order_item oi JOIN product p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("item_id"));
                    item.put("orderId", rs.getInt("order_id"));
                    item.put("productId", rs.getInt("product_id"));
                    item.put("productName", rs.getString("product_name"));
                    item.put("priceAtPurchase", rs.getBigDecimal("price_at_purchase"));
                    item.put("quantity", rs.getInt("quantity"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * 获取用户的订单列表（包含订单项） (DO NOT CHANGE)
     */
    public List<Map<String, Object>> getOrdersByCustomerId(int customerId) {
        // ... (方法体不变)
        List<Map<String, Object>> orders = new ArrayList<>();
        String sql = "SELECT om.order_id, om.customer_id, om.total_amount, om.shipping_address, om.order_status, om.order_date " +
                "FROM order_master om WHERE om.customer_id = ? ORDER BY om.order_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> order = new HashMap<>();
                    order.put("id", rs.getInt("order_id"));
                    order.put("customerId", rs.getInt("customer_id"));
                    order.put("totalAmount", rs.getBigDecimal("total_amount"));
                    order.put("shippingAddress", rs.getString("shipping_address"));
                    order.put("orderStatus", rs.getString("order_status"));
                    order.put("orderDate", rs.getTimestamp("order_date"));
                    order.put("items", getOrderItemsByOrderId(rs.getInt("order_id")));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * 获取订单详情（包含订单项） (DO NOT CHANGE)
     */
    public Map<String, Object> getOrderById(int orderId) {
        // ... (方法体不变)
        String sql = "SELECT om.order_id, om.customer_id, COALESCE(c.username, CONCAT('Customer ', om.customer_id)) as customer_name, " +
                "om.total_amount, om.shipping_address, om.order_status, om.order_date " +
                "FROM order_master om JOIN customer c ON om.customer_id = c.id " +
                "WHERE om.order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> order = new HashMap<>();
                    order.put("id", rs.getInt("order_id"));
                    order.put("customerId", rs.getInt("customer_id"));
                    order.put("customerName", rs.getString("customer_name"));
                    order.put("totalAmount", rs.getBigDecimal("total_amount"));
                    order.put("shippingAddress", rs.getString("shipping_address"));
                    order.put("orderStatus", rs.getString("order_status"));
                    order.put("orderDate", rs.getTimestamp("order_date"));
                    order.put("items", getOrderItemsByOrderId(orderId));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新订单状态 (DO NOT CHANGE)
     */
    public boolean updateOrderStatus(int orderId, String status) {
        // ... (方法体不变)
        String sql = "UPDATE order_master SET order_status = ? WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取所有订单（包含客户名称和订单项）
     * 仅管理员可用
     * * [FIX]: 补充了缺失的方法签名和局部变量初始化。
     */
    public List<Map<String, Object>> getAllOrders() {
        List<Map<String, Object>> orders = new ArrayList<>();
        String sql = "SELECT om.order_id, om.customer_id, COALESCE(c.username, CONCAT('Customer ', om.customer_id)) as customer_name, " +
                "om.total_amount, om.shipping_address, om.order_status, om.order_date " +
                "FROM order_master om " +
                "JOIN customer c ON om.customer_id = c.id " +
                "ORDER BY om.order_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", rs.getInt("order_id"));
                order.put("customerId", rs.getInt("customer_id"));
                order.put("customerName", rs.getString("customer_name")); // 添加客户名称
                order.put("totalAmount", rs.getBigDecimal("total_amount"));
                order.put("shippingAddress", rs.getString("shipping_address"));
                order.put("orderStatus", rs.getString("order_status"));
                order.put("orderDate", rs.getTimestamp("order_date"));
                order.put("items", getOrderItemsByOrderId(rs.getInt("order_id")));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * 删除订单（包含事务处理，确保数据一致性）
     * @param orderId 订单ID
     * @param customerId 客户ID（用于验证权限）
     * @param isAdmin 是否为管理员
     * @return 是否删除成功
     */
    public boolean deleteOrder(int orderId, int customerId, boolean isAdmin) {
        // ... (方法体不变)
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 开启事务

            // 1. 验证订单是否存在且属于该用户或管理员
            String checkSql = "SELECT customer_id FROM order_master WHERE order_id = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, orderId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        // conn.rollback(); // 此处不需要回滚，因为事务还未执行写操作
                        return false; // 订单不存在
                    }
                    int dbCustomerId = rs.getInt("customer_id");
                    if (dbCustomerId != customerId && !isAdmin) {
                        // conn.rollback(); // 此处不需要回滚
                        return false; // 无权限删除该订单
                    }
                }
            }

            // 2. 删除订单项
            String deleteItemsSql = "DELETE FROM order_item WHERE order_id = ?";
            try (PreparedStatement deleteItemsPs = conn.prepareStatement(deleteItemsSql)) {
                deleteItemsPs.setInt(1, orderId);
                deleteItemsPs.executeUpdate();
            }

            // 3. 删除订单主表记录
            String deleteOrderSql = "DELETE FROM order_master WHERE order_id = ?";
            try (PreparedStatement deleteOrderPs = conn.prepareStatement(deleteOrderSql)) {
                deleteOrderPs.setInt(1, orderId);
                int rowsAffected = deleteOrderPs.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit(); // 事务提交
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // 发生异常时事务回滚
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}