import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login', // ⭐ 关键：名称必须是 'login'
      component: () => import('../views/LoginView.vue')
    },

    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },

    {
      path: '/',
      name: 'home',
      component:  () => import('../views/ProductListView.vue')// ⭐ 确保根路径指向商品列表
    },

  ],

})

export default router
