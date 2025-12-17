<script setup lang="ts">
// 导入必要的依赖
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { api } from '@/services/api';
import type { StatsOverview, DailySales, MonthlySales, TopSellingProduct, OrderStatusStats } from '@/types';
// 导入 MonthHeatmap 组件
import MonthHeatmap from '@/components/MonthHeatmap.vue';

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

// 定义每日销售折线图的ref
const lineChartCanvas = ref<HTMLCanvasElement | null>(null);

// **【修复区域 1: 修复 TypeScript 7015 错误】**
// 将 Record<number, string> 更改为 Record<string, string>，以便安全地使用 String() 进行键访问。
const monthAbbr: Record<string, string> = {
    '1': 'JAN', '2': 'FEB', '3': 'MAR', '4': 'APR', '5': 'MAY', '6': 'JUN',
    '7': 'JUL', '8': 'AUG', '9': 'SEP', '10': 'OCT', '11': 'NOV', '12': 'DEC'
};

// 计算月度销售热力图所需数据格式：{ 'JAN': 12000, 'FEB': 9000, ... }
const monthlyHeatmapData = computed<Record<string, number>>(() => {
    const data: Record<string, number> = {};
    monthlySales.value.forEach(item => {
        // 假设 item.month 是 1-12 的数字
        const monthKey = String(item.month); // <-- 修正: 将数字月份转换为字符串键
        const abbr = monthAbbr[monthKey];    // <-- 修正: 使用字符串键进行安全访问
        if (abbr) {
            data[abbr] = item.salesAmount || 0;
        }
    });
    return data;
});

// 计算月度销售额最大值（用于热力图颜色渐变）
const maxMonthlySalesValue = computed(() => {
    if (monthlySales.value.length === 0) return 100;
    const max = Math.max(...monthlySales.value.map(m => m.salesAmount || 0));
    // 确保有一个合理的上限，防止颜色过浅
    return max > 0 ? max : 100;
});


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

            // 修复日期显示NaN问题：将后端返回的sale_date映射到前端的date字段，并确保日期格式正确
            dailySales.value = (allStats.dailySales || []).map((item: any) => {
                // 确保日期字段存在且为字符串
                let date = item.sale_date || item.date || '';
                
                // 处理Java序列化的日期格式（如 /Date(1733049600000)/）
                if (typeof date === 'string' && date.startsWith('/Date(') && date.endsWith(')/')) {
                    // 提取时间戳部分
                    const timestamp = date.substring(6, date.length - 2);
                    // 转换为ISO字符串
                    date = new Date(parseInt(timestamp)).toISOString();
                }
                // 如果日期是Date对象，转换为ISO字符串
                else if (date instanceof Date) {
                    date = date.toISOString();
                }
// 如果日期是数字（时间戳），转换为ISO字符串
                else if (typeof date === 'number') {
                    date = new Date(date).toISOString();
                }
                // 处理后端返回的包含year、month、day字段的日期对象
                else if (typeof date === 'object' && date !== null && date.year && date.month && date.day) {
                    // 注意：Java的Calendar月份是0-11，而JavaScript的Date月份也是0-11
                    date = new Date(date.year, date.month - 1, date.day).toISOString();
                }
                // 如果已经是字符串，确保格式正确
                else if (typeof date === 'string' && date.trim()) {
                    // 专门处理YYYY-MM-DD格式的日期字符串，避免时区问题
                    const yyyyMmDdRegex = /^\d{4}-\d{2}-\d{2}$/;
                    if (yyyyMmDdRegex.test(date)) {
                        // 使用Date.UTC确保日期解析的准确性，避免时区问题
                        const parts = date.split('-');
                        // 修复TypeScript错误：添加类型断言，确保parts数组元素非空
                        const year = parseInt(parts[0] as string);
                        const month = parseInt(parts[1] as string) - 1; // JavaScript月份是0-11
                        const day = parseInt(parts[2] as string);
                        date = new Date(Date.UTC(year, month, day)).toISOString();
                    } else {
                        // 尝试解析其他格式的字符串日期
                        const parsedDate = new Date(date);
                        if (!isNaN(parsedDate.getTime())) {
                            date = parsedDate.toISOString();
                        }
                    }
                }
                
                return {
                    date: date,
                    salesAmount: item.sales_amount || item.salesAmount || 0,
                    orderCount: item.order_count || item.orderCount || 0
                };
            });

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

// 计算最大销售金额（每日）
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

