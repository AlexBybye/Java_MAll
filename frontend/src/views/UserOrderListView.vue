<!-- d:\JavaMall\frontend\src\views\UserOrderListView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { api } from '@/services/api';
import type { OrderMaster } from '@/types';

const router = useRouter();
const authStore = useAuthStore();

// 订单列表
const orders = ref<OrderMaster[]>([]);

// 加载状态
const isLoading = ref(true);
// 错误信息
const errorMessage = ref('');

// 检查权限
onMounted(() => {
    if (!authStore.isAuthenticated) {
        router.push({ name: 'login' });
        return;
    }
    loadOrders();
});

// 加载用户订单列表
async function loadOrders() {
    try {
        isLoading.value = true;
        errorMessage.value = '';
        const response = await api.getUserOrders();
        // 修复后
        if (response.success) {
            orders.value = response.orders;
        }
        
        else {
            errorMessage.value = '加载订单失败：' + (response.message || '未知错误');
            orders.value = [];
        }
    } catch (error: any) {
        console.error('加载订单列表失败:', error);
        errorMessage.value = '加载订单失败：' + (error.message || '网络错误');
        orders.value = []; // 确保orders始终是一个数组
    } finally {
        isLoading.value = false;
    }
}

// 查看订单详情
function viewOrderDetails(orderId: number) {
    router.push({ name: 'user-order-detail', params: { id: orderId } });
}

// 获取状态显示文本
function getStatusLabel(status: string): string {
    const statusMap: Record<string, string> = {
        PENDING: '待处理',
        PAID: '已支付',
        SHIPPED: '已发货',
        DELIVERED: '已送达'
    };
    return statusMap[status] || status;
}
</script>

<template>
    <div class="user-order-list">
        <header class="order-list-header">
            <h1>我的订单</h1>
        </header>

        <div class="order-list">
            <div v-if="isLoading" class="loading">加载中...</div>
            <div v-else-if="errorMessage" class="error-message">
                {{ errorMessage }}
            </div>
            <div v-else-if="orders.length === 0" class="no-data">
                暂无订单数据
            </div>
            <div v-else class="orders-table">
                <table>
                    <thead>
                        <tr>
                            <th>订单号</th>
                            <th>总金额</th>
                            <th>订单状态</th>
                            <th>订单日期</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="order in orders" :key="order.id">
                            <td>{{ order.id }}</td>
                            <td>¥{{ order.totalAmount.toFixed(2) }}</td>
                            <td>{{ getStatusLabel(order.orderStatus) }}</td>
                            <td>{{ order.orderDate }}</td>
                            <td>
                                <button @click="viewOrderDetails(order.id)" class="detail-btn">查看详情</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<style scoped>
.user-order-list {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.order-list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.order-list {
    background-color: #f9f9f9;
    padding: 20px;
    border-radius: 8px;
}

.loading,
.no-data,
.error-message {
    text-align: center;
    padding: 40px;
    font-size: 16px;
}

.error-message {
    color: #ff4d4f;
}

.orders-table {
    overflow-x: auto;
}

table {
    width: 100%;
    border-collapse: collapse;
    background-color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

th,
td {
    padding: 12px 16px;
    text-align: left;
    border-bottom: 1px solid #e8e8e8;
}

th {
    background-color: #f0f0f0;
    font-weight: bold;
}

.detail-btn {
    padding: 6px 12px;
    background-color: #1890ff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.detail-btn:hover {
    background-color: #40a9ff;
}
</style>