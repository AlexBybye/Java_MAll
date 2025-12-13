<!-- frontend\src\views\AdminProductManagementView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/utils/http';
import type { AdminProduct } from '@/types';

const router = useRouter();
const authStore = useAuthStore();

// 商品列表
const products = ref<AdminProduct[]>([]);

// 加载状态
const isLoading = ref(true);

// 检查权限
onMounted(() => {
  if (!authStore.isAuthenticated || !authStore.isAdmin) {
    router.push({ name: 'login' });
    return;
  }
  loadProducts();
});

// 加载商品列表
async function loadProducts() {
  try {
    isLoading.value = true;
    const response = await api.get('/product');
    if (response.data.success) {
      products.value = response.data.data;
    }
  } catch (error) {
    console.error('加载商品列表失败:', error);
  } finally {
    isLoading.value = false;
  }
}

// 添加商品
function addProduct() {
  router.push({ name: 'admin-product-add' });
}

// 编辑商品
function editProduct(productId: number) {
  router.push({ name: 'admin-product-edit', params: { id: productId } });
}

// 删除商品
async function deleteProduct(productId: number) {
  if (confirm('确定要删除这个商品吗？')) {
    try {
      const response = await api.delete(`/product/${productId}`);
      if (response.data.success) {
        // 重新加载商品列表
        loadProducts();
      }
    } catch (error) {
      console.error('删除商品失败:', error);
    }
  }
}

// 返回仪表板
function goBackToDashboard() {
  router.push({ name: 'admin-dashboard' });
}
</script>

<template>
  <div class="admin-product-management">
    <header class="management-header">
      <h1>商品管理</h1>
      <div class="header-actions">
        <button @click="goBackToDashboard" class="back-btn">返回仪表板</button>
        <button @click="addProduct" class="add-btn">添加商品</button>
      </div>
    </header>

    <div class="product-list">
      <div v-if="isLoading" class="loading">加载中...</div>
      <div v-else-if="products.length === 0" class="no-data">
        暂无商品数据
      </div>
      <div v-else class="products-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>商品名称</th>
              <th>价格</th>
              <th>库存</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.id">
              <td>{{ product.id }}</td>
              <td>{{ product.name }}</td>
              <td>¥{{ product.price }}</td>
              <td>{{ product.stockQuantity || product.stock }}</td>
              <td class="actions">
                <button @click="editProduct(product.id)" class="edit-btn">编辑</button>
                <button @click="deleteProduct(product.id)" class="delete-btn">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-product-management {
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

.header-actions {
  display: flex;
  gap: 1rem;
}

.back-btn,
.add-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.back-btn {
  background-color: #95a5a6;
  color: white;
}

.back-btn:hover {
  background-color: #7f8c8d;
}

.add-btn {
  background-color: #27ae60;
  color: white;
}

.add-btn:hover {
  background-color: #229954;
}

.product-list {
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

.products-table {
  overflow-x: auto;
}

.products-table table {
  width: 100%;
  border-collapse: collapse;
}

.products-table th,
.products-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.products-table th {
  background-color: #f8f9fa;
  font-weight: bold;
  color: #495057;
}

.products-table tr:hover {
  background-color: #f8f9fa;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.edit-btn,
.delete-btn {
  padding: 0.25rem 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.edit-btn {
  background-color: #3498db;
  color: white;
}

.edit-btn:hover {
  background-color: #2980b9;
}

.delete-btn {
  background-color: #e74c3c;
  color: white;
}

.delete-btn:hover {
  background-color: #c0392b;
}
</style>