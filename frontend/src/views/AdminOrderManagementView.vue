<!-- frontend\src\views\AdminOrderManagementView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { api } from '@/services/api';
import type { AdminOrder } from '@/types';

const router = useRouter();
const authStore = useAuthStore();

// 订单列表
const orders = ref<AdminOrder[]>([]);

// 加载状态
const isLoading = ref(true);

// 订单状态选项
const statusOptions = [
  { value: 'pending', label: '待处理' },
  { value: 'confirmed', label: '已确认' },
  { value: 'shipped', label: '已发货' },
  { value: 'delivered', label: '已送达' },
  { value: 'cancelled', label: '已取消' }
];

// 检查权限
onMounted(() => {
  if (!authStore.isAuthenticated || !authStore.isAdmin) {
    router.push({ name: 'login' });
    return;
  }
  loadOrders();
});

// 加载订单列表
async function loadOrders() {
  try {
    isLoading.value = true;
    const response = await api.getAllOrders();
    if (response.success) {
      // 修复：将后端返回的orderStatus字段映射为status字段
      orders.value = response.orders.map((order:any) => ({
        ...order,
        status: order.orderStatus
      }));
    }
  } catch (error) {
    console.error('加载订单列表失败:', error);
  } finally {
    isLoading.value = false;
  }
}

async function updateOrderStatus(orderId: number, newStatus: string) {
  try {
    const response = await api.updateOrderStatus(orderId, newStatus);
    if (response.success) { // 直接访问response.success
      // 重新加载订单列表
      loadOrders();
    }
  } catch (error) {
    console.error('更新订单状态失败:', error);
  }
}

// 查看订单详情
function viewOrderDetails(orderId: number) {
  router.push({ name: 'admin-order-detail', params: { id: orderId } });
}

// 返回仪表板
function goBackToDashboard() {
  router.push({ name: 'admin-dashboard' });
}

// 获取状态标签样式
function getStatusClass(status: string): string {
  const statusClasses: Record<string, string> = {
    pending: 'status-pending',
    confirmed: 'status-confirmed',
    shipped: 'status-shipped',
    delivered: 'status-delivered',
    cancelled: 'status-cancelled'
  };
  return statusClasses[status] || '';
}

// 获取状态显示文本
function getStatusLabel(status: string): string {
  const statusMap: Record<string, string> = {
    pending: '待处理',
    confirmed: '已确认',
    shipped: '已发货',
    delivered: '已送达',
    cancelled: '已取消'
  };
  return statusMap[status] || status;
}
type OrderStatus = 'pending' | 'confirmed' | 'shipped' | 'delivered' | 'cancelled';

</script>

<template>
  <div class="admin-order-management">
    <header class="management-header">
      <h1>订单管理</h1>
      <div class="header-actions">
        <button @click="goBackToDashboard" class="back-btn">返回仪表板</button>
      </div>
    </header>

    <div class="order-list">
      <div v-if="isLoading" class="loading">加载中...</div>
      <div v-else-if="orders.length === 0" class="no-data">
        暂无订单数据
      </div>
      <div v-else class="orders-table">
        <table>
          <thead>
            <tr>
              <th>订单号</th>
              <th>客户</th>
              <th>总金额</th>
              <th>状态</th>
              <th>下单时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td>{{ order.id }}</td>
              <td>{{ order.customerName }}</td>
              <td>¥{{ order.totalAmount }}</td>
              <td>
                <select :value="order.status"
                  @change="updateOrderStatus(order.id, ($event.target as HTMLSelectElement).value)"
                  :class="['status-select', getStatusClass(order.status)]">
                  <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </td>
              <td>{{ new Date(order.orderDate).toLocaleString() }}</td>
              <td class="actions">
                <button @click="viewOrderDetails(order.id)" class="view-btn">详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-order-management {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 2rem;
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.management-header h1 {
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

.order-list {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.loading,
.no-data {
  text-align: center;
  color: #666;
  padding: 2rem;
}

.orders-table {
  overflow-x: auto;
}

.orders-table table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th,
.orders-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.orders-table th {
  background-color: #f8f9fa;
  font-weight: bold;
  color: #495057;
}

.orders-table tr:hover {
  background-color: #f8f9fa;
}

.status-select {
  padding: 0.25rem 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
}

.status-pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-confirmed {
  background-color: #d1ecf1;
  color: #0c5460;
}

.status-shipped {
  background-color: #cce5ff;
  color: #004085;
}

.status-delivered {
  background-color: #d4edda;
  color: #155724;
}

.status-cancelled {
  background-color: #f8d7da;
  color: #721c24;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.view-btn {
  padding: 0.25rem 0.5rem;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.view-btn:hover {
  background-color: #2980b9;
}
</style>