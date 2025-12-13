<!-- frontend/src/views/AdminStatsView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { api } from '@/services/api'; // 使用services/api.ts中的API
import type { StatsOverview, DailySales, MonthlySales, TopSellingProduct, OrderStatusStats } from '@/types';

const router = useRouter();
const authStore = useAuthStore();

// 统计概览数据
const statsOverview = ref<StatsOverview>({
    totalProducts: 0,
    totalOrders: 0,
    totalRevenue: 0,
    pendingOrders: 0
});

// 每日销售数据
const dailySales = ref<DailySales[]>([]);

// 月度销售数据
const monthlySales = ref<MonthlySales[]>([]);

// 热销商品数据
const topSellingProducts = ref<TopSellingProduct[]>([]);

// 订单状态统计
const orderStatusStats = ref<OrderStatusStats[]>([]);

// 加载状态
const isLoading = ref(true);

// 检查权限
onMounted(() => {
    if (!authStore.isAuthenticated || !authStore.isAdmin) {
        router.push({ name: 'login' });
        return;
    }
    loadStatsData();
});

// 加载统计数据
async function loadStatsData() {
    try {
        isLoading.value = true;

        // 获取统计概览
        const overviewResponse = await api.getStats();
        if (overviewResponse.success) {
            statsOverview.value = overviewResponse.data;
        }

        // 获取每日销售数据（最近30天）
        const today = new Date();
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(today.getDate() - 30);

        const dailySalesResponse = await api.getDailySales({
            startDate: thirtyDaysAgo.toISOString().split('T')[0] as string,
            endDate: today.toISOString().split('T')[0] as string
        });
        if (dailySalesResponse.success) {
            dailySales.value = dailySalesResponse.data;
        }

        // 获取月度销售数据（今年）
        const monthlySalesResponse = await api.getMonthlySales({
            year: today.getFullYear().toString()
        });
        if (monthlySalesResponse.success) {
            monthlySales.value = monthlySalesResponse.data;
        }

        // 获取热销商品数据（前10名）
        const topProductsResponse = await api.getTopProducts({ limit: 10 });
        if (topProductsResponse.success) {
            topSellingProducts.value = topProductsResponse.data;
        }

        // 获取订单状态统计
        const orderStatusResponse = await api.getOrderStatusStats();
        if (orderStatusResponse.success) {
            orderStatusStats.value = orderStatusResponse.data;
        }
    } catch (error) {
        console.error('加载统计数据失败:', error);
    } finally {
        isLoading.value = false;
    }
}

// 格式化金额
function formatCurrency(amount: number): string {
    return `¥${amount.toFixed(2)}`;
}

// 格式化百分比
function formatPercentage(value: number): string {
    return `${(value * 100).toFixed(1)}%`;
}
</script>

