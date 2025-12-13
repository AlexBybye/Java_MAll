<!-- d:\JavaMall\frontend\src\views\UserOrderDetailView.vue -->
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import {api }from '@/services/api'; // 修改这一行
import type { OrderMaster } from '@/types';

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
    if (!authStore.isAuthenticated) {
        router.push({ name: 'login' });
        return;
    }
    loadOrderDetail();
});

// 加载订单详情
async function loadOrderDetail() {
    try {
        isLoading.value = true;
        const response = await api.getOrderDetail(orderId); // 修改这一行
        if (response.success) {
            // 将后端返回的订单数据赋值给order对象
            Object.assign(order, response.order);
        }
    } catch (error: any) {
        errorMessage.value = '加载订单详情失败: ' + (error.message || '未知错误');
        console.error('加载订单详情失败:', error);
    } finally {
        isLoading.value = false;
    }
}

// 返回订单列表
function goBackToOrders() {
    router.push({ name: 'user-orders' });
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
    <div class="user-order-detail">
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
                        <label>总金额：</label>
                        <span>¥{{ order.totalAmount }}</span>
                    </div>
                    <div class="info-item">
                        <label>订单状态：</label>
                        <span :class="['status-badge', order.orderStatus.toLowerCase()]">
                            {{ getStatusLabel(order.orderStatus) }}
                        </span>
                    </div>
                    <div class="info-item">
                        <label>下单时间：</label>
                        <span>{{ new Date(order.orderDate).toLocaleString() }}</span>
                    </div>
                    <div class="info-item">
                        <label>配送地址：</label>
                        <span>{{ order.shippingAddress }}</span>
                    </div>
                </div>
            </div>

            <!-- 订单商品列表 -->
            <div class="order-items">
                <h2>商品列表</h2>
                <table class="items-table">
                    <thead>
                        <tr>
                            <th>商品名称</th>
                            <th>单价</th>
                            <th>数量</th>
                            <th>小计</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="item in order.items" :key="item.id">
                            <td>{{ item.productName }}</td>
                            <td>¥{{ item.priceAtPurchase }}</td>
                            <td>{{ item.quantity }}</td>
                            <td>¥{{ item.priceAtPurchase * item.quantity }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<style scoped>
.user-order-detail {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.detail-header h1 {
    font-size: 24px;
    color: #333;
}

.back-btn {
    padding: 8px 16px;
    background-color: #6c757d;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
}

.back-btn:hover {
    background-color: #5a6268;
}

.loading,
.error-message,
.no-data {
    text-align: center;
    padding: 50px 0;
    color: #666;
}

.error-message {
    color: #dc3545;
}

.order-detail-content {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    padding: 20px;
}

.order-info h2,
.order-items h2 {
    font-size: 18px;
    margin-bottom: 15px;
    color: #333;
    border-bottom: 1px solid #e0e0e0;
    padding-bottom: 8px;
}

.info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 15px;
    margin-bottom: 30px;
}

.info-item {
    display: flex;
    align-items: center;
}

.info-item label {
    font-weight: 600;
    margin-right: 10px;
    color: #555;
    min-width: 80px;
}

.status-badge {
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    text-transform: uppercase;
}

.status-badge.pending {
    background-color: #fff3cd;
    color: #856404;
}

.status-badge.paid {
    background-color: #d1ecf1;
    color: #0c5460;
}

.status-badge.shipped {
    background-color: #d4edda;
    color: #155724;
}

.status-badge.delivered {
    background-color: #c3e6cb;
    color: #155724;
}

.items-table {
    width: 100%;
    border-collapse: collapse;
}

.items-table th,
.items-table td {
    padding: 12px 16px;
    text-align: left;
    border-bottom: 1px solid #e0e0e0;
}

.items-table th {
    background-color: #f5f5f5;
    font-weight: 600;
    color: #333;
}

.items-table tr:last-child td {
    border-bottom: none;
}
</style>