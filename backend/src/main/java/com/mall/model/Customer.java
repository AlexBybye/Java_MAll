package com.mall.model;
/**
 * 顾客实体类 (映射 customer 表)
 */
public class Customer {
    private int id;
    private String username;
    private String password; // 实际项目中应加密
    private String email;
    private String phone;
    // created_at 字段

    // 构造函数
    public Customer() {}

    // 完整的构造函数
    public Customer(String username, String password, String email, String phone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    // ⭐ 重要：在 IDEA 中，使用 Alt + Insert (或右键 -> Generate...)
    //    为所有 private 字段自动生成 Getter 和 Setter 方法。

    // --- Getter and Setter Methods ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    // ---------------------------------
}