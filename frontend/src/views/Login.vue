<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@/services/api';
import { useAuthStore } from '@/store/authStore';

const router = useRouter();
const authStore = useAuthStore();

const loginForm = ref({
    username: '',
    password: '',
});
const errorMessage = ref('');
const isLoading = ref(false);

async function handleLogin() {
    errorMessage.value = '';
    isLoading.value = true;
    
    try {
        const response = await api.login(loginForm.value);
        
        // Pinia 存储认证信息
        authStore.setLogin(response);

        // 根据 isAdmin 重定向
        if (authStore.isAdmin) {
            router.push({ name: 'AdminProducts' });
        } else {
            router.push({ name: 'CustomerHome' });
        }
        
    } catch (error: any) {
        errorMessage.value = error.message || '登录失败，请检查用户名和密码。';
    } finally {
        isLoading.value = false;
    }
}
</script>

<template>
    <div class="login-container">
        <h2>用户登录</h2>
        <form @submit.prevent="handleLogin" class="login-form">
            <div class="form-group">
                <label for="username">用户名:</label>
                <input type="text" id="username" v-model="loginForm.username" required />
            </div>
            <div class="form-group">
                <label for="password">密码:</label>
                <input type="password" id="password" v-model="loginForm.password" required />
            </div>
            <button type="submit" :disabled="isLoading">
                {{ isLoading ? '登录中...' : '登录' }}
            </button>
            <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
            <p>还没有账号？<router-link to="/register">去注册</router-link></p>
        </form>
    </div>
</template>

<style scoped>
/* 简单的 CSS 样式 */
.login-container { max-width: 400px; margin: 50px auto; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }
.form-group { margin-bottom: 15px; }
.error-message { color: red; }
</style>