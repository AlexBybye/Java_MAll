<script setup lang="ts">
import { RouterView } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

// 导航到用户首页
function goToHome() {
  router.push({ path: '/' });
}

// 导航到商家管理界面
function goToAdminDashboard() {
  router.push({ name: 'admin-dashboard' });
}

// 登出
function handleLogout() {
  authStore.logout();
  router.push({ name: 'login' });
}
</script>

<!-- d:\JavaMall\frontend\src\App.vue -->
<template>
  <div id="app">
    <nav class="app-nav" v-if="authStore.isAuthenticated">
      <div class="nav-container">
        <div class="nav-brand">
          <h2 @click="goToHome" class="brand-link">JavaMall</h2>
        </div>
        <div class="nav-menu">
          <template v-if="!authStore.isAdmin">
            <router-link to="/" class="nav-link">商品浏览</router-link>
            <router-link to="/cart" class="nav-link">购物车</router-link>
            <router-link :to="{ name: 'user-orders' }" class="nav-link">我的订单</router-link> <!-- 添加这一行 -->
            <router-link :to="{ name: 'profile' }" class="nav-link">个人信息</router-link>
          </template>
          <template v-if="authStore.isAdmin">
            <router-link :to="{ name: 'admin-products' }" class="nav-link">商品管理</router-link>
            <router-link :to="{ name: 'admin-orders' }" class="nav-link">订单管理</router-link>
            <router-link :to="{ name: 'profile' }" class="nav-link">个人信息</router-link>
          </template>
        </div>
        <div class="nav-user">
          <span class="username">{{ authStore.username }}</span>
          <button @click="handleLogout" class="logout-btn">退出</button>
        </div>
      </div>
      <div>
        <img src="../public/YbMall.jpeg" alt="Admin Icon" class="admin-icon" style="display: block; margin: 0 auto;" />
      </div>
    </nav>
    <main class="main-content">

      <RouterView />

    </main>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background-color: #f5f5f5;
}

#app {
  min-height: 100vh;
}

/* 导航栏样式 */
.app-nav {
  background-color: #2c3e50;
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-brand .brand-link {
  margin: 0;
  font-size: 1.5rem;
  color: white;
  text-decoration: none;
  cursor: pointer;
}

.nav-brand .brand-link:hover {
  color: #3498db;
}

.nav-menu {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: #34495e;
}

.nav-link.router-link-active {
  background-color: #3498db;
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.username {
  font-size: 0.9rem;
}

.logout-btn {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.logout-btn:hover {
  background-color: #c0392b;
}

/* 主内容区域 */
.main-content {
  min-height: calc(100vh - 80px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-container {
    flex-direction: column;
    gap: 1rem;
  }

  .nav-menu {
    gap: 1rem;
  }
}
</style>