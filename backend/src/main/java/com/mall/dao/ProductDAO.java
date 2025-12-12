package com.mall.dao;

import com.mall.model.Product;
import com.mall.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品数据访问对象 (Product DAO)
 */
public class ProductDAO {

    // --- 1. 创建商品 (Create) ---
    public boolean createProduct(Product product) {
        String sql = "INSERT INTO product (name, description, price, stock_quantity, image_url) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setString(5, product.getImageUrl());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 2. 获取所有商品 (Read - All) ---
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        // 仅查询未被软删除的商品
        String sql = "SELECT * FROM product WHERE is_deleted = 0 ORDER BY id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // --- 3. 获取单个商品 (Read - Single) ---
    public Product getProductById(int id) {
        String sql = "SELECT * FROM product WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractProductFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- 4. 更新商品 (Update) ---
    public boolean updateProduct(Product product) {
        String sql = "UPDATE product SET name = ?, description = ?, price = ?, stock_quantity = ?, image_url = ? WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setString(5, product.getImageUrl());
            ps.setInt(6, product.getId()); // WHERE 子句

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 5. 删除商品 (Delete - 软删除) ---
    public boolean deleteProduct(int id) {
        // 使用软删除 (is_deleted = 1) 而不是物理删除，方便数据恢复和历史订单查询
        String sql = "UPDATE product SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 辅助方法：从 ResultSet 中提取 Product 对象
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setImageUrl(rs.getString("image_url"));
        product.setDeleted(rs.getBoolean("is_deleted"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setUpdatedAt(rs.getTimestamp("updated_at"));
        return product;
    }
}