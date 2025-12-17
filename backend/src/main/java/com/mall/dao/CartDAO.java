package com.mall.dao;

import com.mall.model.Cart;
import com.mall.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CartDAO {

    // --- 1. 添加或更新购物车项 (Add/Update) --- 
    public boolean addOrUpdateCartItem(int customerId, int productId, int quantity) {
        // 检查商品库存
        int stockQuantity = getProductStockQuantity(productId);
        if (stockQuantity <= 0) {
            return false;
        }
        
        // 获取购物车中该商品的当前数量
        int currentQuantity = getCartItemQuantity(customerId, productId);
        
        // 检查添加后的数量是否超过库存
        if (currentQuantity + quantity > stockQuantity) {
            return false;
        }
        
        // 使用 REPLACE INTO 或 ON DUPLICATE KEY UPDATE 语句简化逻辑
        // 我们选择 ON DUPLICATE KEY UPDATE，更灵活地处理数量
        String sql = "INSERT INTO cart (customer_id, product_id, quantity) VALUES (?, ?, ?)" + 
                     " ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity); // 传入的数量
            
            // executeUpdate() 会返回受影响的行数，插入返回1，更新返回2
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 2. 获取用户购物车内容 (Read) ---
    // 为了前端展示，我们需要 JOIN 商品信息
    public List<Map<String, Object>> getCartItemsByCustomerId(int customerId) {
        // 我们返回 List<Map> 而不是 List<Cart>，因为包含了 JOIN 的 product 信息
        List<Map<String, Object>> cartItems = new java.util.ArrayList<>();
        String sql = "SELECT c.id AS cart_id, c.quantity, p.id AS product_id, p.name, p.price, p.image_url, p.stock_quantity "
                + "FROM cart c JOIN product p ON c.product_id = p.id "
                + "WHERE c.customer_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("cart_id", rs.getInt("cart_id"));
                    item.put("quantity", rs.getInt("quantity"));
                    item.put("product_id", rs.getInt("product_id"));
                    item.put("name", rs.getString("name"));
                    item.put("price", rs.getBigDecimal("price"));
                    item.put("image_url", rs.getString("image_url"));
                    item.put("stock_quantity", rs.getInt("stock_quantity")); // 方便前端判断是否超卖
                    cartItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartItems;
    }

    // --- 3. 更新购物车数量 (Update Quantity) ---
    public boolean updateCartQuantity(int cartId, int newQuantity) {
        String sql = "UPDATE cart SET quantity = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQuantity);
            ps.setInt(2, cartId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 4. 移除购物车项 (Delete) ---
    public boolean removeCartItem(int cartId) {
        String sql = "DELETE FROM cart WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cartId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取商品的库存数量
    private int getProductStockQuantity(int productId) {
        String sql = "SELECT stock_quantity FROM product WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // 获取购物车中某商品的当前数量
    private int getCartItemQuantity(int customerId, int productId) {
        String sql = "SELECT quantity FROM cart WHERE customer_id = ? AND product_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}