package com.mall.model;

import java.math.BigDecimal;

/**
 * 订单详情实体类
 */
public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private String productName;
    private BigDecimal priceAtOrder; // 下单时的价格
    private int quantity;

    // 完整的 Getter/Setter (请在 IDEA 中自动生成)
    // ...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPriceAtOrder() { return priceAtOrder; }
    public void setPriceAtOrder(BigDecimal priceAtOrder) { this.priceAtOrder = priceAtOrder; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}