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
    <!-- 全屏背景容器 -->
    <div class="background-container">
        <!-- 背景图片层 -->
        <div class="background-image"></div>
        <!-- 背景呼吸效果层 -->
        <div class="background-breathe"></div>
        
        <!-- 注册表单容器 -->
        <div class="register-wrapper">
            <div class="register-container">
                <div class="register-header">
                    <h2>注册新用户</h2>
                    <p class="subtitle">加入我们，享受更多优惠和服务</p>
                </div>
                
                <form @submit.prevent="handleRegister" class="register-form">
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
                    
                    <div class="form-group">
                        <label for="email" class="form-label">邮箱</label>
                        <div class="input-wrapper">
                            <i class="input-icon">📧</i>
                            <input 
                                id="email" 
                                type="email" 
                                v-model="email" 
                                required 
                                :disabled="isLoading"
                                class="form-input"
                                placeholder="请输入邮箱"
                            >
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label for="phone" class="form-label">手机号</label>
                        <div class="input-wrapper">
                            <i class="input-icon">📱</i>
                            <input 
                                id="phone" 
                                type="tel" 
                                v-model="phone" 
                                required 
                                :disabled="isLoading"
                                class="form-input"
                                placeholder="请输入手机号"
                            >
                        </div>
                    </div>
                    
                    <!-- 添加是否为管理员的选项 -->
                    <div class="form-group checkbox-group">
                        <input 
                            type="checkbox" 
                            id="isAdmin" 
                            v-model="isAdmin" 
                            :disabled="isLoading"
                            class="checkbox-input"
                        >
                        <label for="isAdmin" class="checkbox-label">
                            注册为商家账号
                        </label>
                    </div>

                    <!-- 消息提示 -->
                    <div 
                        v-if="message"
                        :class="{ 
                            'message': true,
                            'message-success': message.includes('成功'), 
                            'message-error': message && !message.includes('成功') 
                        }"
                    >
                        {{ message }}
                    </div>

                    <button 
                        type="submit" 
                        :disabled="isLoading"
                        class="submit-button"
                    >
                        <span v-if="isLoading" class="loading-spinner"></span>
                        {{ isLoading ? '注册中...' : '立即注册' }}
                    </button>
                    
                    <div class="login-link">
                        <span>已有账号？</span>
                        <router-link :to="{ name: 'login' }" class="link-text">去登录</router-link>
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

/* 注册表单包装器 */
.register-wrapper {
    position: relative;
    z-index: 3;
    width: 100%;
    max-width: 450px;
    padding: 20px;
}

/* 注册容器 */
.register-container {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    padding: 40px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(10px);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.register-container:hover {
    transform: translateY(-5px);
    box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
}

/* 注册头部 */
.register-header {
    text-align: center;
    margin-bottom: 30px;
}

.register-header h2 {
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
.register-form {
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

/* 复选框样式 */
.checkbox-group {
    flex-direction: row;
    align-items: center;
    gap: 10px;
}

.checkbox-input {
    width: 18px;
    height: 18px;
    accent-color: #667eea;
    cursor: pointer;
}

.checkbox-label {
    font-weight: 400;
    color: #555;
    cursor: pointer;
    user-select: none;
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

.message-success {
    background: rgba(40, 167, 69, 0.1);
    color: #28a745;
    border: 1px solid rgba(40, 167, 69, 0.3);
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

/* 登录链接样式 */
.login-link {
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
    .register-container {
        padding: 30px 20px;
        margin: 10px;
    }
    
    .register-header h2 {
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