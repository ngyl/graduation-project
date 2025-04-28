<template>
    <div class="admin-page">
        <app-header />
        
        <div class="admin-container">
            <admin-sidebar v-model:collapsed="sidebarCollapsed" />
            
            <div class="admin-content" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
                <slot></slot>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AppHeader from './AppHeader.vue'
import AdminSidebar from './AdminSidebar.vue'

const sidebarCollapsed = ref(false)
</script>

<style scoped>
.admin-page {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: #f5f7fa;
}

.admin-container {
    flex: 1;
    display: flex;
    overflow: hidden;
}

.admin-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    transition: margin-left 0.3s;
}

.admin-content.sidebar-collapsed {
    margin-left: 64px; /* 折叠时侧边栏的宽度 */
}

@media screen and (max-width: 768px) {
    .admin-content {
        padding: 10px;
    }
}
</style> 