import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw, RouteRecordNormalized } from 'vue-router'
import { getCurrentUser } from '@/api/auth'
import { useUserStore } from '@/stores/user'

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
    path: '/search',
    name: 'Search',
    component: () => import('../views/Search.vue')
  },
  {
    path: '/profile/:id',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/posts',
    name: 'PostList',
    component: () => import('../views/PostList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/posts/new',
    name: 'PostNew',
    component: () => import('../views/PostEdit.vue'),
    meta: { requiresAuth: true, isNewPost: true }
  },
  {
    path: '/posts/:id',
    name: 'PostDetail',
    component: () => import('../views/PostDetail.vue')
  },
  {
    path: '/posts/:id/edit',
    name: 'PostEdit',
    component: () => import('../views/PostEdit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/resources',
    name: 'ResourceList',
    component: () => import('../views/ResourceList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/resources/:id',
    name: 'ResourceDetail',
    component: () => import('../views/ResourceDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/resource-upload',
    name: 'ResourceUpload',
    component: () => import('../views/ResourceUpload.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/friends',
    name: 'Friends',
    component: () => import('../views/Friends.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('../views/Messages.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/Favorites.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/events',
    name: 'Events',
    component: () => import('../views/Events.vue')
  },
  // 管理员路由
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/admin/Dashboard.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/admin/UserManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/posts',
    name: 'AdminPosts',
    component: () => import('../views/admin/PostManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/resources',
    name: 'AdminResources',
    component: () => import('../views/admin/ResourceManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/events',
    name: 'AdminEvents',
    component: () => import('../views/admin/EventManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/tags',
    component: () => import('../views/admin/TagManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 获取用户store
  const userStore = useUserStore();
  
  // 如果路由需要身份验证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    try {
      // 判断是否已登录，先尝试从store中获取
      let isLoggedIn = userStore.isLoggedIn;
      
      // 如果store中没有登录信息，则调用API获取
      if (!isLoggedIn) {
        console.log('路由守卫: 未检测到本地登录状态，正在验证用户登录状态...');
        const res = await getCurrentUser();
        isLoggedIn = res.data.code === 200;
        
        // 如果成功获取到用户信息，更新store
        if (isLoggedIn && res.data.data) {
          // 正确地更新ref对象的值
          userStore.$patch({ user: res.data.data });
        }
      }
      
      // 如果未登录，重定向到登录页
      if (!isLoggedIn) {
        console.log('路由守卫: 用户未登录，重定向至登录页');
        next({ 
          path: '/login', 
          query: { redirect: to.fullPath } 
        });
        return;
      }
      
      // 检查是否需要管理员权限
      if (to.matched.some((record: RouteRecordNormalized) => record.meta.requiresAdmin)) {
        const isAdmin = userStore.user?.isAdmin === true;
        
        if (!isAdmin) {
          console.log('路由守卫: 用户不是管理员，无权访问');
          next({ path: '/' });
          return;
        }
      }
      
      // 通过所有验证，允许访问
      next();
    } catch (error) {
      console.error('路由守卫: 验证用户状态失败', error);
      
      // 验证失败，清除可能存在的过期登录状态
      userStore.logoutUser();
      
      // 重定向到登录页面
      next({ 
        path: '/login', 
        query: { redirect: to.fullPath } 
      });
    }
  } else {
    // 不需要认证的路由直接通过
    next();
  }
})

export default router 