<template>
    <div class="admin-stats">
        <header class="admin-header">
            <h1>数据分析</h1>
            <div class="header-actions">
                <span class="welcome-text">⭐欢迎，{{ authStore.username }}</span>
            </div>
        </header>

        <div class="stats-content">
            <!-- 统计概览卡片 -->
            <div class="stats-grid">
                <div class="stat-card">
                    <h3>商品总数</h3>
                    <p class="stat-number">{{ statsOverview.totalProducts }}</p>
                </div>
                <div class="stat-card">
                    <h3>订单总数</h3>
                    <p class="stat-number">{{ statsOverview.totalOrders }}</p>
                </div>
                <div class="stat-card">
                    <h3>总收入</h3>
                    <p class="stat-number">{{ formatCurrency(statsOverview.totalRevenue) }}</p>
                </div>
                <div class="stat-card">
                    <h3>待处理订单</h3>
                    <p class="stat-number">{{ statsOverview.pendingOrders }}</p>
                </div>
            </div>

            <!-- 销售趋势 -->
            <div class="stats-section">
                <h2>销售趋势</h2>
                <div v-if="isLoading" class="loading">加载中...</div>
                <div v-else-if="dailySales.length === 0" class="no-data">
                    暂无销售数据
                </div>
                <div v-else class="sales-chart">
                    <!-- 销售趋势图表容器 -->
                    <div class="chart-placeholder">
                        <h3>近30天销售趋势</h3>
                        <div class="sales-data">
                            <div v-for="day in dailySales" :key="day.date" class="sales-bar">
                                <div class="bar"
                                    :style="{ height: `${(day.salesAmount / Math.max(...dailySales.map(d => d.salesAmount))) * 100}%` }">
                                </div>
                                <div class="date-label">{{ new Date(day.date).getDate() }}</div>
                                <div class="amount-label">{{ formatCurrency(day.salesAmount) }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 热销商品 -->
            <div class="stats-section">
                <h2>热销商品</h2>
                <div v-if="isLoading" class="loading">加载中...</div>
                <div v-else-if="topSellingProducts.length === 0" class="no-data">
                    暂无商品销售数据
                </div>
                <div v-else class="top-products">
                    <table>
                        <thead>
                            <tr>
                                <th>排名</th>
                                <th>商品名称</th>
                                <th>销售数量</th>
                                <th>销售金额</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(product, index) in topSellingProducts" :key="product.productId">
                                <td>{{ index + 1 }}</td>
                                <td>{{ product.productName }}</td>
                                <td>{{ product.totalSales }}</td>
                                <td>{{ formatCurrency(product.totalRevenue) }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- 订单状态分布 -->
            <div class="stats-section">
                <h2>订单状态分布</h2>
                <div v-if="isLoading" class="loading">加载中...</div>
                <div v-else-if="orderStatusStats.length === 0" class="no-data">
                    暂无订单数据
                </div>
                <div v-else class="order-status">
                    <div class="status-cards">
                        <div v-for="status in orderStatusStats" :key="status.orderStatus" class="status-card">
                            <div class="status-header">
                                <span class="status-name">{{ status.orderStatus }}</span>
                                <span class="status-percentage">{{ formatPercentage(status.percentage) }}</span>
                            </div>
                            <div class="status-bar">
                                <div class="status-fill" :style="{ width: `${status.percentage * 100}%` }"></div>
                            </div>
                            <div class="status-count">{{ status.count }} 个订单</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.admin-stats {
    min-height: 100vh;
    background-color: #f5f5f5;
}

.admin-header {
    background-color: #2c3e50;
    color: white;
    padding: 1rem 2rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.admin-header h1 {
    margin: 0;
    font-size: 1.5rem;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.welcome-text {
    font-size: 0.9rem;
}

.stats-content {
    padding: 2rem;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.stat-card {
    background: white;
    padding: 1.5rem;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    text-align: center;
}

.stat-card h3 {
    margin: 0 0 0.5rem 0;
    color: #666;
    font-size: 0.9rem;
}

.stat-number {
    margin: 0;
    font-size: 2rem;
    font-weight: bold;
    color: #2c3e50;
}

.stats-section {
    background: white;
    padding: 1.5rem;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 2rem;
}

.stats-section h2 {
    margin: 0 0 1rem 0;
    color: #2c3e50;
}

.loading,
.no-data {
    text-align: center;
    color: #666;
    padding: 2rem;
}

/* 销售趋势图表 */
.sales-chart {
    padding: 1rem 0;
}

.chart-placeholder {
    background-color: #f8f9fa;
    padding: 1.5rem;
    border-radius: 8px;
}

.chart-placeholder h3 {
    text-align: center;
    color: #495057;
    margin-bottom: 1.5rem;
}

.sales-data {
    display: flex;
    align-items: end;
    gap: 1rem;
    height: 250px;
    padding: 1rem 0;
    border-bottom: 1px solid #dee2e6;
    border-left: 1px solid #dee2e6;
    position: relative;
}

.sales-bar {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 100%;
    position: relative;
}

.bar {
    width: 100%;
    background-color: #3498db;
    border-radius: 4px 4px 0 0;
    transition: background-color 0.3s;
}

.bar:hover {
    background-color: #2980b9;
}

.date-label {
    margin-top: 0.5rem;
    font-size: 0.8rem;
    color: #666;
}

.amount-label {
    margin-top: 0.25rem;
    font-size: 0.7rem;
    color: #495057;
}

/* 热销商品表格 */
.top-products {
    overflow-x: auto;
}

.top-products table {
    width: 100%;
    border-collapse: collapse;
}

.top-products th,
.top-products td {
    padding: 0.75rem;
    text-align: left;
    border-bottom: 1px solid #ddd;
}

.top-products th {
    background-color: #f8f9fa;
    font-weight: bold;
    color: #495057;
}

.top-products tr:hover {
    background-color: #f8f9fa;
}

/* 订单状态分布 */
.status-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
}

.status-card {
    background-color: #f8f9fa;
    padding: 1.5rem;
    border-radius: 8px;
}

.status-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.75rem;
}

.status-name {
    font-weight: bold;
    color: #495057;
}

.status-percentage {
    color: #3498db;
    font-weight: bold;
}

.status-bar {
    height: 8px;
    background-color: #e9ecef;
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 0.5rem;
}

.status-fill {
    height: 100%;
    background-color: #3498db;
    border-radius: 4px;
    transition: width 0.5s ease;
}

.status-count {
    font-size: 0.85rem;
    color: #666;
}
</style>