<template>
  <div class="product-list-container">
    <h1>商品列表</h1>
    <div v-if="isLoading">加载中...</div>
    <div v-else-if="error" class="error-message">{{ error }}</div>
    
    <div v-else class="products-grid">
      <div v-for="product in products" :key="product.id" class="product-card">
        <h3>{{ product.name }}</h3>
        <p class="price">¥{{ product.price.toFixed(2) }}</p>
        <p class="stock">库存: {{ product.stockQuantity }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/utils/http'; // 你的 Axios 实例
import type { Product } from '@/types'; // 导入项目已定义的Product类型

// 定义API响应类型
interface ApiResponse {
  success: boolean;
  data: Product[];
  message?: string;
}

const products = ref<Product[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

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

onMounted(fetchProducts);
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
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  text-align: center;
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
</style>