<template>
  <div 
    :class="['resource-card', {'hover-effect': enableHover, 'compact-view': isCompact}]"
    @click="handleCardClick"
  >
    <!-- 资源封面图 -->
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
      
      <!-- 资源类型标签 -->
      <div class="resource-type-badge" :class="typeClass">
        <el-icon class="type-icon"><component :is="typeIcon" /></el-icon>
        <span>{{ typeText }}</span>
      </div>
      
      <!-- 装饰性绶带 -->
      <div class="corner-ribbon" v-if="isNewResource">新！</div>
    </div>
  
    <div class="resource-content">
      <!-- 资源标题 -->
      <h3 class="resource-title" :title="resource.title">
        {{ resource.title }}
      </h3>
      
      <!-- 资源描述 -->
      <p class="resource-description" v-if="!isCompact && resource.description">
        {{ truncateText(resource.description, 50) }}
      </p>
      
      <!-- 资源信息 -->
      <div class="resource-info">
        <div class="resource-meta">
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <span>{{ resource.username || 'Unknown' }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Clock /></el-icon>
            <span>{{ formatDate(resource.uploadTime) }}</span>
          </div>
        </div>
        
        <!-- 资源标签 -->
        <div class="resource-tags" v-if="resource.tags && resource.tags.length > 0 && !isCompact">
          <el-tag 
            v-for="tag in limitedTags" 
            :key="tag.id"
            class="anime-tag"
            size="small"
          >
            {{ tag.name }}
          </el-tag>
          <span class="more-tags" v-if="resource.tags.length > 3">+{{ resource.tags.length - 3 }}</span>
        </div>
      </div>
      
      <!-- 资源操作栏 -->
      <div class="resource-actions">
        <div class="actions-left">
          <like-button 
            :resource-id="resource.id" 
            :initial-like-count="resource.likeCount || 0"
            :initial-is-liked="resource.isLiked || false"
            entity-type="resource"
            class="action-btn"
          />
          <favorite-button 
            :resource-id="resource.id"
            :initial-is-favorited="resource.isFavorited || false"
            class="action-btn"
          />
          <el-button
            v-if="isDownloadable"
            size="small"
            class="download-btn action-btn"
            @click.stop="handleDownload"
          >
            <div class="btn-content">
              <el-icon><Download /></el-icon>
              <span v-if="!isCompact" class="btn-text">{{ resource.downloadCount || 0 }}</span>
            </div>
          </el-button>
        </div>
        
        <div class="actions-right">
          <!-- 管理操作按钮 -->
          <el-dropdown 
            v-if="allowManage"
            trigger="click" 
            @click.stop 
            @command="handleCommand"
          >
            <el-button size="small" class="more-btn">
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu class="anime-dropdown">
                <el-dropdown-item command="edit">
                  <div class="dropdown-item-content">
                    <el-icon><Edit /></el-icon>
                    <span>编辑</span>
        </div>
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <div class="dropdown-item-content danger">
                    <el-icon><Delete /></el-icon>
                    <span>删除</span>
        </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, PropType } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Delete, Edit, User, Clock, Download, Picture, Document, VideoCamera as Video, Headset, MoreFilled } from '@element-plus/icons-vue'
import FavoriteButton from './FavoriteButton.vue'
import LikeButton from './LikeButton.vue'
import { deleteResource } from '@/api/resource'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import type { Resource } from '@/types/resource'

