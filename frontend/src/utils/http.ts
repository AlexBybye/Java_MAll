// src/http.ts

import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '../stores/auth'; // 假设你的 Pinia Store 在此路径

const BASE_URL = '/api'; 

// 1. 创建 Axios 实例
const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 10000, // 请求超时时间 10 秒
  headers: {
    'Content-Type': 'application/json',
  },
});

// 2. 请求拦截器：在发送请求前自动添加 JWT Token
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 在这里获取 Pinia Store 实例
    const authStore = useAuthStore();
    const token = authStore.token;

    // 只有在 token 存在时，才添加到请求头中
    if (token) {
      // 按照 JWT 规范，Token 格式为: Bearer [Token]
      config.headers.Authorization = `Bearer ${token}`;
    }

    // 打印拦截日志（可选，用于调试）
    // console.log(`[Axios] Intercepting request to ${config.url}`, config.headers.Authorization);

    return config;
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);

// 3. 响应拦截器：处理 Token 过期等情况（可选，但推荐）
api.interceptors.response.use(
  (response) => {
    // 2xx 范围内的状态码都会触发该函数
    return response;
  },
  (error) => {
    // 超出 2xx 范围的状态码都会触发该函数
    const authStore = useAuthStore();

    // 检查是否是 401 Unauthorized 错误
    if (error.response && error.response.status === 401) {
      // Token 无效或过期，强制用户登出
      console.warn('[Axios] 401 Unauthorized, forcing logout.');
      authStore.$reset();
      // 可以选择跳转到登录页
      // router.push('/login'); 
    }

    return Promise.reject(error);
  }
);

// 导出配置好的实例
export default api;