<template>
  <div class="resource-card" :class="{ 'dark-mode': isDarkMode }">
    <div class="resource-image">
      <template v-if="resource.coverUrl">
        <img :src="resource.coverUrl" :alt="resource.title" />
      </template>
      <div v-else class="file-type-icon">
        <el-icon :size="60" :color="getIconColor(resource.fileType || 'unknown')">
          <component :is="getFileTypeIcon(resource.fileType || 'unknown')"></component>
        </el-icon>
        <div class="file-type-label">{{ resource.fileType || '未知类型' }}</div>
      </div>
      <div class="resource-overlay">
        <div class="resource-actions">
          <el-button type="primary" size="small" @click="viewResource(resource.id)">
            <el-icon><View /></el-icon>
            查看详情
          </el-button>
          <favorite-button :resource-id="resource.id" />
          <like-button 
            :resource-id="resource.id" 
            :initial-is-liked="resource.isLiked" 
            :initial-like-count="resource.likeCount || 0"
            :show-count="false"
          />
        </div>
      </div>
      <div class="file-type-badge" v-if="resource.fileType">
        {{ resource.fileType }}
      </div>
    </div>
    <div class="resource-content">
      <h3 class="resource-title">{{ resource.title }}</h3>
      <div class="resource-tags" v-if="resource.tags && resource.tags.length > 0">
        <el-tag 
          v-for="tag in limitedTags" 
          :key="tag.id" 
          size="small" 
          effect="light"
          class="resource-tag"
        >
          {{ tag.name }}
        </el-tag>
        <el-tag v-if="resource.tags.length > maxTagsToShow" size="small" type="info" class="more-tag">
          +{{ resource.tags.length - maxTagsToShow }}
        </el-tag>
      </div>
      <p class="resource-description">{{ truncateText(resource.description, 100) }}</p>
      <div class="resource-meta">
        <span class="resource-stats">
          <el-tooltip content="收藏次数" placement="top">
            <span class="stat-item">
              <el-icon><star /></el-icon> {{ resource.favoriteCount }}
            </span>
          </el-tooltip>
          <el-tooltip content="点赞次数" placement="top">
            <span class="stat-item">
              <el-icon><pointer /></el-icon> {{ resource.likeCount || 0 }}
            </span>
          </el-tooltip>
          <el-tooltip v-if="resource.downloadCount !== undefined" content="下载次数" placement="top">
            <span class="stat-item">
              <el-icon><download /></el-icon> {{ resource.downloadCount }}
            </span>
          </el-tooltip>
        </span>
        <span class="resource-date">{{ formatDate(resource.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { inject, computed } from 'vue';
import { useRouter } from 'vue-router';
import { format } from 'date-fns';
import { View, Document, VideoPlay, Picture, Headset, Collection, More, Star, Download, Pointer } from '@element-plus/icons-vue';
import FavoriteButton from './FavoriteButton.vue';
import LikeButton from './LikeButton.vue';

const props = defineProps({
  resource: {
    type: Object,
    required: true
  }
});

const router = useRouter();
const isDarkMode = inject('isDarkMode', false);
const maxTagsToShow = 2;

const limitedTags = computed(() => {
  if (!props.resource.tags || !Array.isArray(props.resource.tags)) return [];
  return props.resource.tags.slice(0, maxTagsToShow);
});

// 根据文件类型获取对应图标
const getFileTypeIcon = (fileType: string) => {
  // 确保fileType是字符串并转换为小写
  const type = (fileType || '').toLowerCase();
  
  // 检查文件类型并返回相应图标
  if (type.includes('pdf')) {
    return Document;
  } else if (type.includes('doc')) {
    return Document;
  } else if (type.includes('txt')) {
    return Document;
  } else if (type.includes('mp4') || type.includes('mkv') || type.includes('avi') || type.includes('video')) {
    return VideoPlay;
  } else if (type.includes('jpg') || type.includes('jpeg') || type.includes('png') || type.includes('gif') || type.includes('image')) {
    return Picture;
  } else if (type.includes('zip') || type.includes('rar') || type.includes('7z') || type.includes('application')) {
    return Collection;
  } else if (type.includes('mp3') || type.includes('wav') || type.includes('flac') || type.includes('audio')) {
    return Headset;
  } else {
    return More;
  }
};

// 根据文件类型获取颜色
const getIconColor = (fileType: string) => {
  // 确保fileType是字符串并转换为小写
  const type = (fileType || '').toLowerCase();
  
  // 检查文件类型并返回相应颜色
  if (type.includes('pdf')) {
    return '#E53935';
  } else if (type.includes('doc')) {
    return '#1565C0';
  } else if (type.includes('txt')) {
    return '#607D8B';
  } else if (type.includes('mp4') || type.includes('mkv') || type.includes('avi') || type.includes('video')) {
    return '#D81B60';
  } else if (type.includes('jpg') || type.includes('jpeg') || type.includes('png') || type.includes('gif') || type.includes('image')) {
    return '#43A047';
  } else if (type.includes('zip') || type.includes('rar') || type.includes('7z') || type.includes('application')) {
    return '#FF9800';
  } else if (type.includes('mp3') || type.includes('wav') || type.includes('flac') || type.includes('audio')) {
    return '#7B1FA2';
  } else {
    return '#9E9E9E';
  }
};

// 跳转到资源详情页
const viewResource = (id: number) => {
  router.push(`/resources/${id}`);
};

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '';
  try {
    return format(new Date(date), 'yyyy-MM-dd');
  } catch (error) {
    return date;
  }
};

</script>

<style scoped>
.resource-card {
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  background-color: var(--bg-secondary);
  box-shadow: var(--shadow-md);
  transition: var(--transition-base);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.resource-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.resource-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.resource-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: var(--transition-base);
}

.file-type-icon {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.file-type-label {
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
  text-transform: uppercase;
}

.file-type-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  text-transform: uppercase;
}

.resource-card:hover .resource-image img {
  transform: scale(1.05);
}

.resource-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: var(--transition-base);
}

.resource-card:hover .resource-overlay {
  opacity: 1;
}

.resource-actions {
  display: flex;
  gap: var(--spacing-2);
}

.resource-content {
  padding: var(--spacing-4);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.resource-title {
  font-size: var(--font-size-lg);
  margin: 0 0 var(--spacing-2);
  color: var(--text-primary);
  transition: var(--transition-fast);
}

.resource-card:hover .resource-title {
  color: var(--primary-color);
}

.resource-tags {
  margin-bottom: var(--spacing-2);
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.resource-tag {
  margin-right: 0;
}

.more-tag {
  opacity: 0.8;
}

.resource-description {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.5;
  margin-bottom: var(--spacing-4);
  flex: 1;
}

.resource-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.resource-stats {
  display: flex;
  gap: 10px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.resource-type {
  font-size: var(--font-size-sm);
}

.resource-date {
  font-size: var(--font-size-sm);
  color: var(--text-hint);
}

/* 暗黑模式适配 */
.resource-card.dark-mode {
  background-color: var(--bg-tertiary);
}

.resource-card.dark-mode .resource-title {
  color: var(--text-light);
}

.resource-card.dark-mode .resource-description {
  color: var(--text-secondary);
}

.resource-card.dark-mode .file-type-icon {
  background-color: #1e1e1e;
}

.resource-card.dark-mode .file-type-label {
  color: #a6a6a6;
}
</style> 