<!-- d:\JavaMall\frontend\src\views\ProductDetailView.vue -->
<template>
    <div class="product-detail-container">
        <div v-if="isLoading" class="loading-container">
            <div class="loading-spinner"></div>
            <p>正在加载产品详情...</p>
        </div>

        <div v-else-if="error" class="error-container">
            <i class="error-icon">⚠️</i>
            <p class="error-message">{{ error }}</p>
            <button class="retry-btn" @click="fetchProductDetail">重新加载</button>
        </div>

        <div v-else-if="product" class="product-detail-content">
            <div class="product-images">
                <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" class="product-main-image" />
                <div v-else class="no-image">
                    <i class="image-placeholder">📷</i>
                    <span>暂无图片</span>
                </div>
            </div>

            <div class="product-info">
                <h1 class="product-name">{{ product.name }}</h1>
                <div class="product-price">¥{{ product.price.toFixed(2) }}</div>
                <div class="product-stock" :class="{ 'out-of-stock': product.stockQuantity <= 0 }">
                    {{ product.stockQuantity <= 0 ? '已售罄' : `库存: ${product.stockQuantity}` }} </div>

                        <div class="product-description">
                            <h2>商品描述</h2>
                            <p>{{ product.description }}</p>
                        </div>

                        <div class="product-actions">
                            <div class="quantity-selector">
                                <button class="quantity-btn" @click="decreaseQuantity"
                                    :disabled="quantity <= 1 || product.stockQuantity <= 0">-</button>
                                <input type="number" v-model.number="quantity" min="1" :max="product.stockQuantity"
                                    :disabled="product.stockQuantity <= 0" />
                                <button class="quantity-btn" @click="increaseQuantity"
                                    :disabled="quantity >= product.stockQuantity || product.stockQuantity <= 0">+</button>
                            </div>

                            <button class="add-to-cart-btn" @click="addToCart" :disabled="product.stockQuantity <= 0">
                                {{ product.stockQuantity <= 0 ? '已售罄' : '加入购物车' }} </button>

                                    <router-link to="/" class="back-link">返回商品列表</router-link>
                        </div>
                </div>
            </div>
        </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import type { Product } from '@/types';
import { api } from '@/services/api';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();

const product = ref<Product | null>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);
const quantity = ref(1);

async function fetchProductDetail() {
    isLoading.value = true;
    error.value = null;
    try {
        const id = Number(route.params.id);
        const response = await api.getProductDetail(id);

        if (response.success) {
            product.value = response.data;
        } else {
            throw new Error(response.message || '获取产品详情失败');
        }
    } catch (err: any) {
        console.error('获取产品详情失败:', err);
        error.value = '加载产品详情失败，请检查服务器连接。';
    } finally {
        isLoading.value = false;
    }
}

function increaseQuantity() {
    if (product.value && quantity.value < product.value.stockQuantity) {
        quantity.value++;
    }
}

function decreaseQuantity() {
    if (quantity.value > 1) {
        quantity.value--;
    }
}

async function addToCart() {
    if (!product.value) return;

    try {
        await cartStore.addToCart(product.value.id, quantity.value);
        showNotification('商品已成功添加到购物车！', 'success');
    } catch (err: any) {
        showNotification('添加商品到购物车失败：' + (err.message || '未知错误'), 'error');
    }
}

// 简单的通知函数
function showNotification(message: string, type: 'success' | 'error') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    document.body.appendChild(notification);
    setTimeout(() => notification.classList.add('show'), 10);
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => document.body.removeChild(notification), 300);
    }, 3000);
}

onMounted(() => {
    fetchProductDetail();
});
</script>

<style scoped>
.product-detail-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 2rem;
    text-align: center;
}

.loading-spinner {
    width: 50px;
    height: 50px;
    border: 4px solid #f3f3f3;
    border-top: 4px solid #667eea;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 1rem;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

.error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 2rem;
    text-align: center;
    background-color: #fff5f5;
    border-radius: 12px;
    border: 1px solid #ffebee;
}

.error-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
}

.error-message {
    color: #c62828;
    font-size: 1.1rem;
    margin-bottom: 1.5rem;
}

.retry-btn {
    background-color: #667eea;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
}

.retry-btn:hover {
    background-color: #5568d3;
    transform: translateY(-1px);
}

.product-detail-content {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
    margin-top: 2rem;
}

.product-images {
    background-color: #f8f9fa;
    padding: 2rem;
    border-radius: 12px;
    text-align: center;
}

.product-main-image {
    max-width: 100%;
    max-height: 500px;
    object-fit: contain;
    border-radius: 8px;
}

.no-image {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 300px;
    color: #999;
}

.image-placeholder {
    font-size: 4rem;
    margin-bottom: 1rem;
}

.product-info {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.product-name {
    font-size: 2rem;
    font-weight: 700;
    color: #333;
    margin: 0;
}

.product-price {
    font-size: 2.5rem;
    font-weight: 700;
    color: #e63946;
}

.product-stock {
    font-size: 1.1rem;
    color: #4caf50;
    font-weight: 500;
}

.product-stock.out-of-stock {
    color: #f44336;
}

.product-description {
    margin-top: 1rem;
}

.product-description h2 {
    font-size: 1.5rem;
    margin-bottom: 1rem;
    color: #333;
}

.product-description p {
    color: #666;
    line-height: 1.6;
}

.product-actions {
    margin-top: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.quantity-selector {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.quantity-btn {
    width: 40px;
    height: 40px;
    border: 1px solid #ddd;
    background-color: white;
    cursor: pointer;
    font-size: 1.2rem;
    border-radius: 4px;
    transition: all 0.3s ease;
}

.quantity-btn:hover:not(:disabled) {
    background-color: #f0f0f0;
}

.quantity-btn:disabled {
    background-color: #f5f5f5;
    cursor: not-allowed;
    opacity: 0.6;
}

.quantity-selector input {
    width: 80px;
    height: 40px;
    border: 1px solid #ddd;
    border-radius: 4px;
    text-align: center;
    font-size: 1.1rem;
}

.quantity-selector input:disabled {
    background-color: #f5f5f5;
    cursor: not-allowed;
}

.add-to-cart-btn {
    background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
    color: white;
    border: none;
    padding: 1rem 2rem;
    border-radius: 8px;
    font-size: 1.2rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
}

.add-to-cart-btn:hover:not(:disabled) {
    background: linear-gradient(135deg, #45a049 0%, #3d8b40 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.add-to-cart-btn:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
    opacity: 0.6;
}

.back-link {
    color: #667eea;
    text-decoration: none;
    font-weight: 500;
    margin-top: 1rem;
    display: inline-block;
}

.back-link:hover {
    text-decoration: underline;
}

/* 通知样式 */
.notification {
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    color: white;
    font-weight: 600;
    transform: translateX(100%);
    transition: transform 0.3s ease;
    z-index: 1000;
}

.notification.success {
    background-color: #4caf50;
}

.notification.error {
    background-color: #f44336;
}

.notification.show {
    transform: translateX(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .product-detail-content {
        grid-template-columns: 1fr;
    }

    .product-name {
        font-size: 1.5rem;
    }

    .product-price {
        font-size: 2rem;
    }
}
</style>