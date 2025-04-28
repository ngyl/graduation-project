<template>
  <div class="resource-search-results">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="totalResources === 0" class="empty-state">
      <el-empty description="没有找到相关资源" />
    </div>
    <template v-else>
      <div class="search-stats">
        共找到 <span class="highlight">{{ totalResources }}</span> 个相关资源
      </div>
      
      <div class="resource-list">
        <div v-for="resource in resources?.items" :key="resource.id" class="resource-item" @click="viewResourceDetail(resource)">
          <div class="resource-preview">
            <el-image 
              :src="resource.coverUrl || '/images/default-resource.jpg'" 
              fit="cover"
              :alt="resource.title">
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="resource-info">
            <div class="resource-title">{{ resource.title }}</div>
            <div class="resource-desc">{{ resource.description }}</div>
            <div class="resource-meta">
              <span class="type">{{ getResourceType(resource.fileType) }}</span>
              <span class="size" v-if="resource.fileSize">{{ formatSize(resource.fileSize) }}</span>
              <div class="tags">
                <el-tag v-for="tag in resource.tags" :key="tag" size="small">{{ tag }}</el-tag>
              </div>
            </div>
            <div class="resource-stats">
              <span><el-icon><Download /></el-icon> {{ resource.downloadCount || 0 }}</span>
              <span><el-icon><View /></el-icon> {{ resource.downloadCount || 0 }}</span>
              <span><el-icon><Star /></el-icon> {{ resource.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          layout="prev, pager, next"
          :total="totalResources"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Picture, Download, View, Star } from '@element-plus/icons-vue';
import { searchResources } from '@/api/search';
import type { Resource, ResourceListResponse } from '@/types/resource';

const props = defineProps<{
  keyword: string
}>();

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const resources = ref<ResourceListResponse>();
const totalResources = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 监听关键词变化
watch(() => props.keyword, (newKeyword) => {
  if (newKeyword) {
    // 重置页码
    currentPage.value = 1;
    // 查询数据
    fetchResources();
  }
}, { immediate: true });

// 从URL获取页码
onMounted(() => {
  const queryPage = route.query.page ? parseInt(route.query.page as string) : 1;
  if (queryPage && queryPage !== currentPage.value) {
    currentPage.value = queryPage;
  }
  
  if (props.keyword) {
    fetchResources();
  }
});

// 监听页码变化
watch(() => route.query.page, (newPage) => {
  if (newPage && parseInt(newPage as string) !== currentPage.value) {
    currentPage.value = parseInt(newPage as string);
    if (props.keyword) {
      fetchResources();
    }
  }
});

// 查询资源数据
async function fetchResources() {
  if (!props.keyword.trim()) {
    return;
  }
  
  loading.value = true;
  try {
    const response = await searchResources(props.keyword, currentPage.value, pageSize.value);
    if (response.data.code === 200) {
      resources.value = response.data.data || [];
      totalResources.value = response.data.data.total || 0;
    } else {
      ElMessage.error(response.data.message || '搜索失败');
    }
  } catch (error) {
    console.error('搜索资源失败', error);
    ElMessage.error('搜索失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 处理页码变化
function handlePageChange(page: number) {
  router.push({
    path: '/search',
    query: {
      ...route.query,
      page: page.toString(),
      tab: 'resources'
    }
  });
}

// 查看资源详情
function viewResourceDetail(resource: Resource) {
  router.push(`/resources/${resource.id}`);
}

// 获取资源类型的显示文本
function getResourceType(fileType?: string): string {
  if (!fileType) return '未知类型';
  
  if (fileType.startsWith('image/')) return '图片';
  if (fileType.startsWith('video/')) return '视频';
  if (fileType.startsWith('audio/')) return '音频';
  if (fileType === 'application/pdf') return 'PDF文档';
  if (fileType.includes('word')) return 'Word文档';
  if (fileType.includes('excel') || fileType.includes('spreadsheet')) return 'Excel表格';
  if (fileType.includes('powerpoint') || fileType.includes('presentation')) return 'PPT演示文稿';
  if (fileType.includes('zip') || fileType.includes('rar') || fileType.includes('tar') || fileType.includes('7z')) return '压缩包';
  
  return fileType.split('/')[1] || '其他';
}

// 格式化文件大小
function formatSize(size: number) {
  if (size < 1024) {
    return size + ' B';
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB';
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + ' MB';
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  }
}
</script>

<style scoped>
.resource-search-results {
  padding: 10px 0;
}

.search-stats {
  margin-bottom: 20px;
  color: #666;
}

.highlight {
  color: #409EFF;
  font-weight: bold;
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.resource-item {
  display: flex;
  padding: 15px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.resource-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.resource-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 15px;
  flex-shrink: 0;
}

.resource-preview .el-image {
  width: 100%;
  height: 100%;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.resource-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.resource-title {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.resource-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.resource-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.type {
  padding: 2px 5px;
  background-color: #f0f9eb;
  color: #67c23a;
  border-radius: 4px;
}

.tags {
  display: flex;
  gap: 5px;
  margin-left: auto;
  flex-wrap: wrap;
}

.resource-stats {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
  margin-top: auto;
}

.resource-stats span {
  display: flex;
  align-items: center;
}

.resource-stats .el-icon {
  margin-right: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.loading-state,
.empty-state {
  padding: 40px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .resource-item {
    flex-direction: column;
  }
  
  .resource-preview {
    width: 100%;
    height: 180px;
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .resource-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .tags {
    margin-left: 0;
    margin-top: 5px;
  }
}
</style> 