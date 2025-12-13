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
// 添加鼠标悬停状态
const isHovered = ref(true);

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
    <!-- 全屏背景容器 -->
    <div class="background-container">
        <!-- 背景图片层 -->
        <div class="background-image"></div>
        <!-- 背景呼吸效果层 -->
        <div class="background-breathe"></div>
        
        <!-- 登录表单容器 -->
        <div class="login-wrapper">
            <div 
                class="login-container"
                @mouseenter="isHovered = true"
                @mouseleave="isHovered = false"
                :class="{ 'container-hidden': !isHovered }"
            >
                <div class="login-header">
                    <h2>用户登录</h2>
                    <p class="subtitle">欢迎回来，请登录您的账户</p>
                </div>
                
                <form @submit.prevent="handleLogin" class="login-form">
                    <div class="form-group">
                        <label for="username" class="form-label">用户名</label>
                        <div class="input-wrapper">
                            <i class="input-icon">👤</i>
                            <input 
                                id="username" 
                                type="text" 
                                v-model="username" 
                                required 
                                :disabled="isLoading"
                                class="form-input"
                                placeholder="请输入用户名"
                            >
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label for="password" class="form-label">密码</label>
                        <div class="input-wrapper">
                            <i class="input-icon">🔒</i>
                            <input 
                                id="password" 
                                type="password" 
                                v-model="password" 
                                required 
                                :disabled="isLoading"
                                class="form-input"
                                placeholder="请输入密码"
                            >
                        </div>
                    </div>

                    <!-- 错误消息提示 -->
                    <div 
                        v-if="errorMessage"
                        class="message message-error"
                    >
                        {{ errorMessage }}
                    </div>

                    <button 
                        type="submit" 
                        :disabled="isLoading"
                        class="submit-button"
                    >
                        <span v-if="isLoading" class="loading-spinner"></span>
                        {{ isLoading ? '登录中...' : '立即登录' }}
                    </button>
                    
                    <div class="register-link">
                        <span>没有账号？</span>
                        <router-link :to="{ name: 'register' }" class="link-text">去注册</router-link>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 基础样式重置 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

/* 全屏背景容器 */
.background-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 背景图片层 */
.background-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: url('/YbMall0.jpg') center/cover no-repeat;
    filter: brightness(0.7);
    z-index: 1;
}

/* 背景呼吸效果层 */
.background-breathe {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle at center, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
    animation: breathe 8s ease-in-out infinite;
    z-index: 2;
}

/* 呼吸效果动画 */
@keyframes breathe {
    0%, 100% {
        opacity: 0.3;
        transform: scale(1);
    }
    50% {
        opacity: 0.6;
        transform: scale(1.05);
    }
}

/* 登录表单包装器 */
.login-wrapper {
    position: relative;
    z-index: 3;
    width: 100%;
    max-width: 450px;
    padding: 20px;
}

/* 登录容器 */
.login-container {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    padding: 40px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(10px);
    transition: transform 5s ease, box-shadow 5s ease, opacity 3s ease;
    opacity: 1;
}

.login-container:hover {
    transform: translateY(-5px);
    box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
    opacity: 1;
}

/* 鼠标移开时的隐藏样式 */
.login-container.container-hidden {
    opacity: 0;
}

/* 登录头部 */
.login-header {
    text-align: center;
    margin-bottom: 30px;
}

.login-header h2 {
    color: #333;
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 8px;
}

.subtitle {
    color: #666;
    font-size: 14px;
}

/* 表单样式 */
.login-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.form-label {
    font-weight: 600;
    color: #444;
    font-size: 14px;
}

.input-wrapper {
    position: relative;
}

.input-icon {
    position: absolute;
    left: 15px;
    top: 50%;
    transform: translateY(-50%);
    color: #999;
    font-size: 16px;
}

.form-input {
    width: 100%;
    padding: 14px 15px 14px 45px;
    border: 2px solid #e1e5e9;
    border-radius: 12px;
    font-size: 15px;
    transition: all 0.3s ease;
    background: #fafbfc;
}

.form-input:focus {
    outline: none;
    border-color: #667eea;
    background: white;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input:disabled {
    background: #f5f5f5;
    cursor: not-allowed;
    opacity: 0.7;
}

/* 消息提示样式 */
.message {
    padding: 12px 16px;
    border-radius: 8px;
    font-size: 14px;
    text-align: center;
    font-weight: 500;
    animation: fadeIn 0.3s ease;
}

.message-error {
    background: rgba(220, 53, 69, 0.1);
    color: #dc3545;
    border: 1px solid rgba(220, 53, 69, 0.3);
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 提交按钮样式 */
.submit-button {
    width: 100%;
    padding: 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
}

.submit-button:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.submit-button:active:not(:disabled) {
    transform: translateY(0);
}

.submit-button:disabled {
    opacity: 0.7;
    cursor: not-allowed;
    background: linear-gradient(135deg, #a0aec0 0%, #cbd5e0 100%);
}

/* 加载动画 */
.loading-spinner {
    width: 18px;
    height: 18px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top: 2px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* 注册链接样式 */
.register-link {
    text-align: center;
    margin-top: 20px;
    color: #666;
    font-size: 14px;
}

.link-text {
    color: #667eea;
    font-weight: 600;
    text-decoration: none;
    transition: color 0.3s ease;
}

.link-text:hover {
    color: #764ba2;
    text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 500px) {
    .login-container {
        padding: 30px 20px;
        margin: 10px;
    }
    
    .login-header h2 {
        font-size: 24px;
    }
    
    .form-input {
        padding: 12px 12px 12px 40px;
        font-size: 14px;
    }
    
    .submit-button {
        padding: 14px;
        font-size: 15px;
    }
}
</style>