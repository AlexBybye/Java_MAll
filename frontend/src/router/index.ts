// router/index.ts

import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProductListView from '../views/ProductListView.vue'
import CartView from '../views/CartView.vue'
// 引入所有 Admin 视图
import AdminDashboardView from '../views/AdminDashboardView.vue' // 主 Admin 界面，用于导航
import AdminProductManagementView from '../views/AdminProductManagementView.vue'
import AdminOrderManagementView from '../views/AdminOrderManagementView.vue'
// 假设 ProfileView.vue 和 AdminStatsView.vue 仍需创建，这里使用占位
import ProfileView from '../views/ProfileView.vue' // 导入ProfileView组件
import AdminStatsView from '../views/AdminStatsView.vue'
import AdminProductAddView from '../views/AdminProductAddView.vue'
import AdminOrderDetailView from '../views/AdminOrderDetailView.vue'
import AdminProductEditView from '../views/AdminProductEditView.vue';
// 引入 auth store 用于导航守卫
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 认证路由
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },

    // 普通用户/公共路由
    {
      path: '/',
      name: 'home',
      component: ProductListView
    },
    {
      path: '/cart', // 解决购物车 404
      name: 'cart',
      component: CartView,
      meta: { requiresAuth: true } // 需要登录
    },
    {
      path: '/profile', // 普通用户和商家共用
      name: 'profile',
      component: ProfileView, // 使用新创建的ProfileView组件
      meta: { requiresAuth: true }
    },

    // 商家 (Admin) 路由，全部嵌套在 AdminDashboard 下
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: AdminDashboardView, // Admin 主界面，用于承载 Admin 导航
      meta: { requiresAuth: true, requiresAdmin: true }, // 需要登录和管理员权限
      children: [
        {
          path: 'products', // 完整路径: /admin/products
          name: 'admin-products',
          component: AdminProductManagementView
        },
        {
          path: 'products/add', // 完整路径: /admin/products/add
          name: 'admin-product-add',
          component: AdminProductAddView
        },
        // 1. 导入编辑组件


        // 2. 在admin路由的children中添加编辑路由
        {
          path: 'products/edit/:id', // 完整路径: /admin/products/edit/:id
          name: 'admin-product-edit',
          component: AdminProductEditView
        },
        {
          path: 'orders', // 完整路径: /admin/orders
          name: 'admin-orders',
          component: AdminOrderManagementView
        },
        {
          path: 'stats', // 完整路径: /admin/stats
          name: 'admin-stats',

          component: AdminStatsView
        },

        {
          path: 'orders/:id', // 完整路径: /admin/orders/:id
          name: 'admin-order-detail',
          component: AdminOrderDetailView
        }
      ]
    }
  ]
})

// ✅ 添加全局导航守卫 (解决问题 2：商家用户无法进入商家界面)
router.beforeEach((to, from) => {
  const authStore = useAuthStore()

  // 检查是否需要登录
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // 未登录，重定向到登录页
    return { name: 'login' }
  }

  // 检查是否需要管理员权限 (解决问题 2)
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    // 非管理员用户尝试访问 admin 路由，重定向到普通用户首页
    return { name: 'home' }
  }

  // 确保已登录的管理员被重定向到 /admin，普通用户被重定向到 /
  if (to.name === 'login' && authStore.isAuthenticated) {
    if (authStore.isAdmin) {
      return { name: 'admin-products' } // 登录成功后默认进入产品管理
    } else {
      return { name: 'home' }
    }
  }

  return true
})


export default router