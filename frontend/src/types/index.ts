// src/types/index.ts

// --- 认证与用户 ---
export interface AuthResponse {
  token: string;
  userId: number | null;
  username: string | null;
}

//后端 Login API 实际返回的嵌套结构
export interface LoginApiResponseBody {
  success: boolean;
  message: string;
  token: string;
  user?: { // 使用可选链，尽管后端目前是必有的
    id: number;
    username: string;
    email: string;
  };

}

// --- 商品与购物车 ---
export interface Product {
  id: number;
  name: string;
  price: number; // 使用 number 或 string，取决于后端序列化
  stockQuantity: number;
  description: string;
}

export interface CartItem {
  cart_id: number;
  product_id: number;
  name: string;
  price: number;
  quantity: number;
  total: number;
}

// --- 订单 ---
export interface OrderItem {
  id: number;
  orderId: number;
  productId: number;
  productName: string;
  priceAtPurchase: number;
  quantity: number;
}

export interface OrderMaster {
  id: number;
  customerId: number;
  totalAmount: number;
  shippingAddress: string;
  orderStatus: 'PENDING' | 'PAID' | 'SHIPPED' | 'DELIVERED';
  orderDate: string; // ISO 格式日期字符串
  items: OrderItem[];
}