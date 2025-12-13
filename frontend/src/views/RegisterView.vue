<!-- frontend\src\views\RegisterView.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/utils/http';

const router = useRouter();

const username = ref('newuser');
const password = ref('123456');
const email = ref('newuser@test.com');
const phone = ref('13888888888');
const isAdmin = ref(false);
const message = ref('');
const isLoading = ref(false);

async function handleRegister() {
    message.value = '';

    if (!username.value || !password.value || !email.value) {
        message.value = '所有字段都是必填项。';
        return;
    }

    isLoading.value = true;
    try {
        const response = await api.post('/register', {
            username: username.value,
            password: password.value,
            email: email.value,
            phone: phone.value,
            isAdmin: isAdmin.value
        });

        if (response.data.success) {
            message.value = `注册成功！用户: ${username.value}，请前往登录页面。`;

            // 注册成功后，延迟跳转到登录页
            setTimeout(() => {
                router.push({ name: 'login' });
            }, 2000);
        } else {
            message.value = response.data.message || '注册失败，请检查用户名是否已存在。';
        }

    } catch (error: any) {
        console.error('注册请求失败:', error);
        message.value = error.response?.data?.message || '注册请求失败，请检查网络或服务器状态。';
    } finally {
        isLoading.value = false;
    }
}
</script>

<template>
    <div class="register-container">
        <h2>注册新用户</h2>
        <form @submit.prevent="handleRegister">
            <div class="form-group">
                <label for="username">用户名:</label>
                <input id="username" type="text" v-model="username" required :disabled="isLoading">
            </div>
            <div class="form-group">
                <label for="password">密码:</label>
                <input id="password" type="password" v-model="password" required :disabled="isLoading">
            </div>
            <div class="form-group">
                <label for="email">邮箱:</label>
                <input id="email" type="email" v-model="email" required :disabled="isLoading">
            </div>
            <div class="form-group">
                <label for="phone">手机号:</label>
                <input id="phone" type="tel" v-model="phone" required :disabled="isLoading">
            </div>
            <!-- 添加是否为管理员的选项 -->
            <div class="form-group">
                <label>
                    <input type="checkbox" v-model="isAdmin" :disabled="isLoading">
                    注册为商家账号
                </label>
            </div>

            <p
                :class="{ 'success-message': message.includes('成功'), 'error-message': message && !message.includes('成功') }">
                {{ message }}</p>

            <button type="submit" :disabled="isLoading">
                {{ isLoading ? '注册中...' : '注册' }}
            </button>
            <router-link :to="{ name: 'login' }">已有账号？去登录</router-link>
        </form>
    </div>
</template>

<style scoped>
/* 样式与 LoginView 相似，可以复用或简化 */
.register-container {
    max-width: 400px;
    margin: 50px auto;
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.form-group {
    margin-bottom: 15px;
}

.form-group label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
}

.form-group input,
.form-group select {
    width: 100%;
    padding: 8px;
    box-sizing: border-box;
    border: 1px solid #ddd;
    border-radius: 4px;
}

button {
    width: 100%;
    padding: 10px;
    background-color: #28a745;
    /* 绿色表示注册 */
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
}

button:disabled {
    background-color: #79c58f;
    cursor: not-allowed;
}

.success-message {
    color: green;
    margin-bottom: 15px;
}

.error-message {
    color: red;
    margin-bottom: 15px;
}

.link-to-login {
    margin-top: 15px;
    text-align: center;
}
</style>