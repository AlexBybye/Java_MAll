<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/utils/http';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const isLoading = ref(false);

async function handleLogin() {
    errorMessage.value = '';
    if (!username.value || !password.value) {
        errorMessage.value = '用户名和密码不能为空。';
        return;
    }

    isLoading.value = true;
    try {
        const response = await api.post('/login', {
            username: username.value,
            password: password.value
        });

        // 成功处理
        if (response.data.token) {
            // 处理不同的后端响应格式
            const loginData = {
                token: response.data.token,
                userId: response.data.userId || response.data.user?.id || null,
                username: response.data.username || response.data.user?.username || null,
                isAdmin: response.data.isAdmin || response.data.user?.userType === 'admin' || false
            };

            authStore.login(loginData);

            // 根据isAdmin字段决定跳转路由
            if (loginData.isAdmin) {
                // 管理员跳转到管理页面
                router.push({ name: 'admin-dashboard' });
            } else {
                // 普通用户跳转到首页
                const redirectPath = router.currentRoute.value.query.redirect as string | undefined;
                router.push(redirectPath || { name: 'home' });
            }
        } else {
            // 如果后端返回 200 但没有 token (通常不会发生，以防万一)
            errorMessage.value = '登录失败，请检查凭证。';
        }

    } catch (error: any) {
        // 错误处理
        console.error('登录请求失败:', error);

        // 尝试从后端获取更详细的错误消息
        if (error.response?.data?.message) {
            errorMessage.value = error.response.data.message;
        } else {
            errorMessage.value = '登录请求失败，请检查网络或服务器状态。';
        }
    } finally {
        isLoading.value = false;
    }
}
</script>

<template>
    <div class="login-container">
        <h2>用户登录</h2>
        <form @submit.prevent="handleLogin">
            <div class="form-group">
                <label for="username">用户名:</label>
                <input id="username" type="text" v-model="username" required :disabled="isLoading">
            </div>
            <div class="form-group">
                <label for="password">密码:</label>
                <input id="password" type="password" v-model="password" required :disabled="isLoading">
            </div>

            <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

            <button type="submit" :disabled="isLoading">
                {{ isLoading ? '登录中...' : '登录' }}
            </button>

            <!-- 添加注册链接 -->
            <div class="register-link">
                没有账号？<router-link :to="{ name: 'register' }">去注册</router-link>
            </div>
        </form>
    </div>
</template>

<style scoped>
.login-container {
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

.form-group input {
    width: 100%;
    padding: 8px;
    box-sizing: border-box;
    border: 1px solid #ddd;
    border-radius: 4px;
}

button {
    width: 100%;
    padding: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
    margin-bottom: 15px;
    /* 添加底部外边距 */
}

button:disabled {
    background-color: #a0c3e8;
    cursor: not-allowed;
}

.error-message {
    color: red;
    margin-bottom: 15px;
    border: 1px solid red;
    padding: 10px;
    background-color: #ffebeb;
    border-radius: 4px;
}

.register-link {
    text-align: center;
    margin-top: 10px;
}

.register-link a {
    color: #007bff;
    text-decoration: none;
}

.register-link a:hover {
    text-decoration: underline;
}
</style>