<script setup lang="ts">
import { provide, ref } from 'vue'

// 提供全局主题配置
const isDarkMode = ref(false)
provide('isDarkMode', isDarkMode)

const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  document.documentElement.setAttribute('data-theme', isDarkMode.value ? 'dark' : 'light')
}
provide('toggleDarkMode', toggleDarkMode)
</script>

<template>
  <el-config-provider>
    <div class="app-container" :class="{ 'dark-mode': isDarkMode }">
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
}

.dark-mode {
  background-color: #121212;
  color: #e0e0e0;
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
</style>
