// src/store/authStore.ts
import { defineStore } from 'pinia';

interface User {
    userId: number;
    username: string;
    isAdmin: boolean;
}

interface AuthState {
    token: string | null;
    user: User | null;
    isAuthenticated: boolean;
    isAdmin: boolean;
}

export const useAuthStore = defineStore('auth', {
    state: (): AuthState => ({
        // 从 localStorage 恢复状态
        token: localStorage.getItem('token'),
        user: JSON.parse(localStorage.getItem('user') || 'null'),
        isAuthenticated: !!localStorage.getItem('token'),
        isAdmin: JSON.parse(localStorage.getItem('user') || '{}')?.isAdmin || false,
    }),
    
    actions: {
        setLogin(data: { token: string; userId: number; username: string; isAdmin: boolean }) {
            this.token = data.token;
            this.isAuthenticated = true;
            this.isAdmin = data.isAdmin;
            this.user = { 
                userId: data.userId, 
                username: data.username, 
                isAdmin: data.isAdmin 
            };
            
            // 持久化到 localStorage
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify(this.user));
        },
        
        logout() {
            this.token = null;
            this.isAuthenticated = false;
            this.isAdmin = false;
            this.user = null;
            
            // 清除 localStorage
            localStorage.removeItem('token');
            localStorage.removeItem('user');
        },
    },
});