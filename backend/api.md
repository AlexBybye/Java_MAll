# JavaMall后端API文档

## 1. 认证模块 (Authentication)

### 1.1 注册新用户
- **URL**: `POST /api/register`
- **请求体**:
  ```json
  {
    "username": "testuser",
    "password": "testpassword123",
    "email": "testuser@example.com",
    "phone": "13800000001"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "注册成功！请登录。"
  }
  ```

### 1.2 用户登录
- **URL**: `POST /api/login`
- **请求体**:
  ```json
  {
    "username": "testuser",
    "password": "testpassword123"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "登录成功！",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "testuser",
    "isAdmin": false
  }
  ```

## 2. 用户管理模块 (Customer Management)

### 2.1 获取用户个人资料
- **URL**: `GET /api/customer/profile`
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
  ```json
  {
    "success": true,
    "message": "成功获取用户信息。",
    "user_profile": {
      "id": "1",
      "username": "testuser",
      "info": "这是需要登录才能查看的个人资料。"
    }
  }
  ```

### 2.2 更新用户个人资料
- **URL**: `PUT /api/customer/profile`
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
  ```json
  {
    "email": "updated_testuser@example.com",
    "phone": "13800000002"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "用户资料更新成功。"
  }
  ```

## 3. 产品管理模块 (Product Management)

### 3.1 获取产品列表
- **URL**: `GET /api/product`
- **响应**:
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "name": "产品名称",
        "description": "产品描述",
        "price": 99.99,
        "stockQuantity": 100,
        "imageUrl": "http://example.com/image.jpg"
      }
    ]
  }
  ```

### 3.2 获取单个产品详情
- **URL**: `GET /api/product/{id}`
- **响应**:
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "name": "产品名称",
      "description": "产品描述",
      "price": 99.99,
      "stockQuantity": 100,
      "imageUrl": "http://example.com/image.jpg"
    }
  }
  ```

### 3.3 添加新产品 (管理员权限)
- **URL**: `POST /api/product`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **请求体**:
  ```json
  {
    "name": "测试产品",
    "description": "产品描述",
    "price": 3999.00,
    "stockQuantity": 100,
    "imageUrl": "http://example.com/phone.jpg"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "商品添加成功。"
  }
  ```

### 3.4 更新产品 (管理员权限)
- **URL**: `PUT /api/product/{id}`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **请求体**:
  ```json
  {
    "name": "更新后的产品名称",
    "description": "更新后的产品描述",
    "price": 4299.00,
    "stockQuantity": 150
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "商品更新成功。"
  }
  ```

### 3.5 删除产品 (管理员权限)
- **URL**: `DELETE /api/product/{id}`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **响应**:
  ```json
  {
    "success": true,
    "message": "商品删除成功。"
  }
  ```

## 4. 购物车模块 (Cart Management)

### 4.1 获取购物车内容
- **URL**: `GET /api/cart`
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
  ```json
  {
    "success": true,
    "data": [
      {
        "cart_id": 1,
        "product_id": 1,
        "name": "产品名称",
        "price": 99.99,
        "quantity": 2
      }
    ]
  }
  ```

### 4.2 添加/更新购物车项
- **URL**: `POST /api/cart`
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
  ```json
  {
    "productId": 1,
    "quantity": 2
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "商品已加入购物车或数量已更新。"
  }
  ```

### 4.3 更新购物车项数量
- **URL**: `PUT /api/cart/{cartId}`
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
  ```json
  {
    "quantity": 3
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "购物车项数量更新成功。"
  }
  ```

### 4.4 删除购物车项
- **URL**: `DELETE /api/cart/{cartId}`
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
  ```json
  {
    "success": true,
    "message": "购物车项移除成功。"
  }
  ```

## 5. 订单模块 (Order Management)

### 5.1 创建订单
- **URL**: `POST /api/order`
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
  ```json
  {
    "shippingAddress": "北京市海淀区中关村大街1号",
    "cartItemIds": [12]
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "orderId": 1,
    "message": "订单创建成功。"
  }
  ```

### 5.2 获取用户订单列表
- **URL**: `GET /api/order`
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
  ```json
  {
    "success": true,
    "orders": [
      {
        "orderId": 1,
        "customerId": 1,
        "totalAmount": 199.98,
        "orderStatus": "PENDING",
        "orderDate": "2024-01-01T10:00:00"
      }
    ]
  }
  ```

