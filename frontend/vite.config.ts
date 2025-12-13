import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      // 将所有以 /api 开头的请求代理到后端服务器
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true, // 允许跨域
        // 不需要 rewrite，因为后端已经配置了 /api 路径
      }
    }
  }
})