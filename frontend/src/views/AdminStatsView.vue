<script setup lang="ts">
// 导入必要的依赖
import { ref, onMounted, computed, watch, nextTick } from 'vue'; 
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { api } from '@/services/api';
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

// 定义悬浮提示框的响应式数据
const tooltip = ref<HTMLElement | null>(null);
const tooltipProduct = ref({
    productName: '',
    totalSales: 0,
    totalRevenue: 0
});
const tooltipPosition = ref({ x: 0, y: 0 });
const tooltipVisible = ref(false);

// 定义折线图的ref
const lineChartCanvas = ref<HTMLCanvasElement | null>(null);

// 检查权限
onMounted(() => {
    if (!authStore.isAuthenticated || !authStore.isAdmin) {
        router.push({ name: 'login' });
        return;
    }
    loadStatsData();
    // 窗口大小变化时重新绘制
    window.addEventListener('resize', triggerChartRedraw);
});

// 加载统计数据
async function loadStatsData() {
    try {
        isLoading.value = true;

        // 获取所有统计数据
        const allStatsResponse = await api.getStats();
        
        if (allStatsResponse.success) {
            const allStats = allStatsResponse.data;
            
            // 更新响应式数据
            statsOverview.value = allStats.statsOverview || {
                totalProducts: 0,
                totalOrders: 0,
                totalRevenue: 0,
                pendingOrders: 0
            };
            
            // 修复日期显示NaN问题：将后端返回的sale_date映射到前端的date字段
            dailySales.value = (allStats.dailySales || []).map((item:any) => ({
                date: item.sale_date || item.date || '',
                salesAmount: item.sales_amount || item.salesAmount || 0,
                orderCount: item.order_count || item.orderCount || 0
            }));
            
            monthlySales.value = allStats.monthlySales || [];
            topSellingProducts.value = allStats.topSellingProducts || [];
            orderStatusStats.value = allStats.orderStatusStats || [];

            // 数据加载完成后，立即触发图表绘制
            triggerChartRedraw();
        }
    } catch (error) {
        console.error('加载统计数据失败:', error);
    } finally {
        isLoading.value = false;
    }
}

// 格式化百分比
function formatPercentage(value: number): string {
    return `${value.toFixed(1)}%`;
}

// 格式化金额
function formatCurrency(amount: number): string {
    return `¥${amount.toFixed(2)}`;
}

// 计算最大销售金额（修复NaN问题）
const maxSalesAmount = computed(() => {
    if (dailySales.value.length === 0) return 1; // 避免除以0
    const max = Math.max(...dailySales.value.map(d => d.salesAmount || 0));
    return max === 0 ? 1 : max; // 避免除以0
});

// 饼状图绘制函数
function drawPieChart(ctx: CanvasRenderingContext2D, data: OrderStatusStats[], width: number, height: number) {
    const centerX = width / 2;
    const centerY = height / 2;
    const radius = Math.min(centerX, centerY) - 20;
    
    // 清空画布
    ctx.clearRect(0, 0, width, height);
    
    // 如果没有数据，直接返回
    if (data.length === 0) return;
    
    // 定义颜色数组
    const colors = ['#3498db', '#e74c3c', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c'];
    
    // 计算起始角度
    let startAngle = 0;
    
    // 绘制饼状图
    data.forEach((item, index) => {
        // 计算当前扇形的角度（弧度）
        const sliceAngle = (item.percentage / 100) * 2 * Math.PI;
        
        // 设置扇形颜色
        ctx.fillStyle = colors[index % colors.length] as string;
        
        // 绘制扇形
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
        ctx.closePath();
        ctx.fill();
        
        // 绘制扇形边框
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        // 绘制扇形标签
        const labelAngle = startAngle + sliceAngle / 2;
        const labelRadius = radius + 30;
        const labelX = centerX + Math.cos(labelAngle) * labelRadius;
        const labelY = centerY + Math.sin(labelAngle) * labelRadius;
        
        ctx.fillStyle = '#333';
        ctx.font = '12px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        if (item.percentage > 5) { // 优化：只有百分比足够大才绘制标签
            ctx.fillText(item.orderStatus, labelX, labelY);
        }
        
        // 更新起始角度
        startAngle += sliceAngle;
    });
    
    // 绘制中心圆
    ctx.fillStyle = '#fff';
    ctx.beginPath();
    ctx.arc(centerX, centerY, radius * 0.6, 0, 2 * Math.PI);
    ctx.fill();
    
    // 绘制中心文本
    ctx.fillStyle = '#333';
    ctx.font = 'bold 14px Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('订单状态', centerX, centerY - 10);
    ctx.font = '12px Arial';
    ctx.fillText(`共${statsOverview.value.totalOrders}单`, centerX, centerY + 10);
}

