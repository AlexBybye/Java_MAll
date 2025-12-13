// src/stores/auth.ts

import { defineStore } from 'pinia';
import type { AuthResponse } from '@/types';

interface AuthState {
  token: string | null;
  userId: number | null;
  username: string | null;
  isAdmin: boolean; // 添加isAdmin字段
}

// 检查 localStorage 中是否存在持久化的 Token
const storedToken = localStorage.getItem('token');
const storedUserId = localStorage.getItem('userId');
const storedUsername = localStorage.getItem('username');
const storedIsAdmin = localStorage.getItem('isAdmin'); // 获取存储的isAdmin值

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: storedToken,
    userId: storedUserId ? parseInt(storedUserId) : null,
    username: storedUsername,
    isAdmin: storedIsAdmin === 'true', // 将字符串转换为布尔值
  }),
  
  // Getters 用于获取计算后的状态
  getters: {
    isAuthenticated: (state) => !!state.token,
    getUserId: (state) => state.userId,
    getUserName: (state) => state.username,
    isAdminUser: (state) => state.isAdmin, // 添加isAdmin getter
  },

  // Actions 用于修改状态（同步或异步）
  actions: {
    // 处理登录成功
    login(data: AuthResponse) {
      // 确保 data 存在且是对象
      if (!data || typeof data !== 'object') {
        console.error("Login data format error:", data);
        return;
      }

      this.token = data.token;
      this.userId = data.userId;
      this.username = data.username;
      this.isAdmin = data.isAdmin; // 设置isAdmin字段

      // 持久化到 localStorage，防止页面刷新丢失
      localStorage.setItem('token', data.token ?? ''); // 使用空字符串兜底
      
      // ⭐ 关键修正：确保 data.userId 存在，并安全调用 toString()
      // 也可以直接使用模板字符串，避免 toString() 崩溃
      localStorage.setItem('userId', `${data.userId ?? ''}`); 
      
      localStorage.setItem('username', data.username ?? '');
      localStorage.setItem('isAdmin', JSON.stringify(data.isAdmin ?? false)); // 添加管理员标识
    },
    // 添加登出方法
    logout() {
      this.token = null;
      this.userId = null;
      this.username = null;
      this.isAdmin = false;
      
      // 清除localStorage
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      localStorage.removeItem('isAdmin');
    }
  }
});