const props = defineProps({
  resource: {
    type: Object as PropType<Resource>,
    required: true
  },
  enableHover: {
    type: Boolean,
    default: true
  },
  isCompact: {
    type: Boolean,
    default: false
  },
  allowManage: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()

// 根据文件类型获取对应图标
const getFileTypeIcon = (fileType: string) => {
  // 确保fileType是字符串并转换为小写
  const type = (fileType || '').toLowerCase();
  
  // 检查文件类型并返回相应图标
  if (type.includes('pdf') || type.includes('doc') || type.includes('txt')) {
    return Document;
  } else if (type.includes('mp4') || type.includes('mkv') || type.includes('avi') || type.includes('video')) {
    return Video;
  } else if (type.includes('jpg') || type.includes('jpeg') || type.includes('png') || type.includes('gif') || type.includes('image')) {
    return Picture;
  } else if (type.includes('mp3') || type.includes('wav') || type.includes('flac') || type.includes('audio')) {
    return Headset;
  } else {
    return Document;
  }
};

// 根据文件类型获取图标颜色
const getIconColor = (fileType: string) => {
  const type = (fileType || '').toLowerCase();
  
  if (type.includes('pdf') || type.includes('doc') || type.includes('txt')) {
    return '#10b981'; // 绿色 - 文档
  } else if (type.includes('mp4') || type.includes('mkv') || type.includes('avi') || type.includes('video')) {
    return '#ef4444'; // 红色 - 视频
  } else if (type.includes('jpg') || type.includes('jpeg') || type.includes('png') || type.includes('gif') || type.includes('image')) {
    return '#3b82f6'; // 蓝色 - 图片
  } else if (type.includes('mp3') || type.includes('wav') || type.includes('flac') || type.includes('audio')) {
    return '#8b5cf6'; // 紫色 - 音频
  } else {
    return '#6b7280'; // 灰色 - 其他
  }
};

// 根据文件类型获取文本描述
const getFileTypeText = (fileType: string) => {
  const type = (fileType || '').toLowerCase();
  
  if (type.includes('pdf') || type.includes('doc') || type.includes('txt')) {
    return '文档';
  } else if (type.includes('mp4') || type.includes('mkv') || type.includes('avi') || type.includes('video')) {
    return '视频';
  } else if (type.includes('jpg') || type.includes('jpeg') || type.includes('png') || type.includes('gif') || type.includes('image')) {
    return '图片';
  } else if (type.includes('mp3') || type.includes('wav') || type.includes('flac') || type.includes('audio')) {
    return '音频';
  } else {
    return '其他';
  }
};

// 根据文件类型获取CSS类
const getFileTypeClass = (fileType: string) => {
  const type = (fileType || '').toLowerCase();
  
  if (type.includes('pdf') || type.includes('doc') || type.includes('txt')) {
    return 'type-document';
  } else if (type.includes('mp4') || type.includes('mkv') || type.includes('avi') || type.includes('video')) {
    return 'type-video';
  } else if (type.includes('jpg') || type.includes('jpeg') || type.includes('png') || type.includes('gif') || type.includes('image')) {
    return 'type-image';
  } else if (type.includes('mp3') || type.includes('wav') || type.includes('flac') || type.includes('audio')) {
    return 'type-audio';
  } else {
    return 'type-other';
  }
};

// 检查是否可下载
const isDownloadable = computed(() => {
  const fileType = props.resource.fileType?.toLowerCase() || '';
  return fileType !== '';
})

// 检查是否可播放
const isPlayableResource = computed(() => {
  const fileType = props.resource.fileType?.toLowerCase() || '';
  return fileType.includes('video') || fileType.includes('mp4') || 
         fileType.includes('mkv') || fileType.includes('avi') ||
         fileType.includes('audio') || fileType.includes('mp3') || 
         fileType.includes('wav') || fileType.includes('flac');
})

const isNewResource = computed(() => {
  const uploadDate = new Date(props.resource.uploadTime)
  const now = new Date()
  const differenceInDays = Math.floor((now.getTime() - uploadDate.getTime()) / (1000 * 60 * 60 * 24))
  return differenceInDays < 7
})

const typeIcon = computed(() => {
  return getFileTypeIcon(props.resource.fileType || '');
})

const typeText = computed(() => {
  return getFileTypeText(props.resource.fileType || '');
})

const typeClass = computed(() => {
  return getFileTypeClass(props.resource.fileType || '');
})

const limitedTags = computed(() => {
  return props.resource?.tags?.slice(0, 3) || []
})

const defaultCoverMap = {
  'document': 'https://via.placeholder.com/300x200?text=文档',
  'video': 'https://via.placeholder.com/300x200?text=视频',
  'image': 'https://via.placeholder.com/300x200?text=图片',
  'audio': 'https://via.placeholder.com/300x200?text=音频',
  'other': 'https://via.placeholder.com/300x200?text=资源'
}

const getCoverPlaceholder = (fileType: string) => {
  const type = (fileType || '').toLowerCase();
  
  if (type.includes('pdf') || type.includes('doc') || type.includes('txt')) {
    return defaultCoverMap['document'];
  } else if (type.includes('mp4') || type.includes('mkv') || type.includes('avi') || type.includes('video')) {
    return defaultCoverMap['video'];
  } else if (type.includes('jpg') || type.includes('jpeg') || type.includes('png') || type.includes('gif') || type.includes('image')) {
    return defaultCoverMap['image'];
  } else if (type.includes('mp3') || type.includes('wav') || type.includes('flac') || type.includes('audio')) {
    return defaultCoverMap['audio'];
  } else {
    return defaultCoverMap['other'];
  }
}

const coverUrl = ref(props.resource.coverUrl || getCoverPlaceholder(props.resource.fileType || ''))

const handleImageError = () => {
  coverUrl.value = getCoverPlaceholder(props.resource.fileType || '')
}

const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  // 在合适的位置截断文本，优先在标点符号处
  const punctuationPos = text.substring(0, maxLength).lastIndexOf('。')
  if (punctuationPos > maxLength * 0.7) {
    return text.substring(0, punctuationPos + 1) + '...'
  }
  const commaPos = text.substring(0, maxLength).lastIndexOf('，')
  if (commaPos > maxLength * 0.7) {
    return text.substring(0, commaPos + 1) + '...'
  }
  const spacePos = text.substring(0, maxLength).lastIndexOf(' ')
  if (spacePos > maxLength * 0.7) {
    return text.substring(0, spacePos) + '...'
  }
  return text.substring(0, maxLength) + '...'
}

const formatDate = (dateString: string) => {
  try {
    return formatDistanceToNow(new Date(dateString), { addSuffix: true, locale: zhCN })
  } catch (e) {
    console.error('日期格式化错误:', e)
    return dateString
  }
}

const handleCardClick = () => {
  router.push(`/resources/${props.resource.id}`)
}

const handleDownload = (e: Event) => {
  e.stopPropagation()
  window.open(props.resource.fileUrl, '_blank')
}

const handleCommand = (command: string) => {
  if (command === 'edit') {
    router.push(`/resources/edit/${props.resource.id}`)
  } else if (command === 'delete') {
    ElMessageBox.confirm(
      '确定要删除这个资源吗？此操作不可恢复。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      try {
        await deleteResource(props.resource.id)
        ElMessage.success('资源已成功删除')
        // 刷新页面或通知父组件
        setTimeout(() => {
          window.location.reload()
        }, 1000)
      } catch (error) {
        console.error('删除资源失败:', error)
        ElMessage.error('删除资源失败，请重试')
      }
    }).catch(() => {
    // 用户取消删除
    })
  }
}
</script>

