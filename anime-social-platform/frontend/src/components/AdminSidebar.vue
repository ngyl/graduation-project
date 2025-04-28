<template>
  <div class="admin-sidebar-container" :class="{ 'collapsed': isCollapse }">
    <el-menu
      class="admin-sidebar"
      :default-active="activeIndex"
      :router="true"
      :collapse="isCollapse"
      :background-color="isDarkMode ? 'var(--bg-tertiary)' : 'var(--bg-primary)'"
      :text-color="isDarkMode ? 'var(--text-light)' : 'var(--text-primary)'"
      :active-text-color="'var(--primary-color)'"
    >
      <div class="sidebar-header">
        <transition name="fade" mode="out-in">
          <h3 v-if="!isCollapse" class="header-title">管理控制台</h3>
          <el-icon v-else class="header-icon"><Setting /></el-icon>
        </transition>
      </div>
      
      <el-menu-item index="/admin" class="admin-menu-item">
        <el-icon><DataAnalysis /></el-icon>
        <template #title>仪表盘</template>
      </el-menu-item>
      
      <el-menu-item index="/admin/users" class="admin-menu-item">
        <el-icon><User /></el-icon>
        <template #title>用户管理</template>
      </el-menu-item>
      
      <el-menu-item index="/admin/posts" class="admin-menu-item">
        <el-icon><Document /></el-icon>
        <template #title>帖子管理</template>
      </el-menu-item>
      
      <el-menu-item index="/admin/resources" class="admin-menu-item">
        <el-icon><FolderOpened /></el-icon>
        <template #title>资源管理</template>
      </el-menu-item>
      
      <el-menu-item index="/admin/events" class="admin-menu-item">
        <el-icon><Calendar /></el-icon>
        <template #title>活动管理</template>
      </el-menu-item>
      
      <el-menu-item index="/admin/tags" class="admin-menu-item">
        <el-icon><PriceTag /></el-icon>
        <template #title>标签管理</template>
      </el-menu-item>
    </el-menu>
    
    <div class="sidebar-footer">
      <el-tooltip 
        :content="isCollapse ? '展开菜单' : '收起菜单'" 
        placement="right" 
        :disabled="!isCollapse"
      >
        <el-button 
          :type="isDarkMode ? 'primary' : 'default'" 
          circle 
          @click="toggleCollapse"
          class="collapse-btn"
        >
          <el-icon v-if="isCollapse"><DArrowRight /></el-icon>
          <el-icon v-else><DArrowLeft /></el-icon>
        </el-button>
      </el-tooltip>
      
      <el-tooltip 
        content="返回前台" 
        placement="right" 
        :disabled="!isCollapse"
      >
        <el-button 
          type="danger"
          circle
          class="back-btn"
          @click="backToSite"
        >
          <el-icon><Back /></el-icon>
        </el-button>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  Setting, 
  User, 
  Document, 
  FolderOpened, 
  Calendar, 
  DataAnalysis, 
  DArrowLeft, 
  DArrowRight,
  Back,
  PriceTag
} from '@element-plus/icons-vue';

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:collapsed']);
const route = useRoute();
const router = useRouter();
const isDarkMode = inject('isDarkMode', false);

const isCollapse = computed({
  get: () => props.collapsed,
  set: (value) => emit('update:collapsed', value)
});

const activeIndex = computed(() => route.path);

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
};

const backToSite = () => {
  router.push('/');
};
</script>

<style scoped>
.admin-sidebar-container {
  height: 100vh;
  position: relative;
  transition: var(--transition-base);
  display: flex;
  flex-direction: column;
}

.admin-sidebar {
  height: 100%;
  border-right: 1px solid var(--gray-200);
  transition: var(--transition-base);
}

[data-theme="dark"] .admin-sidebar {
  border-right-color: var(--gray-700);
}

.sidebar-header {
  padding: var(--spacing-6) 0;
  text-align: center;
  border-bottom: 1px solid var(--gray-200);
  transition: var(--transition-base);
  display: flex;
  align-items: center;
  justify-content: center;
}

[data-theme="dark"] .sidebar-header {
  border-bottom-color: var(--gray-700);
}

.header-title {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--primary-color);
  transition: var(--transition-fast);
}

.header-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.admin-menu-item {
  transition: var(--transition-base);
}

.admin-menu-item:hover {
  background-color: var(--bg-secondary) !important;
}

.sidebar-footer {
  margin-top: auto;
  padding: var(--spacing-4);
  display: flex;
  justify-content: center;
  gap: var(--spacing-3);
}

.collapse-btn,
.back-btn {
  box-shadow: var(--shadow-sm);
  transition: var(--transition-base);
}

.collapse-btn:hover,
.back-btn:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

/* 折叠动画 */
.collapsed .sidebar-header {
  padding: var(--spacing-4) 0;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 