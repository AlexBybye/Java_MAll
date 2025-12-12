<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/utils/http'; // 引入封装好的 api
import { useAuthStore } from '@/stores/auth'; // 引入认证 Store
// 只需要 AuthResponse 类型，因为后端现在返回的就是这个平铺结构
import type { AuthResponse } from '@/types'; 

const router = useRouter();
const authStore = useAuthStore();

// 绑定表单数据
const username = ref('');
const password = ref('');
const errorMessage = ref('');
const isLoading = ref(false);

// 登录处理函数
async function handleLogin() {
    errorMessage.value = '';
    if (!username.value || !password.value) {
        errorMessage.value = '用户名和密码不能为空。';
        return;
    }

    isLoading.value = true;
    try {
        // 调用后端登录 API，现在我们期望它返回 AuthResponse (平铺结构)
        const response = await api.post<AuthResponse>('/login', {
            username: username.value,
            password: password.value
        });
        
        // 成功处理
        if (response.data.token) {
            // ⭐ 核心修正：后端返回结构正确，直接将数据传递给 Pinia Store
            authStore.login(response.data);

            // 2. 检查是否有重定向参数，否则跳转到首页
            const redirectPath = router.currentRoute.value.query.redirect as string | undefined;
            router.push(redirectPath || { path: '/' });
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
</style>