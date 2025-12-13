<!-- frontend/src/views/AdminDashboardView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/utils/http';
import type { RecentOrder } from '@/types';

const router = useRouter();
const authStore = useAuthStore();

// 检查权限
onMounted(() => {
  if (!authStore.isAuthenticated || !authStore.isAdmin) {
    router.push({ name: 'login' });
    return;
  }
});
</script>

<template>
  <div class="admin-dashboard">
    <header class="admin-header">
      <h1>商家管理后台</h1>
      <div class="header-actions">
        <span class="welcome-text">⭐ 欢迎，{{ authStore.username }}</span>
      </div>
    </header>

    <!-- 导航菜单 -->
    <nav class="admin-nav">
      <div class="nav-container">
        <router-link to="/admin/products" class="nav-link">商品管理</router-link>
        <router-link to="/admin/orders" class="nav-link">订单管理</router-link>
        <router-link to="/admin/stats" class="nav-link">数据分析</router-link>
        <router-link to="/" class="nav-link">查看商城</router-link>
      </div>
    </nav>

    <!-- 子路由内容渲染区域 -->
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
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

.admin-nav {
  background-color: #34495e;
  padding: 0 2rem;
}

.nav-container {
  display: flex;
  gap: 1rem;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 1rem;
  display: inline-block;
  transition: background-color 0.3s;
}

.nav-link:hover,
.nav-link.router-link-active {
  background-color: #2c3e50;
}

.admin-content {
  flex: 1;
  padding: 2rem;
}
</style>