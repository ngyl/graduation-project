<template>
  <app-layout>
    <div class="resource-detail-container">
      <div class="page-header">
        <el-button type="text" @click="$router.back()" class="back-button">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- 资源详情 -->
      <template v-else-if="resource">
        <div class="resource-header">
          <h1>{{ resource.title }}</h1>
          <div class="resource-meta">
            <el-tag size="small" :type="getTagType(resource.fileType || 'unknown')">
              {{ resource.fileType || 'unknown' }}
            </el-tag>
            <div class="resource-tags" v-if="resource.tags && resource.tags.length > 0">
              <el-tag 
                v-for="tag in resource.tags" 
                :key="tag.id" 
                size="small" 
                effect="light"
                class="resource-tag"
              >
                {{ tag.name }}
              </el-tag>
            </div>
            <span class="resource-date">发布时间: {{ formatDate(resource.uploadTime) }}</span>
            <div class="resource-actions">
              <favorite-button :resource-id="resource.id" />
              <like-button
                :resource-id="resource.id"
                :initial-is-liked="resource.isLiked"
                :initial-like-count="resource.likeCount || 0"
              />
              <el-button type="primary" size="small" @click="openResource">
                <el-icon><Download /></el-icon> 下载资源
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                v-if="isAdmin || (isLoggedIn && isOwner)" 
                @click="showEditDialog = true"
              >
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                v-if="isAdmin || (isLoggedIn && isOwner)" 
                @click="confirmDelete"
              >
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
          </div>
        </div>

        <el-row :gutter="20" class="resource-content">
          <el-col :xs="24" :sm="24" :md="14" :lg="16" class="resource-main">
            <div class="resource-cover">
              <template v-if="resource.coverUrl">
                <img :src="resource.coverUrl" class="cover-image" alt="资源封面" />
                <div class="file-type-badge" v-if="resource.fileType">
                  {{ resource.fileType }}
                </div>
              </template>
              <template v-else>
                <div class="file-type-icon">
                  <el-icon :size="80" :color="getIconColor(resource.fileType || 'unknown')">
                    <component :is="getFileTypeIcon(resource.fileType || 'unknown')"></component>
                  </el-icon>
                  <div class="file-type-label">{{ resource.fileType || 'unknown' }}</div>
                </div>
                <!-- 添加文件类型角标 -->
                <div class="file-type-badge" v-if="resource.fileType">
                  {{ resource.fileType }}
                </div>
              </template>
            </div>
            <div class="resource-description">
              <h2>资源简介</h2>
              <p>{{ resource.description }}</p>
            </div>
            
            <div class="resource-stats card">
              <h3>资源统计</h3>
              <div class="stats-container">
                <div class="stat-item">
                  <el-icon><Star /></el-icon>
                  <span class="stat-value">{{ resource.favoriteCount }} 收藏</span>
                </div>
                <div class="stat-item">
                  <el-icon><Pointer /></el-icon>
                  <span class="stat-value">{{ resource.likeCount || 0 }} 点赞</span>
                </div>
                <div class="stat-item" v-if="resource.downloadCount !== undefined">
                  <el-icon><Download /></el-icon>
                  <span class="stat-value">{{ resource.downloadCount }} 下载</span>
                </div>
              </div>
            </div>
          </el-col>

          <el-col :xs="24" :sm="24" :md="10" :lg="8" class="resource-sidebar">
            <div class="resource-info card">
              <h3>发布者信息</h3>
              <div class="publisher-info">
                <el-avatar :size="50" :src="resource.userAvatar || defaultAvatar" class="publisher-avatar" />
                <div class="publisher-details">
                  <h4>{{ resource.username || '匿名用户' }}</h4>
                  <p>贡献者</p>
                </div>
              </div>
            </div>

            <!-- 相关资源 -->
            <div class="related-resources card">
              <h3>相关资源</h3>
              <div v-if="loadingRelated" class="loading-related">
                <el-skeleton :rows="3" animated />
              </div>
              <div v-else-if="relatedResources.length === 0" class="empty-related">
                <el-empty description="暂无相关资源" :image-size="80" />
              </div>
              <div v-else v-for="item in relatedResources" :key="item.id" class="related-item">
                <div class="related-cover">
                  <el-icon :size="40" :color="getIconColor(item.fileType || 'unknown')">
                    <component :is="getFileTypeIcon(item.fileType || 'unknown')"></component>
                  </el-icon>
                </div>
                <div class="related-info">
                  <h4 @click="$router.push(`/resources/${item.id}`)">{{ item.title }}</h4>
                  <div class="related-meta">
                    <el-tag size="small" :type="getTagType(item.fileType || 'unknown')">{{ item.fileType || 'unknown' }}</el-tag>
                    <div class="related-stats">
                      <span><el-icon><Star /></el-icon> {{ item.favoriteCount }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </template>

      <!-- 资源不存在 -->
      <div v-else class="not-found">
        <el-empty description="资源不存在或已被删除" />
        <el-button type="primary" @click="$router.push('/resources')">返回资源列表</el-button>
      </div>

      <!-- 编辑资源对话框 -->
      <el-dialog
        v-model="showEditDialog"
        title="编辑资源"
        width="500px"
        destroy-on-close
      >
        <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="80px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="editForm.title" placeholder="请输入资源标题" />
          </el-form-item>
          <el-form-item label="类型" prop="fileType">
            <el-select v-model="editForm.fileType" placeholder="请选择资源类型">
              <el-option v-for="type in fileTypes" :key="type" :label="type" :value="type" />
            </el-select>
          </el-form-item>
          <el-form-item label="链接" prop="fileUrl">
            <el-input v-model="editForm.fileUrl" placeholder="请输入资源链接" />
          </el-form-item>
          <el-form-item label="简介" prop="description">
            <el-input 
              v-model="editForm.description" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入资源简介"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showEditDialog = false">取消</el-button>
            <el-button type="primary" @click="updateResource" :loading="submitting">保存</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, Edit, Document, Picture, VideoPlay, Headset, Collection, More, Star, Download, Delete, Pointer } from '@element-plus/icons-vue';
