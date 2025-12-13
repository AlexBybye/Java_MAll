<!-- d:\JavaMall\frontend\src\views\ProfileView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
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
    <h1>个人信息管理</h1>
    
    <div v-if="isLoading" class="loading">加载中...</div>
    
    <div v-else>
      <div v-if="message" class="success-message">{{ message }}</div>
      <div v-if="error" class="error-message">{{ error }}</div>
      
      <form @submit.prevent="updateProfile" class="profile-form">
        <div class="form-group">
          <label for="email">邮箱:</label>
          <input 
            id="email" 
            type="email" 
            v-model="userForm.email" 
            required 
            :disabled="isSaving"
          />
        </div>
        
        <div class="form-group">
          <label for="phone">电话:</label>
          <input 
            id="phone" 
            type="tel" 
            v-model="userForm.phone" 
            required 
            :disabled="isSaving"
          />
        </div>
        
        <div class="form-actions">
          <button type="submit" :disabled="isSaving">
            {{ isSaving ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 2rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.success-message {
  background-color: #d4edda;
  color: #155724;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.profile-form {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.form-actions {
  text-align: right;
}

.form-actions button {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.form-actions button:hover {
  background-color: #2980b9;
}

.form-actions button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}
</style>