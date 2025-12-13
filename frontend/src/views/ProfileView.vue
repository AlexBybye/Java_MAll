<!-- d:\JavaMall\frontend\src\views\ProfileView.vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import api from '@/utils/http';
import type { AuthResponse } from '@/types';

const authStore = useAuthStore();

// 用户信息表单
const userForm = ref({
  email: '',
  phone: ''
});

// 加载状态和消息
const isLoading = ref(false);
const isSaving = ref(false);
const message = ref('');
const error = ref('');

// 计算用户名首字母用于头像
const userInitial = computed(() => {
  return authStore.username ? authStore.username.charAt(0).toUpperCase() : 'U';
});

// 获取用户个人资料
async function fetchProfile() {
  isLoading.value = true;
  error.value = '';
  
  try {
    const response = await api.get('/customer/profile');
    if (response.data.success && response.data.user_profile) {
      const profile = response.data.user_profile;
      userForm.value = {
        email: profile.email || '',
        phone: profile.phone || ''
      };
    }
  } catch (err: any) {
    console.error('获取用户信息失败:', err);
    error.value = '获取用户信息失败，请稍后重试';
  } finally {
    isLoading.value = false;
  }
}

// 更新用户个人资料
async function updateProfile() {
  isSaving.value = true;
  message.value = '';
  error.value = '';
  
  try {
    const response = await api.put('/customer/profile', userForm.value);
    if (response.data.success) {
      message.value = '用户资料更新成功';
      // 3秒后自动清除成功消息
      setTimeout(() => {
        message.value = '';
      }, 3000);
    } else {
      throw new Error(response.data.message || '更新失败');
    }
  } catch (err: any) {
    console.error('更新用户信息失败:', err);
    error.value = err.message || '更新用户信息失败，请稍后重试';
  } finally {
    isSaving.value = false;
  }
}

// 页面加载时获取用户信息
onMounted(() => {
  fetchProfile();
});
</script>

<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="user-avatar" :style="{ backgroundColor: `hsl(${userInitial.charCodeAt(0) * 10}, 70%, 60%)` }">
        {{ userInitial }}
      </div>
      <h1 class="page-title">个人信息管理</h1>
      <p class="username">{{ authStore.username }}</p>
    </div>
    
    <div v-if="isLoading" class="loading">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
    
    <div v-else class="profile-content">
      <!-- 消息提示 -->
      <transition name="fade">
        <div v-if="message" class="success-message">
          <i class="message-icon">✅</i>
          {{ message }}
        </div>
      </transition>
      
      <transition name="fade">
        <div v-if="error" class="error-message">
          <i class="message-icon">❌</i>
          {{ error }}
        </div>
      </transition>
      
      <form @submit.prevent="updateProfile" class="profile-form">
        <div class="form-group">
          <div class="input-container">
            <label for="email" class="floating-label">邮箱</label>
            <input 
              id="email" 
              type="email" 
              v-model="userForm.email" 
              required 
              :disabled="isSaving"
              class="form-input"
            />
          </div>
        </div>
        
        <div class="form-group">
          <div class="input-container">
            <label for="phone" class="floating-label">电话</label>
            <input 
              id="phone" 
              type="tel" 
              v-model="userForm.phone" 
              required 
              :disabled="isSaving"
              class="form-input"
            />
          </div>
        </div>
        
        <div class="form-actions">
          <button type="submit" :disabled="isSaving" class="save-btn">
            <span v-if="isSaving" class="btn-loading">
              <div class="btn-spinner"></div>
              保存中...
            </span>
            <span v-else>保存修改</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 2rem auto;
  padding: 0 2rem;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.profile-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background-color: #3498db;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  font-weight: bold;
  margin: 0 auto 1rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.page-title {
  font-size: 2rem;
  color: #2c3e50;
  margin: 0 0 0.5rem;
}

.username {
  color: #7f8c8d;
  font-size: 1.1rem;
  margin: 0;
}

.loading {
  text-align: center;
  padding: 3rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.profile-content {
  background-color: white;
  border-radius: 12px;
  padding: 2.5rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.profile-content:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

/* 消息提示样式 */
.success-message, .error-message {
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
}

.success-message {
  background-color: #e8f5e9;
  color: #2e7d32;
  border-left: 4px solid #4caf50;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  border-left: 4px solid #f44336;
}

.message-icon {
  font-size: 1.2rem;
}

/* 表单样式 */
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  margin: 0;
}

.input-container {
  position: relative;
  margin-bottom: 0.5rem;
}

.floating-label {
  position: absolute;
  top: 50%;
  left: 1rem;
  transform: translateY(-50%);
  color: #7f8c8d;
  font-size: 1rem;
  pointer-events: none;
  transition: all 0.3s ease;
  background-color: white;
  padding: 0 0.5rem;
}

.form-input {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background-color: white;
}

.form-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.form-input:focus + .floating-label,
.form-input:not(:placeholder-shown) + .floating-label {
  top: 0;
  left: 0.75rem;
  font-size: 0.8rem;
  color: #3498db;
  transform: translateY(-100%);
}

.form-input:disabled {
  background-color: #f5f5f5;
  border-color: #bdbdbd;
  cursor: not-allowed;
}

.form-input:disabled + .floating-label {
  color: #9e9e9e;
}

/* 按钮样式 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}

.save-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.save-btn:hover:not(:disabled) {
  background-color: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.3);
}

.save-btn:active:not(:disabled) {
  transform: translateY(0);
}

.save-btn:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}

.btn-loading {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    margin: 1rem;
    padding: 0 1rem;
  }
  
  .profile-content {
    padding: 1.5rem;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .user-avatar {
    width: 80px;
    height: 80px;
    font-size: 2.5rem;
  }
}
</style>