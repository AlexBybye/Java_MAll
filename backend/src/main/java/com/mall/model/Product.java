package com.mall.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品实体类 (映射 product 表)
 */
public class Product {
    private int id;
    private String name;
    private String description;
    private BigDecimal price; // 使用 BigDecimal 处理货币，避免浮点数精度问题
    private int stockQuantity;
    private String imageUrl;
    private boolean isDeleted; // 对应数据库的 TINYINT(1)
    private Date createdAt;
    private Date updatedAt;

    // 1. 构造函数（使用 Alt + Insert 自动生成默认构造和全参数构造）
    public Product() {}

    // 2. 完整 Getter/Setter (省略部分，请在 IDEA 中自动生成)

    // --- Getter and Setter Methods ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    // ---------------------------------
}