// 绘制折线图
function drawLineChart() {
    const canvas = lineChartCanvas.value;
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    
    // 获取数据
    const salesData = dailySales.value;
    // 修复：至少需要两个点才能绘制折线
    if (salesData.length < 2) { 
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        return;
    }
    
    // 设置画布尺寸
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;
    
    // 清空画布
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // 计算坐标比例
    const margin = { top: 20, right: 20, bottom: 40, left: 60 };
    const chartWidth = canvas.width - margin.left - margin.right;
    const chartHeight = canvas.height - margin.top - margin.bottom;
    
    // 计算数据范围
    const maxAmount = maxSalesAmount.value;
    
    // 绘制网格线
    ctx.strokeStyle = '#e0e0e0';
    ctx.lineWidth = 1;
    
    // 绘制垂直网格线
    const stepX = chartWidth / (salesData.length - 1); // 修复：使用 length - 1 作为步长
    salesData.forEach((_, i) => {
        const x = margin.left + i * stepX;
        ctx.beginPath();
        ctx.moveTo(x, margin.top);
        ctx.lineTo(x, margin.top + chartHeight);
        ctx.stroke();
    });
    
    // 绘制水平网格线
    for (let i = 0; i <= 5; i++) {
        const y = margin.top + (i / 5) * chartHeight;
        ctx.beginPath();
        ctx.moveTo(margin.left, y);
        ctx.lineTo(margin.left + chartWidth, y);
        ctx.stroke();
    }
    
    // 绘制折线
    ctx.strokeStyle = '#3498db';
    ctx.lineWidth = 3;
    ctx.beginPath();
    
    salesData.forEach((item: DailySales, index: number) => {
        const x = margin.left + index * stepX;
        const y = margin.top + chartHeight - ((item.salesAmount || 0) / maxAmount) * chartHeight;
        
        if (index === 0) {
            ctx.moveTo(x, y);
        } else {
            ctx.lineTo(x, y);
        }
    });
    
    ctx.stroke();
    
    // 绘制数据点
    salesData.forEach((item: DailySales, index: number) => {
        const x = margin.left + index * stepX;
        const y = margin.top + chartHeight - ((item.salesAmount || 0) / maxAmount) * chartHeight;
        
        ctx.fillStyle = '#3498db';
        ctx.beginPath();
        ctx.arc(x, y, 5, 0, Math.PI * 2);
        ctx.fill();
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 2;
        ctx.stroke();
    });
}

// 核心绘制逻辑：确保数据和DOM都准备就绪
function triggerChartRedraw() {
    // 确保在 DOM 更新后执行，以获取正确的尺寸
    nextTick(() => {
        // 1. 绘制饼状图
        const pieCanvas = document.getElementById('order-status-pie-chart') as HTMLCanvasElement;
        if (pieCanvas) {
            const ctx = pieCanvas.getContext('2d');
            if (ctx) {
                // 确保饼状图尺寸正确
                const width = pieCanvas.offsetWidth > 0 ? pieCanvas.offsetWidth : 400;
                const height = pieCanvas.offsetHeight > 0 ? pieCanvas.offsetHeight : 400;
                pieCanvas.width = width;
                pieCanvas.height = height;
                drawPieChart(ctx, orderStatusStats.value, width, height);
            }
        }
        
        // 2. 绘制折线图
        drawLineChart();
    });
}

// 显示悬浮提示
function showTooltip(event: MouseEvent, productData: string) {
    try {
        const product = JSON.parse(productData);
        tooltipProduct.value = product;
        tooltipPosition.value = {
            x: event.clientX + 10,
            y: event.clientY + 10
        };
        tooltipVisible.value = true;
    } catch (error) {
        console.error('解析产品数据失败:', error);
    }
}

// 隐藏悬浮提示
function hideTooltip() {
    tooltipVisible.value = false;
}

// 折线图数据（用于Vuetify图表）
const lineChartData = computed(() => ({
    labels: dailySales.value.map(day => {
        const date = new Date(day.date);
        return `${date.getMonth() + 1}/${date.getDate()}`;
    }),
    datasets: [
        {
            label: '销售金额',
            data: dailySales.value.map(day => day.salesAmount),
            borderColor: '#3498db',
            backgroundColor: 'rgba(52, 152, 219, 0.1)',
            tension: 0.3,
            fill: true,
        },
    ],
}));

// 折线图配置
const lineChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    animation: {
        duration: 1000,
        easing: 'easeInOutQuart',
    },
    scales: {
        y: {
            beginAtZero: true,
            title: {
                display: true,
                text: '金额 (¥)',
            },
        },
    },
};