import { format } from 'date-fns';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user';
import FavoriteButton from '@/components/FavoriteButton.vue';
import LikeButton from '@/components/LikeButton.vue';
import type { Resource } from '@/types/resource.ts';
import AppLayout from '@/components/AppLayout.vue';
import { getResourceById, downloadResource, updateResource as updateResourceApi, deleteResource } from '@/api/resource';
import { getResources } from '@/api/resource';
import { ApiResponseUtil } from '@/types/api';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 用户状态
const isLoggedIn = computed(() => userStore.isLoggedIn);
const isAdmin = computed(() => userStore.isAdmin);
const isOwner = ref(false);

// 资源状态
const resourceId = computed(() => Number(route.params.id));
const resource = ref<Resource | null>(null);
const loading = ref(true);
const defaultAvatar = '/images/default-avatar.png';

// 编辑状态
const showEditDialog = ref(false);
const editFormRef = ref();
const editForm = reactive({
  title: '',
  fileType: '',
  description: '',
  fileUrl: ''
});

// 文件类型列表
const fileTypes = ['VIDEO/MP4', 'IMAGE/ZIP', 'APPLICATION/ZIP', 'AUDIO/ZIP', 'APPLICATION/PDF', 'DOC/PDF'];

// 相关资源
const relatedResources = ref<Resource[]>([]);
const loadingRelated = ref(false);

// 提交状态
const submitting = ref(false);

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入资源标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在2到50个字符之间', trigger: 'blur' }
  ],
  fileType: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入资源简介', trigger: 'blur' },
    { min: 10, max: 500, message: '简介长度在10到500个字符之间', trigger: 'blur' }
  ],
  fileUrl: [
    { required: true, message: '请输入资源链接', trigger: 'blur' },
    { pattern: /^(https?:\/\/)/, message: '请输入有效的链接地址', trigger: 'blur' }
  ]
};

// 初始化时获取资源详情
onMounted(async () => {
  if (!resourceId.value) {
    router.push('/resources');
    return;
  }
  
  await fetchResourceDetail();
});

// 监听资源ID变化，重新加载数据
watch(() => route.params.id, async () => {
  if (resourceId.value) {
    await fetchResourceDetail();
  }
});

