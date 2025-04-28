<template>
  <el-button
    :type="isFavorited ? 'danger' : 'default'"
    size="small"
    @click="toggleFavorite"
    :loading="loading"
    class="favorite-button"
  >
    <el-icon>
      <Star v-if="isFavorited" :style="{ color: isFavorited ? 'var(--error-color)' : 'inherit' }" />
      <StarFilled v-else />
    </el-icon>
    <span>{{ isFavorited ? '已收藏' : '收藏' }}</span>
    <span v-if="showCount" class="count">({{ favoriteCount }})</span>
  </el-button>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue';
import { ElMessage } from 'element-plus';
import { Star, StarFilled } from '@element-plus/icons-vue';
import { addFavorite, removeFavorite, checkFavorite, getResourceFavoriteCount } from '../api/favorite';
import { useUserStore } from '../stores/user';

const props = defineProps({
  resourceId: {
    type: Number,
    required: true
  },
  showCount: {
    type: Boolean,
    default: true
  }
});

const userStore = useUserStore();
const isFavorited = ref(false);
const favoriteCount = ref(0);
const loading = ref(false);

// 在组件加载时检查是否已收藏
onMounted(async () => {
  if (userStore.isLoggedIn) {
    try {
      // 检查是否已收藏
      const checkRes = await checkFavorite(props.resourceId, userStore.userInfo.id);
      if (checkRes.data.code === 200) {
        isFavorited.value = checkRes.data.data;
      }
      
      // 获取收藏次数
      if (props.showCount) {
        const countRes = await getResourceFavoriteCount(props.resourceId);
        if (countRes.data.code === 200) {
          favoriteCount.value = countRes.data.data;
        }
      }
    } catch (error) {
      console.error('获取收藏状态失败', error);
    }
  }
});

// 切换收藏状态
const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }
  
  loading.value = true;
  try {
    if (isFavorited.value) {
      // 取消收藏
      const res = await removeFavorite(props.resourceId, userStore.userInfo.id);
      if (res.data.code === 200) {
        isFavorited.value = false;
        favoriteCount.value--;
        ElMessage.success('取消收藏成功');
      } else {
        ElMessage.error(res.data.message || '取消收藏失败');
      }
    } else {
      // 添加收藏
      const res = await addFavorite(props.resourceId, userStore.userInfo.id);
      if (res.data.code === 200) {
        isFavorited.value = true;
        favoriteCount.value++;
        ElMessage.success('收藏成功');
      } else {
        ElMessage.error(res.data.message || '收藏失败');
      }
    }
  } catch (error) {
    console.error('操作收藏失败', error);
    ElMessage.error('操作失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.favorite-button {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-1);
  transition: var(--transition-base);
  position: relative;
  overflow: hidden;
}

.favorite-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, rgba(255, 255, 255, 0.1), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s ease;
}

.favorite-button:hover::before {
  transform: translateX(100%);
}

.favorite-button:hover {
  transform: translateY(-2px);
}

.count {
  font-size: 12px;
  opacity: 0.9;
  margin-left: 2px;
}

/* 已收藏状态的样式 */
.el-button--danger.favorite-button {
  background-color: rgba(var(--error-color-rgb, 231, 76, 60), 0.1);
  color: var(--error-color);
  border-color: var(--error-color);
}

.el-button--danger.favorite-button:hover {
  background-color: rgba(var(--error-color-rgb, 231, 76, 60), 0.2);
}

.el-icon {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.favorite-button:hover .el-icon {
  transform: scale(1.2);
}
</style> 