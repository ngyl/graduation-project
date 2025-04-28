<template>
  <div class="navbar-container">
    <el-menu 
      mode="horizontal" 
      :ellipsis="false"
      class="main-navbar"
      :router="true"
      :background-color="isDarkMode ? 'var(--bg-primary)' : ''"
      :text-color="isDarkMode ? 'var(--text-primary)' : ''"
      :active-text-color="'var(--primary-color)'"
    >
      <div class="logo">
        <router-link to="/">
          <h1>动漫社交平台</h1>
        </router-link>
      </div>
      
      <div class="flex-grow"></div>
      
      <el-menu-item index="/">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-menu-item>
      
      <el-menu-item index="/posts">
        <el-icon><Document /></el-icon>
        <span>社区</span>
      </el-menu-item>
      
      <el-menu-item index="/events">
        <el-icon><Calendar /></el-icon>
        <span>活动</span>
      </el-menu-item>
      
      <template v-if="isLoggedIn">
        <el-menu-item index="/friends">
          <el-icon><UserFilled /></el-icon>
          <span>好友</span>
        </el-menu-item>
        
        <el-menu-item index="/favorites">
          <el-icon><Star /></el-icon>
          <span>我的收藏</span>
        </el-menu-item>
        
        <el-menu-item index="/messages">
          <el-icon><ChatDotRound /></el-icon>
          <span>消息</span>
          <el-badge v-if="unreadCount > 0" :value="unreadCount" class="message-badge" />
        </el-menu-item>
      </template>
      
      <!-- 用户菜单 -->
      <template v-if="isLoggedIn">
        <el-dropdown class="user-dropdown" trigger="click">
          <div class="user-avatar">
            <el-avatar :size="32" :src="userInfo?.avatar || defaultAvatar" />
            <span class="username-text hidden-xs">{{ userInfo?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleDropdownClick(`/profile/${userInfo?.id}`)">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item @click="handleDropdownClick('/favorites')">
                <el-icon><Star /></el-icon>我的收藏
              </el-dropdown-item>
              <el-dropdown-item @click="handleDropdownClick('/messages')">
                <el-icon><ChatDotRound /></el-icon>消息通知
              </el-dropdown-item>
              <el-dropdown-item v-if="isAdmin" @click="handleDropdownClick('/admin')">
                <el-icon><Setting /></el-icon>管理控制台
              </el-dropdown-item>
              <el-dropdown-item divided>
                <div class="theme-toggle">
                  <span>暗黑模式</span>
                  <el-switch v-model="darkMode" @change="toggleTheme" />
                </div>
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      
      <!-- 未登录菜单 -->
      <template v-else>
        <div class="auth-buttons">
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </div>
      </template>
      
      <!-- 主题切换按钮 -->
      <div class="theme-toggle-button">
        <el-button circle @click="toggleTheme" :icon="isDarkMode ? 'Sunny' : 'Moon'">
          <el-icon v-if="isDarkMode"><Sunny /></el-icon>
          <el-icon v-else><Moon /></el-icon>
        </el-button>
      </div>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';
import { 
  HomeFilled, 
  Document, 
  Calendar, 
  UserFilled, 
  Star, 
  ChatDotRound, 
  User,
  Setting,
  SwitchButton,
  ArrowDown,
  Sunny,
  Moon
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 从App.vue注入的暗黑模式状态
const isDarkMode = inject('isDarkMode', ref(false));
const toggleDarkMode = inject('toggleDarkMode', () => {});
const darkMode = computed({
  get: () => isDarkMode.value,
  set: (value) => {
    isDarkMode.value = value;
  }
});

// 切换主题
const toggleTheme = () => {
  toggleDarkMode();
};

// 用户信息
const isLoggedIn = computed(() => userStore.isLoggedIn);
const userInfo = computed(() => userStore.userInfo);
const isAdmin = computed(() => userStore.isAdmin);

// 模拟未读消息数量
const unreadCount = ref(0);

// 下拉菜单点击处理
const handleDropdownClick = (path: string) => {
  router.push(path);
};

// 退出登录
const handleLogout = async () => {
  try {
    await userStore.logoutUser();
    ElMessage.success('退出登录成功');
    router.push('/');
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '退出登录失败');
  }
};
</script>

<style scoped>
.navbar-container {
  position: sticky;
  top: 0;
  width: 100%;
  z-index: 100;
  box-shadow: var(--shadow-sm);
  background-color: var(--bg-primary);
  transition: var(--transition-base);
}

.main-navbar {
  display: flex;
  align-items: center;
  height: var(--header-height);
  padding: 0 var(--spacing-4);
  max-width: var(--container-width);
  margin: 0 auto;
  border-bottom: none !important;
}

.logo {
  padding: var(--spacing-2) var(--spacing-4) var(--spacing-2) 0;
}

.logo h1 {
  margin: 0;
  font-size: var(--font-size-xl);
  color: var(--primary-color);
  transition: var(--transition-fast);
}

.logo h1:hover {
  transform: scale(1.05);
}

.flex-grow {
  flex-grow: 1;
}

:deep(.el-menu-item) {
  font-size: var(--font-size-md);
  height: var(--header-height);
  transition: var(--transition-base);
}

.user-dropdown {
  margin-left: var(--spacing-4);
  height: 100%;
  display: flex;
  align-items: center;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  cursor: pointer;
  padding: 0 var(--spacing-2);
  border-radius: var(--border-radius-md);
  transition: var(--transition-base);
}

.user-avatar:hover {
  background-color: var(--bg-secondary);
}

.username-text {
  margin: 0 var(--spacing-2);
  color: var(--text-primary);
  font-weight: 500;
}

.auth-buttons {
  display: flex;
  gap: var(--spacing-3);
  align-items: center;
  height: 100%;
  padding: 0 var(--spacing-2);
}

.message-badge {
  margin-top: -8px;
  margin-right: -4px;
}

.theme-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--spacing-2);
  width: 100%;
}

.theme-toggle-button {
  margin-left: var(--spacing-2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-navbar {
    padding: 0 var(--spacing-2);
  }
  
  :deep(.el-menu-item) span {
    display: none;
  }
  
  .auth-buttons .el-button {
    padding: 6px 10px;
  }
}

@media (max-width: 480px) {
  .logo h1 {
    font-size: var(--font-size-lg);
  }
  
  .auth-buttons .el-button {
    padding: 4px 8px;
    font-size: 12px;
  }
}
</style> 