// 绘制每日销售折线图
function drawLineChart() {
    const canvas = lineChartCanvas.value;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // 获取数据
    const salesData = dailySales.value;
    // 至少需要两个点才能绘制折线
    if (salesData.length < 2) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        return;
    }

    // 获取父容器尺寸，实现响应式
    const parent = canvas.parentElement?.parentElement;
    if (parent) {
        // 设置画布宽度为父容器实际宽度
        canvas.width = parent.offsetWidth;
        // 保持模板中设置的固定高度
        canvas.height = canvas.getAttribute('height') ? parseInt(canvas.getAttribute('height')!) : 300;
    } else {
        canvas.width = 800; // 默认值
        canvas.height = 300; // 默认值
    }

    // 清空画布
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // 计算坐标比例
    // 左边距设置为 0，因为 Y 轴刻度由 Vue 模板中的元素处理
    const margin = { top: 20, right: 20, bottom: 40, left: 0 };
    const chartWidth = canvas.width - margin.left - margin.right;
    const chartHeight = canvas.height - margin.top - margin.bottom;

    // 计算数据范围
    const maxAmount = maxSalesAmount.value;

    // 绘制网格线
    ctx.strokeStyle = '#e0e0e0';
    ctx.lineWidth = 1;

    // 绘制垂直网格线 (一个数据点一条线)
    const dataPoints = salesData.length;
    const stepX = dataPoints > 1 ? chartWidth / (dataPoints - 1) : chartWidth; 
    salesData.forEach((_, i) => {
        const x = margin.left + i * stepX;
        ctx.beginPath();
        ctx.moveTo(x, margin.top);
        ctx.lineTo(x, margin.top + chartHeight);
        ctx.stroke();
    });

    // 绘制水平网格线 (5 等分)
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

// 更新 triggerChartRedraw 函数
function triggerChartRedraw() {
    // 确保在 DOM 更新后执行，以获取正确的尺寸
    nextTick(() => {
        // 1. 绘制饼状图
        const pieCanvas = document.getElementById('order-status-pie-chart') as HTMLCanvasElement;
        if (pieCanvas) {
            const ctx = pieCanvas.getContext('2d');
            if (ctx) {
                // 获取父容器尺寸
                const container = pieCanvas.parentElement;
                const width = container ? container.offsetWidth : 300; // 减小默认宽度
                const height = container ? container.offsetHeight : 300; // 减小默认高度
                pieCanvas.width = width;
                pieCanvas.height = height;
                drawPieChart(ctx, orderStatusStats.value, width, height);
            }
        }

        // 2. 绘制每日销售折线图
        drawLineChart();
    });
}

// 格式化日期为 MM/DD 格式
function formatDate(dateString: string): string {
    if (!dateString) return '-';
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '-';
        const month = date.getMonth() + 1;
        const day = date.getDate();
        return `${month}/${day}`;
    } catch (error) {
        console.error('日期格式化错误:', error);
        return '-';
    }
}

</script>