// 饼图数据
const pieChartData = computed(() => ({
    labels: orderStatusStats.value.map(stat => stat.orderStatus),
    datasets: [
        {
            data: orderStatusStats.value.map(stat => stat.count),
            backgroundColor: ['#3498db', '#e74c3c', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c'],
            hoverBackgroundColor: ['#2980b9', '#c0392b', '#27ae60', '#e67e22', '#8e44ad', '#16a085'],
        },
    ],
}));

// 饼图配置
const pieChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    animation: {
        animateScale: true,
        animateRotate: true,
    },
};
</script>

<template>
    <div class="admin-stats">
        <div class="admin-header">
            <h1>统计分析</h1>
            <div class="welcome-text">欢迎，{{ authStore.username }}</div>
        </div>

        <!-- 统计概览卡片 -->
        <div class="stats-grid">
            <div class="stat-card">
                <h3>总商品数</h3>
                <div class="stat-number">{{ statsOverview.totalProducts }}</div>
            </div>
            <div class="stat-card">
                <h3>总订单数</h3>
                <div class="stat-number">{{ statsOverview.totalOrders }}</div>
            </div>
            <div class="stat-card">
                <h3>总销售额</h3>
                <div class="stat-number">{{ formatCurrency(statsOverview.totalRevenue) }}</div>
            </div>
            <div class="stat-card">
                <h3>待处理订单</h3>
                <div class="stat-number">{{ statsOverview.pendingOrders }}</div>
            </div>
        </div>

        <!-- 销售趋势图表 -->
        <div class="stats-section">
            <h2>销售趋势</h2>
            <div v-if="isLoading" class="loading">加载中...</div>
            <div v-else-if="dailySales.length === 0" class="no-data">
                暂无销售数据
            </div>
            <div v-else class="sales-chart">
                <div class="chart-container">
                    <h3>每日销售金额</h3>
                    <div class="line-chart">
                        <div class="chart-title">
                            <span>销售金额</span>
                            <span>日期</span>
                        </div>
                        <div class="chart-area">
                            <div class="y-axis">
                                <div v-for="i in 6" :key="i" class="y-tick">
                                    {{ formatCurrency((maxSalesAmount * (5 - i + 1)) / 5) }}
                                </div>
                            </div>
                            <div class="chart-grid">
                                <canvas 
                                    ref="lineChartCanvas" 
                                    class="line-chart-canvas"
                                    width="800" 
                                    height="300"
                                ></canvas>
                                <div class="x-axis">
                                    <div 
                                        v-for="(day, index) in dailySales" 
                                        :key="day.date"
                                        class="x-tick"
                                        :style="{
                                            left: `${dailySales.length > 1 ? index * (100 / (dailySales.length - 1)) : 0}%`,
                                            transform: index === dailySales.length - 1 ? 'translateX(-100%)' : 'translateX(-50%)'
                                        }"
                                    >
                                        {{ new Date(day.date).getMonth() + 1 }}/{{ new Date(day.date).getDate() }}
                                    </div>
                                </div>
                            </div>
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
                        <tr 
                            v-for="(product, index) in topSellingProducts" 
                            :key="product.productId"
                            class="product-row" 
                            @mouseenter="showTooltip($event, JSON.stringify(product))" 
                            @mouseleave="hideTooltip"
                        >
                            <td>{{ index + 1 }}</td>
                            <td>{{ product.productName }}</td>
                            <td>{{ product.totalSales }}</td>
                            <td>{{ formatCurrency(product.totalRevenue) }}</td>
                        </tr>
                    </tbody>
                </table>
                <div 
                    ref="tooltip" 
                    class="product-tooltip"
                    :style="{
                        left: `${tooltipPosition.x}px`,
                        top: `${tooltipPosition.y}px`,
                        opacity: tooltipVisible ? 1 : 0
                    }"
                >
                    <div class="tooltip-content">
                        <h4>{{ tooltipProduct.productName }}</h4>
                        <p>销售数量: {{ tooltipProduct.totalSales }}</p>
                        <p>销售金额: {{ formatCurrency(tooltipProduct.totalRevenue) }}</p>
                        <p v-if="tooltipProduct.totalSales > 0">
                            平均单价: {{ formatCurrency(tooltipProduct.totalRevenue / tooltipProduct.totalSales) }}
                        </p>
                    </div>
                </div>
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
                <div class="pie-chart-container">
                    <canvas id="order-status-pie-chart" width="400" height="400"></canvas>
                    <div class="pie-legend">
                        <div v-for="(status, index) in orderStatusStats" :key="status.orderStatus" class="legend-item">
                            <div class="legend-color" :style="{ backgroundColor: ['#3498db', '#e74c3c', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c'][index % 6] }"></div>
                            <div class="legend-text">
                                <span>{{ status.orderStatus }}</span>
                                <span class="legend-value">{{ status.count }}单 ({{ formatPercentage(status.percentage) }})</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 基本样式 */
