import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/profile/:id',
    name: 'Profile',
    component: () => import('../views/Profile.vue')
  },
  // {
  //   path: '/post/:id',
  //   name: 'PostDetail',
  //   component: () => import('../views/PostDetail.vue')
  // },
  // {
  //   path: '/resource/:id',
  //   name: 'ResourceDetail',
  //   component: () => import('../views/ResourceDetail.vue')
  // },
  // {
  //   path: '/admin',
  //   name: 'Admin',
  //   component: () => import('../views/admin/AdminDashboard.vue'),
  //   meta: { requiresAdmin: true }
  // }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 这里将来添加登录验证和权限验证逻辑
  next()
})

export default router 