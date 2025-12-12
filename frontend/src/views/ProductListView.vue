<template>
  <div class="product-list-container">
    <div class="header">
      <h1>商品列表</h1>
      <router-link to="/cart" class="cart-link">
        购物车 ({{ cartStore.totalItems }})
      </router-link>
      <div v-if="isLoading">加载中...</div>
      <div v-else-if="error" class="error-message">{{ error }}</div>

      <div v-else class="products-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <h3>{{ product.name }}</h3>
          <p class="price">¥{{ product.price.toFixed(2) }}</p>
          <p class="stock">库存: {{ product.stockQuantity }}</p>
          <button class="add-to-cart-btn" @click="addToCart(product.id)" :disabled="product.stockQuantity <= 0">
            {{ product.stockQuantity <= 0 ? '已售罄' : '加入购物车' }} </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 现有的样式 */

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cart-link {
  padding: 10px 15px;
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}

.cart-link:hover {
  background-color: #45a049;
}
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/utils/http'; // 你的 Axios 实例
import type { Product } from '@/types'; // 导入项目已定义的Product类型
import { useCartStore } from '@/stores/cart';

// 定义API响应类型
interface ApiResponse {
  success: boolean;
  data: Product[];
  message?: string;
}

const products = ref<Product[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

// 获取购物车store
const cartStore = useCartStore();

async function fetchProducts() {
  isLoading.value = true;
  error.value = null;
  try {
    // 调用 API: 获取所有商品列表
    const response = await api.get<ApiResponse>('/product');

    // 检查响应是否成功
    if (response.data.success) {
      products.value = response.data.data;
    } else {
      throw new Error(response.data.message || '获取商品列表失败');
    }
  } catch (err: any) {
    console.error('获取商品列表失败:', err);
    error.value = '加载商品失败，请检查服务器连接。';
  } finally {
    isLoading.value = false;
  }
}

// 添加商品到购物车
async function addToCart(productId: number) {
  try {
    await cartStore.addToCart(productId, 1);
    alert('商品已成功添加到购物车！');
  } catch (err: any) {
    alert('添加商品到购物车失败：' + (err.message || '未知错误'));
  }
}

onMounted(() => {
  fetchProducts();
});
</script>

<style scoped>
/* (可选) 基础样式，你可以根据需要调整 */
.product-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.product-card {
  border: 1px solid #ddd;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.price {
  color: #e60000;
  font-weight: bold;
  font-size: 1.2em;
}

.error-message {
  color: red;
  padding: 10px;
  border: 1px solid red;
  background-color: #ffebeb;
}

.add-to-cart-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-top: auto;
}

.add-to-cart-btn:hover {
  background-color: #45a049;
}

.add-to-cart-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
</style>