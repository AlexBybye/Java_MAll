// src/types/index.ts

// --- 认证与用户 ---
export interface AuthResponse {
  token: string;
  userId: number | null;
  username: string | null;
  isAdmin: boolean; // 添加isAdmin字段
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
    userType?: 'customer' | 'admin'; // 新增用户类型字段
  };
}

// 新增用户注册请求体类型
export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  phone: string;
  isAdmin: boolean;
}

// --- 商品与购物车 ---
export interface Product {
  id: number;
  name: string;
  price: number; // 使用 number 或 string，取决于后端序列化
  stockQuantity: number;
  description: string;
  imageUrl?: string; // 添加imageUrl字段，设为可选以保持向后兼容
}

export interface CartItem {
  cart_id: number;
  product_id: number;
  name: string;
  price: number;
  quantity: number;
  image_url: string;
  stock_quantity: number;
}

// 管理后台商品接口
export interface AdminProduct {
  id: number;
  name: string;
  price: number;
  stockQuantity: number; // 使用后端API期望的字段名
  stock?: number; // 保留stock字段以保持向后兼容
  description?: string;
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
  customerName?: string; // 新增客户名称字段，设为可选
  totalAmount: number;
  shippingAddress: string;
  orderStatus: 'PENDING' | 'PAID' | 'SHIPPED' | 'DELIVERED';
  orderDate: string; // ISO 格式日期字符串
  items: OrderItem[];
}

// 简化的订单接口（用于仪表板展示）
export interface RecentOrder {
  id: number;
  customerName: string;
  totalAmount: number;
  status: string;
  orderDate: string;
}

// 管理后台订单接口
export interface AdminOrder {
  id: number;
  customerName: string;
  totalAmount: number;
  status: string;
  orderDate: string;
  customerId?: number;
}

// --- 统计数据 ---
// 统计概览
export interface StatsOverview {
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
  pendingOrders: number;
}

// 每日销售数据
export interface DailySales {
  date: string;
  salesAmount: number;
  orderCount: number;
}

// 月度销售数据
export interface MonthlySales {
  month: string;
  salesAmount: number;
  orderCount: number;
}

// 热销商品数据
export interface TopSellingProduct {
  productId: number;
  productName: string;
  totalSales: number;
  totalRevenue: number;
}

// 订单状态统计
export interface OrderStatusStats {
  orderStatus: string;
  count: number;
  percentage: number;
}