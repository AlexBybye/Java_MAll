package com.mall.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List; // 包含订单详情

/**
 * 订单主表实体类
 */
public class OrderMaster {
    private int id;
    private int customerId;
    private Date orderDate;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String status; // 例如：PENDING, PAID, SHIPPED

    // 组合关系：一个订单包含多个订单项
    private List<OrderItem> items;

    // 完整的 Getter/Setter (请在 IDEA 中自动生成)
    // ...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}