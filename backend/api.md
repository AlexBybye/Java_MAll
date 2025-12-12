# 📚 Mall System API 文档 (V1.0)

所有接口的基础 URL：`{{baseUrl}}` (例如: `http://localhost:8080/api`)

---

## 🔑 认证要求

* 所有受保护的接口（除注册和登录外）都需要在 HTTP 请求头中包含有效的 JWT Token：
    * **Header Key:** `Authorization`
    * **Header Value:** `Bearer [YOUR_JWT_TOKEN]`

---

## I. 用户认证模块 (Auth)

| 编号 | 接口描述 | 方法 | URL | 认证 |
| :--- | :--- | :--- | :--- | :--- |
| **A** | **注册新用户** | `POST` | `/api/auth/register` | 否 |
| **B** | **用户登录** | `POST` | `/api/auth/login` | 否 |

### A. 注册新用户

* **请求体 (JSON):**
    ```json
    {
        "username": "newuser",
        "password": "password123",
        "email": "user@example.com"
    }
    ```
* **成功响应 (201 Created):**
    ```json
    {
        "success": true,
        "message": "注册成功"
    }
    ```

### B. 用户登录

* **请求体 (JSON):**
    ```json
    {
        "username": "testuser",
        "password": "password123"
    }
    ```
* **成功响应 (200 OK):**
    ```json
    {
        "token": "eyJhbGciOiJIUzI1NiIsInR5c...",
        "userId": 1,
        "username": "testuser"
    }
    ```

---

## II. 商品模块 (Product)

| 编号 | 接口描述 | 方法 | URL | 认证 |
| :--- | :--- | :--- | :--- | :--- |
| **C** | **获取所有商品** | `GET` | `/api/products` | 否 |
| **D** | **获取商品详情** | `GET` | `/api/products/{id}` | 否 |

### C. 获取所有商品

* **成功响应 (200 OK) - 示例:**
    ```json
    [
      {
        "id": 1,
        "name": "测试商品A",
        "price": 50.00,
        "stockQuantity": 98,
        "description": "这是商品A的描述"
      }
      // ... 更多商品
    ]
    ```

---

## III. 购物车模块 (Cart)

| 编号 | 接口描述 | 方法 | URL | 认证 |
| :--- | :--- | :--- | :--- | :--- |
| **E** | **添加商品到购物车** | `POST` | `/api/cart` | 是 |
| **F** | **获取购物车列表** | `GET` | `/api/cart` | 是 |
| **G** | **移除购物车项** | `DELETE` | `/api/cart/{cart_id}` | 是 |

### E. 添加商品到购物车

* **请求体 (JSON):**
    ```json
    {
        "productId": 1,
        "quantity": 2
    }
    ```
* **成功响应 (200 OK):**
    ```json
    {
        "success": true,
        "message": "商品已添加到购物车"
    }
    ```

### F. 获取购物车列表

* **成功响应 (200 OK) - 示例:**
    ```json
    [
      {
        "cart_id": 6,             
        "product_id": 1,
        "name": "测试商品A",
        "price": 50.00,
        "quantity": 2,
        "total": 100.00
      }
    ]
    ```

### G. 移除购物车项

* **成功响应 (200 OK):**
    ```json
    {
        "success": true,
        "message": "购物车项已移除"
    }
    ```

---

## IV. 订单模块 (Order)

| 编号 | 接口描述 | 方法 | URL | 认证 |
| :--- | :--- | :--- | :--- | :--- |
| **H** | **创建新订单 (事务)** | `POST` | `/api/order` | 是 |
| **I** | **获取用户所有订单** | `GET` | `/api/order` | 是 |
| **J** | **获取订单详情** | `GET` | `/api/order/{orderId}` | 是 |

### H. 创建新订单 (事务)

* **请求体 (JSON):**
    ```json
    {
        "shippingAddress": "北京市海淀区中关村大街1号",
        "cartItemIds": [6] // 要购买的购物车项的 ID 列表
    }
    ```
* **成功响应 (201 Created):**
    ```json
    {
        "success": true,
        "orderId": 4,
        "message": "订单创建成功（已隔离辅助功能）。"
    }
    ```

### I. 获取用户所有订单

* **成功响应 (200 OK) - 示例:**
    ```json
    [
      {
        "id": 4,
        "customerId": 1,
        "totalAmount": 100.00,
        "shippingAddress": "北京市海淀区中关村测试点",
        "orderStatus": "PENDING",
        "orderDate": "Dec 13, 2025, 4:18:36 AM",
        "items": [
          // 包含 OrderItem 列表
        ]
      },
      // ... 更多订单
    ]
    ```

### J. 获取订单详情

* **成功响应 (200 OK) - 示例:**
    ```json
    {
      "success": true,
      "order": {
        "id": 4,
        "customerId": 1,
        "totalAmount": 100.00,
        "shippingAddress": "北京市海淀区中关村测试点",
        "orderStatus": "PENDING",
        "orderDate": "Dec 13, 2025, 4:18:36 AM",
        "items": [
          {
            "id": 3,
            "orderId": 4,
            "productId": 1,
            "productName": "测试商品A - 订单核心测试",
            "priceAtPurchase": 50.00,
            "quantity": 2
          }
        ]
      }
    }
    ```