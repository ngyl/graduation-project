<template>
    <div class="app-layout" :class="{ 'with-sidebar': showSidebar }">
        <!-- 主导航栏组件 -->
        <main-navbar />
        
        <!-- 页面内容 -->
        <div class="content-container">
            <!-- 页面顶部导航 -->
            <app-header />
            
            <!-- 主要内容区域 -->
            <main class="main-content">
                <div class="container">
                    <slot></slot>
                </div>
            </main>
            
            <!-- 页面底部装饰元素 -->
            <div class="footer-wave">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 100">
                    <path class="wave-path" d="M0,0L48,8C96,16,192,32,288,37.3C384,43,480,37,576,42.7C672,48,768,64,864,58.7C960,53,1056,27,1152,21.3C1248,16,1344,32,1392,40L1440,48L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
                </svg>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './AppHeader.vue'
import MainNavbar from './MainNavbar.vue'

const route = useRoute()

// 以下路由不显示侧边栏
const sidebarlessRoutes = ['/login', '/register', '/admin']

// 计算是否显示侧边栏
const showSidebar = computed(() => {
    return !sidebarlessRoutes.some(path => route.path.startsWith(path))
})
</script>

<style scoped>
.app-layout {
    display: flex;
    width: 100%;
    min-height: 100vh;
    position: relative;
}

.content-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    padding-bottom: 60px; /* 为移动端底部导航留出空间 */
    transition: all 0.3s;
}

.main-content {
    flex: 1;
    padding: 20px 0;
    position: relative;
    z-index: 1;
}

/* 波浪底部装饰 */
.footer-wave {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    pointer-events: none;
    z-index: 0;
}

.wave-path {
    fill: rgba(var(--primary-color-rgb, 255, 107, 129), 0.05);
}

/* 桌面端样式 */
@media screen and (min-width: 769px) {
    .with-sidebar .content-container {
        margin-left: 80px; /* 对应侧边栏宽度 */
        padding-bottom: 0;
    }

    .container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }
}
</style> 