<template>
    <div class="admin-stats">
        <div class="admin-header">
            <h1>统计分析</h1>
            <div class="welcome-text">欢迎，{{ authStore.username }}</div>
        </div>

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

        <div class="stats-section">
            <h2>月度销售热力图</h2>
            <div v-if="isLoading" class="loading">加载中...</div>
            <div v-else-if="monthlySales.length === 0" class="no-data">
                暂无月度销售数据
            </div>
            <div v-else class="month-heatmap-container">
                <MonthHeatmap 
                    :month-data="monthlyHeatmapData"
                    :max-value="maxMonthlySalesValue" 
                />
            </div>
        </div>

        <div class="stats-section">
            <h2>销售趋势 (每日)</h2>
            <div v-if="isLoading" class="loading">加载中...</div>
            <div v-else-if="dailySales.length === 0" class="no-data">
                暂无每日销售数据
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
                                <!-- 修改后 -->
                                <div v-for="i in 6" :key="i" class="y-tick">
                                    {{ formatCurrency((maxSalesAmount * i) / 5) }}
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
                                        <!-- 修复日期显示NaN问题：添加安全检查 -->
                                        {{ day.date ? (new Date(day.date).getMonth() + 1) + '/' + (new Date(day.date).getDate()) : '-' }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

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

        <div class="stats-section">
            <h2>订单状态分布</h2>
            <div v-if="isLoading" class="loading">加载中...</div>
            <div v-else-if="orderStatusStats.length === 0" class="no-data">
                暂无订单数据
            </div>
            <div v-else class="order-status">
                <div class="pie-chart-container">
                    <canvas id="order-status-pie-chart" width="300" height="300"></canvas>
                </div>
                <!-- 将图例移到饼状图右侧 -->
                <div class="pie-legend">
                    <div 
                        v-for="(status, index) in orderStatusStats" 
                        :key="status.orderStatus" 
                        class="legend-item"
                    >
                        <div 
                            class="legend-color"
                            :style="{ backgroundColor: ['#3498db', '#e74c3c', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c'][index % 6] }"
                        ></div>
                        <div class="legend-text">
                            <span>{{ status.orderStatus }}</span>
                            <span class="legend-value">{{ formatPercentage(status.percentage) }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 样式保持不变 */

.admin-stats {
    padding: 20px;
    background-color: #f4f7f9;
    min-height: 100vh;
}

.admin-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
}

.admin-header h1 {
    font-size: 2rem;
    color: #333;
}

.welcome-text {
    color: #666;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 20px;
    margin-bottom: 30px;
}

.stat-card {
    background-color: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.stat-card h3 {
    font-size: 1rem;
    color: #999;
    margin-bottom: 5px;
}

.stat-number {
    font-size: 2rem;
    font-weight: bold;
    color: #3498db;
}

.stats-section {
    margin-bottom: 30px;
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stats-section h2 {
    border-bottom: 2px solid #eee;
    padding-bottom: 10px;
    margin-bottom: 20px;
    color: #333;
}

.loading, .no-data {
    text-align: center;
    padding: 50px;
    color: #999;
}

/* 每日销售图表样式 */
.sales-chart .chart-container {
    background-color: #fff;
    padding: 20px;
    border-radius: 8px;
}

.line-chart {
    position: relative;
}

.chart-title {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    font-size: 0.9rem;
    color: #666;
}

.chart-area {
    display: flex;
    height: 300px; /* 固定高度 */
}

.y-axis {
    display: flex;
    flex-direction: column-reverse; /* 确保数值从下到上递增 */
    justify-content: space-between;
    padding-right: 10px;
    width: 60px;
    font-size: 0.8rem;
    color: #666;
    text-align: right;
    margin-bottom: 40px; /* 留出 X 轴标签空间 */
}

.y-tick {
    height: 30px; /* 匹配 canvas 绘制时的水平网格线 */
    line-height: 30px;
}

.chart-grid {
    flex-grow: 1;
    position: relative;
}

.line-chart-canvas {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}

.x-axis {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 40px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    font-size: 0.8rem;
    color: #666;
}

.x-tick {
    position: absolute;
    white-space: nowrap;
    text-align: center;
}

/* 热销商品表格样式 */
.top-products {
    position: relative;
}

.top-products table {
    width: 100%;
    border-collapse: collapse;
}

.top-products th, .top-products td {
    padding: 12px 15px;
    text-align: left;
    border-bottom: 1px solid #eee;
}

.top-products th {
    background-color: #f9f9f9;
    font-weight: bold;
    color: #333;
}

.product-row:hover {
    background-color: #f5f5f5;
    cursor: default;
}

/* 悬浮提示框样式 */
.product-tooltip {
    position: fixed;
    z-index: 1000;
    background-color: rgba(50, 50, 50, 0.9);
    color: #fff;
    padding: 10px;
    border-radius: 4px;
    pointer-events: none;
    transition: opacity 0.2s;
    min-width: 150px;
}

.tooltip-content h4 {
    margin-top: 0;
    font-size: 1rem;
    margin-bottom: 5px;
}

.tooltip-content p {
    margin: 2px 0;
    font-size: 0.9rem;
}

/* 订单状态饼图样式 */
.order-status {
    display: flex;
    justify-content: center; /* 修改为居中排列 */
    align-items: center;
    flex-wrap: wrap;
    gap: 40px; /* 添加间距 */
}

.pie-chart-container {
    /* 缩小饼状图容器尺寸 */
    width: 300px; 
    height: 300px;
    position: relative;
}

.pie-legend {
    display: flex;
    flex-direction: column;
    padding: 20px;
    min-width: 200px;
}

.legend-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.legend-color {
    width: 12px;
    height: 12px;
    border-radius: 3px;
    margin-right: 10px;
}

.legend-text {
    display: flex;
    justify-content: space-between;
    width: 100%;
    font-size: 0.9rem;
    color: #333;
}

.legend-value {
    font-weight: bold;
    margin-left: 10px;
}

/* MonthHeatmap 组件的样式 (如果 MonthHeatmap.vue 没有样式，这里可以提供容器样式) */
.month-heatmap-container {
    max-width: 100%;
    margin: 20px auto;
}
</style>