// 获取资源详情
const fetchResourceDetail = async () => {
  loading.value = true;
  try {
    const response = await getResourceById(resourceId.value);
    
    if (ApiResponseUtil.isSuccess(response.data)) {
      resource.value = response.data.data;
      
      // 判断当前用户是否为资源发布者
      isOwner.value = isLoggedIn.value && resource.value.userId === userStore.user?.id;
      
      // 初始化编辑表单
      editForm.title = resource.value.title;
      editForm.fileType = resource.value.fileType || '';
      editForm.description = resource.value.description;
      editForm.fileUrl = resource.value.fileUrl || '';
      
      // 获取相关资源
      fetchRelatedResources();
    } else {
      ElMessage.error(response.data.message || '获取资源详情失败');
      resource.value = null;
    }
  } catch (error: any) {
    console.error('获取资源详情失败:', error);
    ElMessage.error(error.response?.data?.message || '获取资源详情失败，请稍后重试');
    resource.value = null;
  } finally {
    loading.value = false;
  }
};

// 获取相关资源
const fetchRelatedResources = async () => {
  if (!resource.value) return;
  
  loadingRelated.value = true;
  try {
    // 获取同类型或同标签的相关资源
    // 此处逻辑可以替换为专门的相关资源API，暂时使用普通资源列表代替
    const response = await getResources(1, 3, resource.value.tags?.[0]?.id);
    
    if (ApiResponseUtil.isSuccess(response.data)) {
      // 过滤掉当前资源
      relatedResources.value = (response.data.data.items || [])
        .filter(item => item.id !== resource.value?.id)
        .slice(0, 3);
    } else {
      relatedResources.value = [];
    }
  } catch (error: any) {
    console.error('获取相关资源失败:', error);
    relatedResources.value = [];
  } finally {
    loadingRelated.value = false;
  }
};

// 更新资源信息
const updateResource = async () => {
  if (!editFormRef.value) return;
  
  await editFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true;
      try {
        // 构建FormData
        const formData = new FormData();
        formData.append('title', editForm.title);
        formData.append('fileType', editForm.fileType);
        formData.append('description', editForm.description);
        formData.append('fileUrl', editForm.fileUrl);
        
        const response = await updateResourceApi(resourceId.value, formData);
        
        if (ApiResponseUtil.isSuccess(response.data)) {
          ElMessage.success('资源更新成功');
          
          // 更新本地资源信息
          if (resource.value) {
            resource.value.title = editForm.title;
            resource.value.fileType = editForm.fileType;
            resource.value.description = editForm.description;
            resource.value.fileUrl = editForm.fileUrl;
          }
          
          showEditDialog.value = false;
        } else {
          ElMessage.error(response.data.message || '更新资源失败');
        }
      } catch (error: any) {
        console.error('更新资源失败:', error);
        ElMessage.error(error.response?.data?.message || '更新资源失败，请稍后重试');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 删除资源
const confirmDelete = () => {
  ElMessageBox.confirm(
    '确定要删除该资源吗？此操作不可逆。',
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await deleteResource(resourceId.value);
      
      if (ApiResponseUtil.isSuccess(response.data)) {
        ElMessage.success('资源已成功删除');
        router.push('/resources');
      } else {
        ElMessage.error(response.data.message || '删除资源失败');
      }
    } catch (error: any) {
      console.error('删除资源失败:', error);
      ElMessage.error(error.response?.data?.message || '删除资源失败，请稍后重试');
    }
  }).catch(() => {
    // 用户取消删除，不做任何操作
  });
};

// 访问资源
const openResource = async () => {
  if (!resource.value?.fileUrl) {
    ElMessage.warning('资源链接不存在');
    return;
  }
  
  try {
    // 获取下载链接并记录下载次数
    const response = await downloadResource(resourceId.value);
    
    if (ApiResponseUtil.isSuccess(response.data)) {
      const downloadUrl = response.data.data;
      window.open(downloadUrl, '_blank');
      
      // 更新本地下载计数
      if (resource.value && resource.value.downloadCount !== undefined) {
        resource.value.downloadCount += 1;
      }
    } else {
      ElMessage.error(response.data.message || '获取资源链接失败');
    }
  } catch (error: any) {
    console.error('下载资源失败:', error);
    ElMessage.error(error.response?.data?.message || '下载失败，请稍后重试');
  }
};

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '';
  try {
    return format(new Date(date), 'yyyy-MM-dd HH:mm');
  } catch (error) {
    return date;
  }
};