.admin-stats {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

.admin-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
    padding-bottom: 15px;
    border-bottom: 1px solid #e0e0e0;
}

.admin-header h1 {
    font-size: 28px;
    color: #333;
}

.welcome-text {
    font-size: 16px;
    color: #666;
}

/* 统计卡片网格 */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 30px;
    margin-bottom: 40px;
}

.stat-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    animation: fadeIn 0.5s ease-out;
}

.stat-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.stat-card h3 {
    font-size: 16px;
    color: #666;
    margin-bottom: 10px;
}

.stat-number {
    font-size: 32px;
    font-weight: bold;
    color: #333;
}

/* 统计区块 */
.stats-section {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    margin-bottom: 30px;
    animation: fadeIn 0.5s ease-out;
}

.stats-section h2 {
    font-size: 20px;
    color: #333;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e0e0e0;
}

/* 加载状态 */
.loading {
    text-align: center;
    padding: 40px;
    color: #666;
    font-style: italic;
}

/* 无数据状态 */
.no-data {
    text-align: center;
    padding: 40px;
    color: #999;
    font-style: italic;
    background-color: #f9f9f9;
    border-radius: 4px;
}

/* 销售趋势图表 */
.sales-chart {
    margin-top: 20px;
}

.chart-container {
    margin: 0 auto;
    max-width: 900px;
}

.chart-container h3 {
    text-align: center;
    margin-bottom: 20px;
    color: #555;
}

.line-chart {
    background-color: #f9f9f9;
    padding: 20px;
    border-radius: 8px;
}

.chart-title {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    font-size: 14px;
    color: #666;
}

.chart-area {
    display: flex;
    align-items: flex-end;
    height: 300px;
}

.y-axis {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 100%;
    margin-right: 10px;
    font-size: 12px;
    color: #666;
}

.y-tick {
    text-align: right;
    padding-right: 5px;
}

.chart-grid {
    position: relative;
    flex: 1;
    height: 100%;
    overflow: hidden;
}

.line-chart-canvas {
    width: 100%;
    height: 100%;
    background-color: #fff;
    border-radius: 4px;
    animation: chartFadeIn 0.8s ease-out;
}

.x-axis {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    padding: 0 20px;
    font-size: 12px;
    color: #666;
}

.x-tick {
    position: absolute;
    bottom: -25px;
    white-space: nowrap;
    transform: translateX(-50%);
}

/* 热销商品表格 */
.top-products {
    margin-top: 20px;
    overflow-x: auto;
    position: relative;
}

.top-products table {
    width: 100%;
    border-collapse: collapse;
    background-color: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.top-products th,
.top-products td {
    padding: 15px;
    text-align: left;
    border-bottom: 1px solid #f0f0f0;
}

.top-products th {
    background-color: #f9f9f9;
    font-weight: bold;
    color: #333;
}

.top-products tr.product-row:hover {
    background-color: #f5f5f5;
    cursor: pointer;
    transition: background-color 0.2s ease;
}

/* 悬浮提示框 */
.product-tooltip {
    position: fixed;
    z-index: 1000;
    background-color: rgba(0, 0, 0, 0.85);
    color: #fff;
    padding: 15px;
    border-radius: 6px;
    font-size: 14px;
    opacity: 0;
    transform: translateY(10px);
    transition: all 0.3s ease;
    pointer-events: none;
    max-width: 250px;
}

.product-tooltip.show {
    opacity: 1;
    transform: translateY(0);
}

.tooltip-content h4 {
    margin: 0 0 10px 0;
    padding-bottom: 5px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.tooltip-content p {
    margin: 5px 0;
}

/* 订单状态饼图 */
.order-status {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
}

.pie-chart-container {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 50px;
    max-width: 800px;
    margin: 0 auto;
}

.pie-legend {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.legend-item {
    display: flex;
    align-items: center;
    gap: 10px;
}

.legend-color {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    border: 1px solid #e0e0e0;
}

.legend-text {
    display: flex;
    flex-direction: column;
    font-size: 14px;
}

.legend-value {
    font-size: 12px;
    color: #666;
}

/* 动画效果 */
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes chartFadeIn {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .stats-grid {
        grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
        gap: 15px;
    }
    
    .stat-card h3 {
        font-size: 14px;
    }
    
    .stat-number {
        font-size: 24px;
    }
    
    .pie-chart-container {
        flex-direction: column;
        gap: 30px;
    }
}
</style>