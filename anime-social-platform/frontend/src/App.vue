<script setup lang="ts">
import { provide, ref, onMounted } from 'vue'

// 提供全局主题配置
const isDarkMode = ref(false)
provide('isDarkMode', isDarkMode)

const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  document.documentElement.setAttribute('data-theme', isDarkMode.value ? 'dark' : 'light')
}
provide('toggleDarkMode', toggleDarkMode)

// 检查用户首选主题
onMounted(() => {
  // 检查系统首选色彩方案
  const prefersDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
  
  // 设置初始主题
  if (prefersDarkMode) {
    isDarkMode.value = true
    document.documentElement.setAttribute('data-theme', 'dark')
  }
  
  // 添加自定义字体
  document.head.innerHTML += `
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=ZCOOL+KuaiLe&family=ZCOOL+QingKe+HuangYou&display=swap" rel="stylesheet">
  `
})
</script>

<template>
  <el-config-provider>
    <div class="app-container" :class="{ 'dark-mode': isDarkMode }">
      <!-- 背景装饰元素 -->
      <div class="anime-bg-elements">
        <div class="anime-circle anime-circle-1"></div>
        <div class="anime-circle anime-circle-2"></div>
        <div class="anime-circle anime-circle-3"></div>
      </div>
      
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </el-config-provider>
</template>

<style>
.app-container {
  width: 100%;
  min-height: 100vh;
  transition: background-color 0.3s ease;
  position: relative;
  overflow: hidden;
  z-index: 0;
}

.dark-mode {
  background-color: var(--bg-primary);
  color: var(--text-primary);
}

/* 页面过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 动漫风格背景元素 */
.anime-bg-elements {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.anime-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.05;
  filter: blur(40px);
}

.anime-circle-1 {
  background: var(--primary-color);
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
  animation: float 15s infinite alternate ease-in-out;
}

.anime-circle-2 {
  background: var(--secondary-color);
  width: 500px;
  height: 500px;
  bottom: -200px;
  left: -100px;
  animation: float 20s infinite alternate-reverse ease-in-out;
}

.anime-circle-3 {
  background: var(--warning-color);
  width: 200px;
  height: 200px;
  top: 40%;
  right: 10%;
  animation: float 12s infinite alternate ease-in-out;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background-color: rgba(var(--gray-200-rgb, 233, 236, 239), 0.5);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background-color: var(--primary-color);
  background-image: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: var(--primary-dark);
}

/* 超链接全局样式 */
a {
  color: var(--primary-color);
  text-decoration: none;
  transition: all 0.3s;
}

a:hover {
  color: var(--primary-light);
  text-decoration: underline;
}

/* 选中文本样式 */
::selection {
  background-color: var(--primary-color);
  color: white;
}
</style>
