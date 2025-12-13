<!-- frontend/src/views/AdminOrderDetailView.vue -->
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { api } from '@/services/api'; // 使用正确的API导入方式
import type { OrderMaster, OrderItem } from '@/types';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// 订单详情数据
const order = reactive<OrderMaster>({
  id: 0,
  customerId: 0,
  totalAmount: 0,
  shippingAddress: '',
  orderStatus: 'PENDING',
  orderDate: '',
  items: []
});

// 加载状态
const isLoading = ref(true);
// 错误信息
const errorMessage = ref('');

// 获取订单ID
const orderId = parseInt(route.params.id as string);

// 检查权限
onMounted(() => {
  if (!authStore.isAuthenticated || !authStore.isAdmin) {
    router.push({ name: 'login' });
    return;
  }
  loadOrderDetail();
});
// 加载订单详情
async function loadOrderDetail() {
  try {
    isLoading.value = true;
    const response = await api.getOrderDetail(orderId); // 使用统一API方法
    if (response.success) { // 直接访问response.success
      // 将后端返回的订单数据赋值给order对象
      Object.assign(order, response.order); // 正确获取response.order
    }
  } catch (error: any) {
    errorMessage.value = '加载订单详情失败: ' + (error.response?.data?.message || error.message);
    console.error('加载订单详情失败:', error);
  } finally {
    isLoading.value = false;
  }
}

// 返回订单列表
function goBackToOrders() {
  router.push({ name: 'admin-orders' });
}

// 获取状态显示文本
function getStatusLabel(status: string): string {
  const statusMap: Record<string, string> = {
    PENDING: '待处理',
    PAID: '已支付',
    SHIPPED: '已发货',
    DELIVERED: '已送达',
    CANCELLED: '已取消',
    CONFIRMED: '已确认'
  };
  return statusMap[status] || status;
}
</script>

<template>
  <div class="admin-order-detail">
    <header class="detail-header">
      <h1>订单详情</h1>
      <div class="header-actions">
        <button @click="goBackToOrders" class="back-btn">返回订单列表</button>
      </div>
    </header>

    <div v-if="isLoading" class="loading">加载中...</div>
    <div v-else-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <div v-else class="order-detail-content">
      <!-- 订单基本信息 -->
      <div class="order-info">
        <h2>订单信息</h2>
        <div class="info-grid">
          <div class="info-item">
            <label>订单号：</label>
            <span>{{ order.id }}</span>
          </div>
          <div class="info-item">
            <label>客户ID：</label>
            <span>{{ order.customerId }}</span>
          </div>
          <div class="info-item">
            <label>总金额：</label>
            <span class="amount">¥{{ order.totalAmount }}</span>
          </div>
          <div class="info-item">
            <label>订单状态：</label>
            <span class="status">{{ getStatusLabel(order.orderStatus) }}</span>
          </div>
          <div class="info-item">
            <label>收货地址：</label>
            <span>{{ order.shippingAddress }}</span>
          </div>
          <div class="info-item">
            <label>下单时间：</label>
            <span>{{ new Date(order.orderDate).toLocaleString() }}</span>
          </div>
        </div>
      </div>

      <!-- 订单商品列表 -->
      <div class="order-items">
        <h2>商品列表</h2>
        <div class="items-table">
          <table>
            <thead>
              <tr>
                <th>商品ID</th>
                <th>商品名称</th>
                <th>购买价格</th>
                <th>数量</th>
                <th>小计</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in order.items" :key="item.id">
                <td>{{ item.productId }}</td>
                <td>{{ item.productName }}</td>
                <td>¥{{ item.priceAtPurchase }}</td>
                <td>{{ item.quantity }}</td>
                <td>¥{{ (item.priceAtPurchase * item.quantity).toFixed(2) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-order-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 2rem;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.detail-header h1 {
  margin: 0;
  color: #2c3e50;
}

.back-btn {
  padding: 0.5rem 1rem;
  background-color: #95a5a6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.back-btn:hover {
  background-color: #7f8c8d;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.order-detail-content {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.order-info h2, .order-items h2 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #2c3e50;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.5rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item label {
  font-weight: bold;
  color: #555;
  margin-bottom: 0.25rem;
}

.info-item span {
  color: #333;
}

.info-item .amount {
  font-size: 1.25rem;
  color: #e74c3c;
  font-weight: bold;
}

.info-item .status {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  background-color: #d1ecf1;
  color: #0c5460;
  font-weight: bold;
  display: inline-block;
  width: fit-content;
}

.items-table {
  overflow-x: auto;
}

.items-table table {
  width: 100%;
  border-collapse: collapse;
}

.items-table th, .items-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.items-table th {
  background-color: #f8f9fa;
  font-weight: bold;
  color: #495057;
}

.items-table tr:hover {
  background-color: #f8f9fa;
}
</style>