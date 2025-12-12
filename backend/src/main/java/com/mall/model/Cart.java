package com.mall.model;

import java.util.Date;

/**
 * 购物车实体类 (映射 cart 表)
 */
public class Cart {
    private int id;
    private int customerId;
    private int productId;
    private int quantity;
    private Date createdAt;

    // 此外，为了前端展示，我们通常会包含 Product 对象的完整信息

    // 完整的 Getter/Setter (请在 IDEA 中自动生成)
    // ...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}