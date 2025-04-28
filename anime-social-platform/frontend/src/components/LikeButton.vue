<template>
  <el-button
    :type="isLiked ? 'danger' : 'default'"
    size="small"
    @click="toggleLike"
    :loading="loading"
    class="like-button"
  >
    <el-icon>
      <component :is="isLiked ? 'Pointer' : 'Pointer'" :style="{ color: isLiked ? 'var(--error-color)' : 'inherit' }" />
    </el-icon>
    <span>{{ isLiked ? '已点赞' : '点赞' }}</span>
    <span v-if="showCount" class="count">({{ likeCount }})</span>
  </el-button>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Pointer } from '@element-plus/icons-vue';
import { likeResource, unlikeResource } from '../api/resource';
import { useUserStore } from '../stores/user';

const props = defineProps({
  resourceId: {
    type: Number,
    required: true
  },
  initialIsLiked: {
    type: Boolean,
    default: false
  },
  initialLikeCount: {
    type: Number,
    default: 0
  },
  showCount: {
    type: Boolean,
    default: true
  }
});

const userStore = useUserStore();
const isLiked = ref(props.initialIsLiked);
const likeCount = ref(props.initialLikeCount);
const loading = ref(false);

// 在组件加载时初始化点赞状态
onMounted(() => {
  isLiked.value = props.initialIsLiked;
  likeCount.value = props.initialLikeCount;
});

// 切换点赞状态
const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }
  
  loading.value = true;
  try {
    if (isLiked.value) {
      // 取消点赞
      const res = await unlikeResource(props.resourceId);
      if (res.data.code === 200) {
        isLiked.value = false;
        if (likeCount.value > 0) {
          likeCount.value--;
        }
        ElMessage.success('取消点赞成功');
      } else {
        ElMessage.error(res.data.message || '取消点赞失败');
      }
    } else {
      // 点赞
      const res = await likeResource(props.resourceId);
      if (res.data.code === 200) {
        isLiked.value = true;
        likeCount.value++;
        ElMessage.success('点赞成功');
      } else {
        ElMessage.error(res.data.message || '点赞失败');
      }
    }
  } catch (error) {
    console.error('操作点赞失败', error);
    ElMessage.error('操作失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.like-button {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-1);
  transition: var(--transition-base);
  position: relative;
  overflow: hidden;
}

.like-button::before {
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

.like-button:hover::before {
  transform: translateX(100%);
}

.like-button:hover {
  transform: translateY(-2px);
}

.count {
  font-size: 12px;
  opacity: 0.9;
  margin-left: 2px;
}

/* 已点赞状态的样式 */
.el-button--danger.like-button {
  background-color: rgba(var(--error-color-rgb, 231, 76, 60), 0.1);
  color: var(--error-color);
  border-color: var(--error-color);
}

.el-button--danger.like-button:hover {
  background-color: rgba(var(--error-color-rgb, 231, 76, 60), 0.2);
}

.el-icon {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.like-button:hover .el-icon {
  transform: scale(1.2);
}
</style> 