### 5.3 获取单个订单详情
- **URL**: `GET /api/order/{id}`
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
  ```json
  {
    "success": true,
    "order": {
      "orderId": 1,
      "customerId": 1,
      "shippingAddress": "北京市海淀区中关村大街1号",
      "totalAmount": 199.98,
      "orderStatus": "PENDING",
      "orderDate": "2024-01-01T10:00:00",
      "orderItems": [
        {
          "productId": 1,
          "productName": "产品名称",
          "priceAtOrder": 99.99,
          "quantity": 2
        }
      ]
    }
  }
  ```

### 5.4 获取所有订单 (管理员权限)
- **URL**: `GET /api/order/all`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **响应**:
  ```json
  {
    "success": true,
    "orders": [
      {
        "orderId": 1,
        "customerId": 1,
        "totalAmount": 199.98,
        "orderStatus": "PENDING",
        "orderDate": "2024-01-01T10:00:00"
      }
    ]
  }
  ```

### 5.5 更新订单状态 (管理员权限)
- **URL**: `PUT /api/order/{id}/status`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **请求体**:
  ```json
  {
    "status": "SHIPPED"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "订单状态更新成功。"
  }
  ```

### 5.6 删除订单
- **URL**: `DELETE /api/order/{id}`
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
  ```json
  {
    "success": true,
    "message": "订单删除成功。"
  }
  ```

## 6. 统计模块 (Statistics)

### 6.1 获取所有统计数据 (管理员权限)
- **URL**: `GET /api/stats`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **响应**:
  ```json
  {
    "success": true,
    "data": {
      "dailySales": [...],
      "monthlySales": [...],
      "topSellingProducts": [...],
      "orderStatusStats": [...]
    }
  }
  ```

### 6.2 获取每日销售额 (管理员权限)
- **URL**: `GET /api/stats/daily?startDate=2025-01-01&endDate=2025-12-31`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **参数**:
  - `startDate`: 开始日期 (格式: YYYY-MM-DD)
  - `endDate`: 结束日期 (格式: YYYY-MM-DD)
- **响应**:
  ```json
  {
    "success": true,
    "data": [
      {
        "date": "2025-01-01",
        "salesAmount": 1000.00,
        "orderCount": 5
      }
    ]
  }
  ```

### 6.3 获取月度销售额 (管理员权限)
- **URL**: `GET /api/stats/monthly?year=2025`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **参数**:
  - `year`: 年份 (格式: YYYY)
- **响应**:
  ```json
  {
    "success": true,
    "data": [
      {
        "month": "2025-01",
        "salesAmount": 30000.00,
        "orderCount": 150
      }
    ]
  }
  ```

### 6.4 获取热销商品 (管理员权限)
- **URL**: `GET /api/stats/top-products?limit=10`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **参数**:
  - `limit`: 返回数量限制 (默认: 10, 最大: 100)
- **响应**:
  ```json
  {
    "success": true,
    "data": [
      {
        "productId": 1,
        "productName": "产品名称",
        "totalSales": 100,
        "totalRevenue": 9999.00
      }
    ]
  }
  ```

### 6.5 获取订单状态统计 (管理员权限)
- **URL**: `GET /api/stats/status`
- **请求头**: `Authorization: Bearer {token}` (需管理员权限)
- **响应**:
  ```json
  {
    "success": true,
    "data": [
      {
        "orderStatus": "PENDING",
        "count": 10,
        "percentage": 20.0
      }
    ]
  }
  ```

## 认证与授权

- 大部分API需要通过JWT Token进行认证
- Token应包含在请求头中: `Authorization: Bearer {token}`
- 管理员权限的API需要`isAdmin=true`
- 产品的GET请求不需要认证
- 购物车和订单相关的API都需要用户登录

## 错误响应格式

所有API的错误响应格式统一为:

```json
{
  "success": false,
  "message": "错误信息描述"
}
```

## 状态码说明

- `200 OK`: 请求成功
- `201 Created`: 创建资源成功
- `204 No Content`: 删除资源成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 未登录或Token无效
- `403 Forbidden`: 权限不足
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器内部错误

## 跨域支持

- 支持CORS，允许的前端地址: `http://localhost:5173`
- 允许的请求方法: `POST, GET, PUT, DELETE, OPTIONS`
- 允许的请求头: `Content-Type, Authorization`
        