<style scoped>
.resource-card {
  background-color: var(--bg-secondary);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  border: 1px solid rgba(var(--primary-color-rgb, 255, 107, 129), 0.05);
  width: 100%;
  height: 100%;
}

.hover-effect:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
  border-color: rgba(var(--primary-color-rgb, 255, 107, 129), 0.2);
}

/* 封面样式 */
.resource-image {
  position: relative;
  padding-top: 56.25%; /* 固定16:9比例 */
  overflow: hidden;
  background: linear-gradient(to right, #f9f9f9, #f1f1f1, #f9f9f9);
}

.resource-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.hover-effect:hover .resource-image img {
  transform: scale(1.05);
}

/* 文件类型图标 */
.file-type-icon {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--text-secondary);
}

.file-type-label {
  margin-top: 10px;
  font-size: 14px;
  color: var(--text-hint);
  opacity: 0.8;
}

/* 资源类型标签 */
.resource-type-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 8px;
  border-radius: 20px;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  backdrop-filter: blur(3px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  z-index: 2;
}

.type-icon {
  font-size: 14px;
}

.type-image {
  background-color: rgba(var(--info-color-rgb, 87, 201, 239), 0.8);
}

.type-video {
  background-color: rgba(var(--primary-color-rgb, 255, 107, 129), 0.8);
}

