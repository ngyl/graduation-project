<template>
  <nav class="main-navbar">
    <div class="navbar-container">
      <!-- 移动端菜单按钮 -->
      <div class="mobile-menu-toggle" @click="mobileMenuOpen = !mobileMenuOpen">
        <el-icon :size="24"><Menu /></el-icon>
      </div>
      
      <!-- 导航栏项目 -->
      <div class="navbar-links" :class="{ 'mobile-open': mobileMenuOpen }">
        <router-link 
          v-for="navItem in navItems" 
          :key="navItem.path" 
          :to="navItem.path" 
          class="nav-link"
          :class="{ 'active': isActivePath(navItem.path) }"
          @click="mobileMenuOpen = false"
        >
          <div class="nav-link-content">
            <el-icon :size="20"><component :is="navItem.icon" /></el-icon>
            <span class="nav-text">{{ navItem.label }}</span>
            <span class="nav-link-underline"></span>
          </div>
        </router-link>
      </div>
      
      <!-- 移动端关闭按钮 -->
      <div v-if="mobileMenuOpen" class="mobile-close" @click="mobileMenuOpen = false">
        <el-icon :size="24"><Close /></el-icon>
      </div>
      
      <!-- 主题切换按钮 -->
      <div class="theme-toggle" @click="toggleTheme">
        <transition name="flip">
          <el-icon :size="20" v-if="!isDarkMode"><Sunny /></el-icon>
          <el-icon :size="20" v-else><Moon /></el-icon>
        </transition>
      </div>
    </div>
    
    <!-- 移动端菜单背景 -->
    <div 
      v-if="mobileMenuOpen" 
      class="mobile-menu-backdrop" 
      @click="mobileMenuOpen = false"
    ></div>
  </nav>
</template>

<script setup lang="ts">
import { computed, ref, inject } from 'vue';
import { useRoute } from 'vue-router';
import { House, ChatDotSquare, Files, Calendar, Bell, User, Menu, Close, Moon, Sunny } from '@element-plus/icons-vue';

const route = useRoute();
const mobileMenuOpen = ref(false);
const isDarkMode = inject('isDarkMode', ref(false));
const toggleDarkMode = inject('toggleDarkMode', () => {});

// 导航项数据
const navItems = [
  { path: '/', label: '首页', icon: House },
  { path: '/posts', label: '社区', icon: ChatDotSquare },
  { path: '/resources', label: '资源', icon: Files },
  { path: '/events', label: '活动', icon: Calendar },
  { path: '/messages', label: '消息', icon: Bell },
  { path: '/profile', label: '我的', icon: User },
];

// 判断当前路径是否激活
const isActivePath = (path: string) => {
  if (path === '/') {
    return route.path === '/';
  }
  return route.path.startsWith(path);
};

// 切换主题
const toggleTheme = () => {
  if (typeof toggleDarkMode === 'function') {
    toggleDarkMode();
  }
};
</script>

<style scoped>
.main-navbar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background-color: var(--bg-primary);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 50;
  transition: all 0.3s;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
  overflow: hidden;
}

.navbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 500px;
  margin: 0 auto;
  padding: 10px 0;
  position: relative;
}

.navbar-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.nav-link {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 0;
  text-decoration: none;
  color: var(--text-secondary);
  position: relative;
  transition: all 0.3s;
}

.nav-link-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.nav-text {
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
  transition: all 0.3s;
}

.nav-link:hover {
  color: var(--primary-color);
}

.nav-link.active {
  color: var(--primary-color);
}

.nav-link-underline {
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 0;
  height: 3px;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  transition: width 0.3s;
  border-radius: 3px;
}

.nav-link.active .nav-link-underline {
  width: 100%;
}

/* 主题切换按钮 */
.theme-toggle {
  position: absolute;
  top: -40px;
  right: 15px;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  color: white;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  z-index: 5;
  transition: all 0.3s;
}

.theme-toggle:hover {
  transform: rotate(30deg);
}

/* 翻转动画 */
.flip-enter-active, .flip-leave-active {
  transition: all 0.3s;
}

.flip-enter-from, .flip-leave-to {
  transform: rotateY(90deg);
  opacity: 0;
}

/* 移动端样式 */
.mobile-menu-toggle, .mobile-close {
  display: none;
}

.mobile-menu-backdrop {
  display: none;
}

@media screen and (max-width: 768px) {
  .main-navbar {
    border-radius: 0;
  }
  
  /* 在小屏幕上，显示移动端菜单按钮 */
  .mobile-menu-toggle {
    display: block;
    padding: 10px;
    cursor: pointer;
    z-index: 5;
  }
  
  .navbar-links {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    width: 70%;
    max-width: 300px;
    background-color: var(--bg-primary);
    flex-direction: column;
    justify-content: flex-start;
    padding-top: 60px;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
    z-index: 60;
  }
  
  .navbar-links.mobile-open {
    transform: translateX(0);
  }
  
  .nav-link {
    padding: 15px 20px;
    width: 100%;
    flex-direction: row;
    justify-content: flex-start;
  }
  
  .nav-link-content {
    flex-direction: row;
  }
  
  .nav-text {
    margin-top: 0;
    margin-left: 15px;
    font-size: 16px;
  }
  
  .mobile-close {
    display: block;
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 10px;
    cursor: pointer;
    z-index: 65;
  }
  
  .mobile-menu-backdrop {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 55;
  }
  
  .nav-link-underline {
    display: none;
  }
  
  .theme-toggle {
    top: 10px;
    right: 60px;
  }
}

/* 桌面端样式 */
@media screen and (min-width: 769px) {
  .main-navbar {
    width: 80px;
    height: 100vh;
    top: 0;
    bottom: auto;
    border-radius: 0;
    border-right: 1px solid rgba(0, 0, 0, 0.05);
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
  }
  
  .navbar-container {
    flex-direction: column;
    height: 100%;
    padding: 20px 0;
  }
  
  .navbar-links {
    flex-direction: column;
    height: auto;
  }
  
  .nav-link {
    margin: 10px 0;
  }
  
  .nav-link-underline {
    bottom: -4px;
    left: 30%;
    width: 0;
    height: 3px;
  }
  
  .theme-toggle {
    position: static;
    margin-top: 20px;
  }
}
</style> 