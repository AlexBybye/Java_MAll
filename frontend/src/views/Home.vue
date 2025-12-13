<script setup lang="ts">
import { computed } from 'vue';
import { useAuthStore } from '@/store/authStore';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const isAdmin = computed(() => authStore.isAdmin);

const customerNavs = [
    { name: '商品浏览', path: '/customer' },
    { name: '购物车管理', path: '/cart' },
    { name: '个人信息管理', path: '/profile' },
];

const adminNavs = [
    { name: '产品管理', path: '/admin/products' },
    { name: '订单管理', path: '/admin/orders' },
    { name: '销售统计', path: '/admin/stats' },
    { name: '个人信息管理', path: '/profile' },
];

function handleLogout() {
    authStore.logout();
    router.push('/login');
}
</script>

<template>
    <div class="app-layout">
        <header class="main-header">
            <div class="logo-container">
                <h1 class="logo">JavaMall</h1>
                <span class="subtitle">{{ isAdmin ? '商家管理后台' : '用户商城' }}</span>
            </div>

            <nav class="main-nav">
                <router-link v-for="nav in (isAdmin ? adminNavs : customerNavs)" :key="nav.name" :to="nav.path"
                    class="nav-link" active-class="nav-link-active">
                    {{ nav.name }}
                </router-link>

                <div class="user-section">
                    <span class="username">{{ authStore.user?.username }}</span>
                    <button @click="handleLogout" class="logout-button">
                        <i class="logout-icon">🔒</i> 登出
                    </button>
                </div>
            </nav>
        </header>

        <main class="main-content">
            <router-view />
        </main>
    </div>
</template>

<style scoped>
/* 基础样式重置 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.app-layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f5f7fa;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 导航栏样式 */
.main-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 2rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    position: sticky;
    top: 0;
    z-index: 1000;
}

.logo-container {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.logo {
    font-size: 1.75rem;
    font-weight: 700;
    letter-spacing: -0.5px;
    margin: 0;
}

.subtitle {
    font-size: 0.875rem;
    opacity: 0.9;
    font-weight: 400;
}

/* 导航链接样式 */
.main-nav {
    display: flex;
    align-items: center;
    gap: 2rem;
}

.nav-link {
    color: white;
    text-decoration: none;
    font-weight: 500;
    padding: 0.5rem 0.75rem;
    border-radius: 8px;
    transition: all 0.3s ease;
    position: relative;
}

.nav-link:hover {
    background-color: rgba(255, 255, 255, 0.1);
    transform: translateY(-1px);
}

.nav-link-active {
    background-color: rgba(255, 255, 255, 0.2);
    font-weight: 600;
}

.nav-link-active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 20px;
    height: 2px;
    background-color: white;
    border-radius: 1px;
}

/* 用户信息和登出按钮 */
.user-section {
    display: flex;
    align-items: center;
    gap: 1rem;
    background-color: rgba(255, 255, 255, 0.1);
    padding: 0.5rem 1rem;
    border-radius: 50px;
}

.username {
    font-weight: 500;
    font-size: 0.9rem;
}

.logout-button {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background-color: rgba(255, 255, 255, 0.2);
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 0.875rem;
}

.logout-button:hover {
    background-color: rgba(255, 255, 255, 0.3);
    transform: translateY(-1px);
}

.logout-icon {
    font-size: 1rem;
}

/* 主内容区域 */
.main-content {
    flex: 1;
    padding: 2rem;
    max-width: 1400px;
    margin: 0 auto;
    width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .main-header {
        flex-direction: column;
        gap: 1rem;
        padding: 1rem;
    }

    .main-nav {
        flex-wrap: wrap;
        gap: 1rem;
        justify-content: center;
    }

    .user-section {
        margin-left: auto;
    }

    .main-content {
        padding: 1rem;
    }

    .logo {
        font-size: 1.5rem;
    }
}

@media (max-width: 480px) {
    .main-nav {
        flex-direction: column;
        align-items: center;
        width: 100%;
    }

    .nav-link {
        width: 100%;
        text-align: center;
    }

    .user-section {
        flex-direction: column;
        width: 100%;
        gap: 0.5rem;
    }
}
</style>