.type-audio {
  background-color: rgba(var(--secondary-color-rgb, 115, 103, 240), 0.8);
}

.type-document {
  background-color: rgba(var(--success-color-rgb, 109, 213, 176), 0.8);
}

/* 装饰性绶带 */
.corner-ribbon {
  position: absolute;
  top: 0;
  right: 0;
  width: 80px;
  background: var(--primary-color);
  color: white;
  text-align: center;
  line-height: 30px;
  font-size: 12px;
  font-weight: bold;
  transform: rotate(45deg) translateX(20px) translateY(-14px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  z-index: 2;
}

/* 内容区域样式 */
.resource-content {
  padding: 15px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 160px; /* 调整内容区域的最小高度 */
}

.resource-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 2.4em; /* 固定标题高度，大约两行文字的高度 */
}

.resource-description {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  height: 1.4em; /* 固定描述高度，只有一行文字的高度 */
}

/* 资源信息区域 */
.resource-info {
  margin-top: auto;
  margin-bottom: 10px;
}

.resource-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-hint);
  margin-bottom: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 资源标签 */
.resource-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  min-height: 22px; /* 设置标签区域的最小高度 */
}

.anime-tag {
  background-color: rgba(var(--secondary-color-rgb, 115, 103, 240), 0.1);
  color: var(--secondary-color);
  border: 1px solid rgba(var(--secondary-color-rgb, 115, 103, 240), 0.2);
  border-radius: 12px;
  padding: 0 8px;
  font-size: 11px;
  transition: all 0.3s;
}

.anime-tag:hover {
  background-color: rgba(var(--secondary-color-rgb, 115, 103, 240), 0.2);
  transform: translateY(-2px);
}

.more-tags {
  font-size: 11px;
  color: var(--text-hint);
  display: flex;
  align-items: center;
}

/* 操作按钮区域 */
.resource-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  border-top: 1px solid rgba(var(--gray-300-rgb, 222, 226, 230), 0.3);
  padding-top: 12px;
  height: 36px; /* 固定操作栏高度 */
}

.actions-left {
  display: flex;
  gap: 8px;
}

.action-btn {
  border: none;
  background: none;
  padding: 4px 8px;
  border-radius: 20px;
  transition: all 0.3s;
}

.action-btn:hover {
  background-color: rgba(var(--gray-200-rgb, 233, 236, 239), 0.3);
}

.download-btn {
  color: var(--info-color);
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-text {
  font-size: 12px;
}

.more-btn {
  border: none;
  background: none;
  color: var(--text-hint);
  transition: all 0.3s;
}

.more-btn:hover {
  color: var(--text-primary);
  background-color: rgba(var(--gray-200-rgb, 233, 236, 239), 0.3);
  transform: rotate(90deg);
}

.dropdown-item-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.danger {
  color: var(--error-color);
}

/* 紧凑视图样式 */
.compact-view {
  flex-direction: row;
  height: 100px;
}

.compact-view .resource-image {
  width: 100px;
  height: 100px;
  padding-top: 0;
  flex-shrink: 0;
}

.compact-view .resource-content {
  flex: 1;
  padding: 10px;
  min-height: auto;
}

.compact-view .resource-title {
  font-size: 14px;
  margin-bottom: 5px;
  -webkit-line-clamp: 1;
  height: auto;
}

.compact-view .resource-description {
  display: none; /* 在紧凑视图中隐藏描述 */
}

.compact-view .resource-actions {
  margin-top: 5px;
  padding-top: 5px;
}

.compact-view .resource-type-badge {
  top: 5px;
  left: 5px;
  padding: 2px 6px;
  font-size: 10px;
}

.compact-view .corner-ribbon {
  display: none;
}

.compact-view .file-type-icon {
  padding: 5px;
}

.compact-view .file-type-icon .el-icon {
  font-size: 30px !important;
}

.compact-view .file-type-label {
  font-size: 10px;
  margin-top: 5px;
}
</style> 