// 根据资源类型返回不同的标签样式
const getTagType = (type: string) => {
  // 确保fileType是字符串并转换为小写
  const typeStr = (type || '').toLowerCase();
  
  if (typeStr.includes('pdf')) {
    return 'primary';
  } else if (typeStr.includes('doc')) {
    return 'primary';
  } else if (typeStr.includes('txt')) {
    return 'info';
  } else if (typeStr.includes('mp4') || typeStr.includes('mkv') || typeStr.includes('avi') || typeStr.includes('video')) {
    return 'danger';
  } else if (typeStr.includes('jpg') || typeStr.includes('jpeg') || typeStr.includes('png') || typeStr.includes('gif') || typeStr.includes('image')) {
    return 'success';
  } else if (typeStr.includes('zip') || typeStr.includes('rar') || typeStr.includes('7z') || typeStr.includes('application')) {
    return 'warning';
  } else if (typeStr.includes('mp3') || typeStr.includes('wav') || typeStr.includes('flac') || typeStr.includes('audio')) {
    return 'info';
  } else {
    return 'info';
  }
};

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
</script>

<style scoped>
.resource-detail-container {
  max-width: var(--container-width);
  margin: 0 auto;
  padding: var(--spacing-4);
}

.page-header {
  margin-bottom: var(--spacing-4);
}

.back-button {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  font-size: var(--font-size-md);
  color: var(--text-secondary);
  transition: var(--transition-fast);
}

.back-button:hover {
  color: var(--primary-color);
}

.loading-container,
.not-found {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-8);
}

.not-found .el-button {
  margin-top: var(--spacing-4);
}

.resource-header {
  margin-bottom: var(--spacing-6);
}

.resource-header h1 {
  font-size: var(--font-size-3xl);
  color: var(--text-primary);
  margin-bottom: var(--spacing-2);
}

.resource-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--spacing-4);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-4);
}

.resource-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.resource-tag {
  margin-right: 0;
}

.resource-date {
  font-size: var(--font-size-sm);
}

.resource-actions {
  display: flex;
  gap: var(--spacing-2);
  margin-left: auto;
}

.resource-content {
  margin-bottom: var(--spacing-8);
}

.resource-main {
  margin-bottom: var(--spacing-6);
}

.resource-cover {
  position: relative;
  margin-bottom: var(--spacing-6);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-tertiary);
}

.resource-cover img.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
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
  margin-top: var(--spacing-2);
  font-size: var(--font-size-md);
  color: var(--text-secondary);
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

.resource-description {
  margin-bottom: var(--spacing-6);
}

.resource-description h2 {
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  margin-bottom: var(--spacing-4);
  padding-bottom: var(--spacing-2);
  border-bottom: 1px solid var(--gray-200);
}

.resource-description p {
  font-size: var(--font-size-md);
  line-height: 1.8;
  color: var(--text-secondary);
  white-space: pre-line;
}

.resource-stats {
  margin-bottom: var(--spacing-6);
}

.stats-container {
  display: flex;
  gap: var(--spacing-6);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

.card {
  background-color: var(--bg-secondary);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-4);
  margin-bottom: var(--spacing-6);
  box-shadow: var(--shadow-sm);
  transition: var(--transition-base);
}

.card:hover {
  box-shadow: var(--shadow-md);
}

.card h3 {
  font-size: var(--font-size-lg);
  color: var(--text-primary);
  margin-bottom: var(--spacing-4);
  padding-bottom: var(--spacing-2);
  border-bottom: 1px solid var(--gray-200);
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
}

.publisher-details h4 {
  margin: 0 0 var(--spacing-1);
  font-size: var(--font-size-md);
  color: var(--text-primary);
}

.publisher-details p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.related-resources .related-item {
  display: flex;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-4);
  padding-bottom: var(--spacing-4);
  border-bottom: 1px solid var(--gray-200);
}

.related-resources .related-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.related-cover {
  width: 60px;
  height: 60px;
  border-radius: var(--border-radius-md);
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-tertiary);
}

.related-info {
  flex: 1;
  min-width: 0;
}

.related-info h4 {
  font-size: var(--font-size-md);
  margin: 0 0 var(--spacing-1);
  color: var(--text-primary);
  cursor: pointer;
  transition: var(--transition-fast);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.related-info h4:hover {
  color: var(--primary-color);
}

.related-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.related-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.related-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-related, .loading-related {
  padding: var(--spacing-4) 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .resource-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-2);
  }
  
  .resource-actions {
    margin-left: 0;
    margin-top: var(--spacing-2);
    width: 100%;
    flex-wrap: wrap;
  }
  
  .stats-container {
    flex-direction: column;
    gap: var(--spacing-3);
  }
}
</style> 