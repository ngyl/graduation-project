<template>
  <admin-layout>
    <div class="resource-management">
      <h1>资源管理</h1>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索资源标题或描述"
          clearable
          @keyup.enter="handleSearch"
          class="search-input"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><search /></el-icon>
            </el-button>
          </template>
        </el-input>
        
        <el-select
          v-model="fileType"
          placeholder="文件类型"
          clearable
          @change="handleSearch"
          class="filter-select"
        >
          <el-option label="全部类型" value="" />
          <el-option label="图片" value="image" />
          <el-option label="视频" value="video" />
          <el-option label="文档" value="document" />
          <el-option label="音频" value="audio" />
          <el-option label="其他" value="other" />
        </el-select>
        
        <el-select
          v-model="selectedTagId"
          placeholder="按标签筛选"
          clearable
          @change="handleSearch"
          class="filter-select"
        >
          <el-option label="全部标签" value="" />
          <el-option 
            v-for="tag in resourceTags" 
            :key="tag.id" 
            :label="tag.name" 
            :value="tag.id" 
          />
        </el-select>
        
        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          @change="handleSearch"
          class="filter-select"
        >
          <el-option label="最新上传" value="latest" />
          <el-option label="下载最多" value="downloads" />
          <el-option label="点赞最多" value="likes" />
          <el-option label="文件大小" value="size" />
        </el-select>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY/MM/DD"
          value-format="YYYY-MM-DD"
          @change="handleSearch"
          class="date-picker"
        />
      </div>
      
      <!-- 资源列表 -->
      <el-table
        v-loading="loading"
        :data="resourceList"
        style="width: 100%; margin-top: 20px;"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="预览" width="100">
          <template #default="scope">
            <div class="resource-preview">
              <el-image 
                v-if="isImageFile(scope.row.fileType)"
                :src="scope.row.fileUrl" 
                fit="cover"
                :preview-src-list="[scope.row.fileUrl]"
                class="resource-thumbnail"
              ></el-image>
              <el-icon v-else :size="30" class="file-icon">
                <document v-if="isDocumentFile(scope.row.fileType)" />
                <video-play v-else-if="isVideoFile(scope.row.fileType)" />
                <headset v-else-if="isAudioFile(scope.row.fileType)" />
                <folder v-else />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="username" label="上传者" width="120" />
        <el-table-column prop="uploadTime" label="上传时间" width="180" />
        <el-table-column prop="fileType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getFileTypeTag(scope.row.fileType)">
              {{ getFileTypeLabel(scope.row.fileType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="150">
          <template #default="scope">
            <div class="tag-list">
              <el-tag 
                v-for="tag in scope.row.tags" 
                :key="tag.id"
                size="small"
                effect="plain"
                class="resource-tag"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="!scope.row.tags || scope.row.tags.length === 0" class="no-tags">
                无标签
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载数" width="100" sortable />
        <el-table-column prop="likeCount" label="点赞数" width="100" sortable />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button 
                size="small" 
                @click="handlePreview(scope.row)"
              >
                查看
              </el-button>
              <el-button 
                size="small" 
                type="primary"
                @click="handleDownload(scope.row)"
              >
                下载
              </el-button>
              <el-button 
                size="small" 
                type="danger"
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <!-- 资源预览对话框 -->
      <el-dialog
        v-model="previewVisible"
        :title="currentResource?.title || '资源预览'"
        width="70%"
      >
        <div v-if="currentResource" class="resource-detail">
          <div class="resource-header">
            <div class="uploader-info">
              <div class="uploader-name">上传者: {{ currentResource.username }}</div>
              <div class="upload-time">上传时间: {{ currentResource.uploadTime }}</div>
            </div>
            <div class="resource-stats">
              <span><el-icon><download /></el-icon> {{ currentResource.downloadCount }}</span>
              <span><el-icon><star /></el-icon> {{ currentResource.likeCount }}</span>
            </div>
          </div>
          
          <div class="resource-description">
            <div class="description-label">资源描述:</div>
            <p>{{ currentResource.description }}</p>
          </div>
          
          <div class="resource-content">
            <div v-if="isImageFile(currentResource.fileType)" class="image-preview">
              <el-image 
                :src="currentResource.fileUrl" 
                :preview-src-list="[currentResource.fileUrl]"
                fit="contain"
                class="full-image"
              ></el-image>
            </div>
            <div v-else-if="isVideoFile(currentResource.fileType)" class="video-preview">
              <video 
                :src="currentResource.fileUrl" 
                controls 
                class="full-video"
              ></video>
            </div>
            <div v-else-if="isAudioFile(currentResource.fileType)" class="audio-preview">
              <audio 
                :src="currentResource.fileUrl" 
                controls 
                class="full-audio"
              ></audio>
            </div>
            <div v-else class="file-preview">
              <el-icon :size="80" class="big-file-icon">
                <document v-if="isDocumentFile(currentResource.fileType)" />
                <folder v-else />
              </el-icon>
              <div class="file-info">
                <div>类型: {{ getFileTypeLabel(currentResource.fileType) }}</div>
                <div>大小: {{ formatFileSize(currentResource.fileSize) }}</div>
              </div>
            </div>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="previewVisible = false">关闭</el-button>
            <el-button 
              type="primary"
              @click="handleDownload(currentResource)"
            >
              下载资源
            </el-button>
            <el-button 
              type="danger"
              @click="handleDeleteFromPreview"
            >
              删除资源
            </el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 删除确认对话框 -->
      <el-dialog
        v-model="deleteDialogVisible"
        title="删除资源"
        width="30%"
      >
        <p>确定要删除资源"{{ currentResource?.title }}"吗？此操作不可撤销。</p>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="deleteDialogVisible = false">取消</el-button>
            <el-button type="danger" @click="confirmDelete">确认删除</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import * as adminApi from '@/api/admin';
import * as tagApi from '@/api/tag';
import { 
  Search, 
  Document, 
  VideoPlay, 
  Headset, 
  Folder, 
  Download,
  Star
} from '@element-plus/icons-vue';
import AdminLayout from '@/components/AdminLayout.vue';
import type { TagDTO } from '@/types/tag';

// 资源接口定义
interface Resource {
  id: number;
  title: string;
  description: string;
  fileUrl: string;
  fileType: string;
  fileSize: number;
  userId: number;
  username: string;
  uploadTime: string;
  downloadCount: number;
  likeCount: number;
  tags: TagDTO[];
}

// 安全获取属性值，防止因属性不存在导致页面崩溃
const getSafeValue = (obj: any, path: string, defaultValue: any = '') => {
  if (!obj) return defaultValue;
  
  const keys = path.split('.');
  let value = obj;
  
  for (const key of keys) {
    if (value === null || value === undefined || typeof value !== 'object') {
      return defaultValue;
    }
    value = value[key];
  }
  
  return value !== null && value !== undefined ? value : defaultValue;
};

// 添加资源数据适配器，统一处理不同格式的资源数据
const adaptResourceData = (resource: any): Resource => {
  return {
    id: getSafeValue(resource, 'id', 0),
    title: getSafeValue(resource, 'title', '未知标题'),
    description: getSafeValue(resource, 'description', ''),
    fileUrl: getSafeValue(resource, 'fileUrl', '') || getSafeValue(resource, 'url', ''),
    fileType: getSafeValue(resource, 'fileType', '') || getSafeValue(resource, 'type', ''),
    fileSize: getSafeValue(resource, 'fileSize', 0) || getSafeValue(resource, 'size', 0),
    userId: getSafeValue(resource, 'userId', 0) || getSafeValue(resource, 'user.id', 0),
    username: getSafeValue(resource, 'username', '') || getSafeValue(resource, 'user.username', '未知用户'),
    uploadTime: getSafeValue(resource, 'uploadTime', '') || getSafeValue(resource, 'createdAt', ''),
    downloadCount: getSafeValue(resource, 'downloadCount', 0) || getSafeValue(resource, 'downloads', 0),
    likeCount: getSafeValue(resource, 'likeCount', 0) || getSafeValue(resource, 'likes', 0),
    tags: getSafeValue(resource, 'tags', [])
  };
};

// 状态
const loading = ref(false);
const resourceList = ref<Resource[]>([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchKeyword = ref('');
const fileType = ref('');
const sortBy = ref('latest');
const dateRange = ref([]);

// 资源预览
const previewVisible = ref(false);
const currentResource = ref<Resource | null>(null);

// 删除确认
const deleteDialogVisible = ref(false);

// 标签筛选
const selectedTagId = ref<number | string>('');
const allTags = ref<TagDTO[]>([]);
const tagsLoading = ref(false);

// 计算属性：资源标签
const resourceTags = computed(() => {
  return allTags.value.filter(tag => tag.type === 'resource');
});

// 获取资源列表
const fetchResources = async () => {
  loading.value = true;
  try {
    // 构建请求参数
    const params = {
      page: page.value,
      size: pageSize.value,
      tagId: selectedTagId.value || null,
      sort: sortBy.value || 'latest'
    };
    
    // 添加调试信息
    console.log('获取资源列表，筛选条件:', { 
      searchKeyword: searchKeyword.value,
      fileType: fileType.value,
      selectedTagId: selectedTagId.value,
      sortBy: sortBy.value,
      dateRange: dateRange.value,
      ...params
    });
    
    let response;
    
    if (searchKeyword.value) {
      // 调用搜索API
      console.log('正在搜索资源:', searchKeyword.value);
      response = await adminApi.searchResources(searchKeyword.value, page.value, pageSize.value);
    } else {
      // 调用获取全部API
      console.log('正在获取全部资源列表');
      response = await adminApi.getAllResources(
        page.value, 
        pageSize.value, 
        selectedTagId.value ? Number(selectedTagId.value) : null,
        sortBy.value
      );
    }
    
    // 打印完整响应，帮助调试
    console.log('资源API完整响应:', response);
    
    if (response.data && response.data.code === 200) {
      const data = response.data.data as any;
      
      // 添加调试信息
      console.log('资源数据详细内容:', data);
      
      // 特殊处理：检查data是否为null或undefined
      if (!data) {
        console.warn('返回的资源数据为空');
        resourceList.value = [];
        total.value = 0;
        return;
      }
      
      let rawResources: any[] = [];
      
      if (data && data.list && Array.isArray(data.list)) {
        // 返回格式是 {list: [...], total: number}
        console.log('处理list格式数据');
        rawResources = data.list;
        total.value = data.total || 0;
      } else if (data && Array.isArray(data)) {
        // 返回格式是数组
        console.log('处理数组格式数据');
        rawResources = data;
        total.value = data.length;
      } else if (data && data.items && Array.isArray(data.items)) {
        // 返回格式是 {items: [...], total: number}
        console.log('处理items格式数据');
        rawResources = data.items;
        total.value = data.total || 0;
      } else if (data && data.content && Array.isArray(data.content)) {
        // Spring Data JPA格式 {content: [...], totalElements: number}
        console.log('处理content格式数据');
        rawResources = data.content;
        total.value = data.totalElements || 0;
      } else if (typeof data === 'object' && Object.keys(data).length > 0) {
        // 如果是对象且有键，尝试查找第一个数组类型的属性
        console.log('尝试自动检测数组属性');
        const arrayProps = Object.keys(data).filter(key => Array.isArray(data[key]));
        if (arrayProps.length > 0) {
          const arrayProp = arrayProps[0];
          console.log(`找到数组属性: ${arrayProp}`);
          rawResources = data[arrayProp];
          
          // 尝试查找可能的总数属性
          const possibleTotalProps = ['total', 'totalCount', 'totalElements', 'count', 'size'];
          const totalProp = possibleTotalProps.find(prop => typeof data[prop] === 'number');
          total.value = totalProp ? data[totalProp] : rawResources.length;
        } else {
          rawResources = [];
          total.value = 0;
          console.error('无法在返回数据中找到数组属性');
        }
      } else {
        rawResources = [];
        total.value = 0;
        console.error('返回的资源数据格式不正确:', data);
      }
      
      // 使用适配器处理每一个资源项
      let processedResources = rawResources.map(item => adaptResourceData(item));
      
      // 前端筛选：根据文件类型进行筛选
      if (fileType.value) {
        console.log(`应用文件类型筛选: ${fileType.value}`);
        processedResources = processedResources.filter(resource => {
          switch (fileType.value) {
            case 'image':
              return isImageFile(resource.fileType);
            case 'video':
              return isVideoFile(resource.fileType);
            case 'audio':
              return isAudioFile(resource.fileType);
            case 'document':
              return isDocumentFile(resource.fileType);
            case 'other':
              return !isImageFile(resource.fileType) && 
                     !isVideoFile(resource.fileType) && 
                     !isAudioFile(resource.fileType) && 
                     !isDocumentFile(resource.fileType);
            default:
              return true;
          }
        });
      }
      
      // 前端筛选：根据日期范围进行筛选
      if (dateRange.value && dateRange.value.length === 2) {
        console.log(`应用日期筛选: ${dateRange.value[0]} 至 ${dateRange.value[1]}`);
        const startDate = new Date(dateRange.value[0]);
        const endDate = new Date(dateRange.value[1]);
        endDate.setHours(23, 59, 59, 999); // 设置为当天结束时间
        
        processedResources = processedResources.filter(resource => {
          const uploadDate = new Date(resource.uploadTime);
          return uploadDate >= startDate && uploadDate <= endDate;
        });
      }
      
      // 前端排序：根据选择的排序方式进行排序
      if (sortBy.value) {
        console.log(`应用排序方式: ${sortBy.value}`);
        switch (sortBy.value) {
          case 'downloads':
            processedResources.sort((a, b) => b.downloadCount - a.downloadCount);
            break;
          case 'likes':
            processedResources.sort((a, b) => b.likeCount - a.likeCount);
            break;
          case 'size':
            processedResources.sort((a, b) => b.fileSize - a.fileSize);
            break;
          default: // 默认按最新上传排序
            processedResources.sort((a, b) => new Date(b.uploadTime).getTime() - new Date(a.uploadTime).getTime());
            break;
        }
      }
      
      resourceList.value = processedResources;
      
      // 打印最终结果
      console.log('最终处理后的资源列表:', resourceList.value);
      console.log('总数:', resourceList.value.length);
      total.value = resourceList.value.length; // 更新过滤后的总数
    } else {
      const errorMsg = (response.data && response.data.message) ?? '获取资源列表失败';
      console.error('API响应错误:', errorMsg);
      ElMessage.error(errorMsg);
    }
  } catch (error: any) {
    console.error('获取资源列表出错:', error);
    ElMessage.error(error.message ?? '获取资源列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  page.value = 1;
  fetchResources();
};

// 处理页面大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchResources();
};

// 处理页码变化
const handleCurrentChange = (p: number) => {
  page.value = p;
  fetchResources();
};

// 文件类型判断辅助函数
const isImageFile = (type: string) => {
  return type && (type.startsWith('image/') || type === 'image');
};

const isVideoFile = (type: string) => {
  return type && (type.startsWith('video/') || type === 'video');
};

const isAudioFile = (type: string) => {
  return type && (type.startsWith('audio/') || type === 'audio');
};

const isDocumentFile = (type: string) => {
  return type && (
    type.startsWith('application/pdf') || 
    type.startsWith('application/msword') || 
    type.startsWith('application/vnd.openxmlformats-officedocument') ||
    type === 'document'
  );
};

// 获取文件类型标签
const getFileTypeLabel = (type: string) => {
  if (isImageFile(type)) return '图片';
  if (isVideoFile(type)) return '视频';
  if (isAudioFile(type)) return '音频';
  if (isDocumentFile(type)) return '文档';
  return '其他';
};

// 获取文件类型标签样式
const getFileTypeTag = (type: string) => {
  if (isImageFile(type)) return 'success';
  if (isVideoFile(type)) return 'danger';
  if (isAudioFile(type)) return 'warning';
  if (isDocumentFile(type)) return 'primary';
  return 'info';
};

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (!size) return '0 KB';
  
  const kb = size / 1024;
  
  if (kb < 1024) {
    return kb.toFixed(2) + ' KB';
  } else {
    const mb = kb / 1024;
    if (mb < 1024) {
      return mb.toFixed(2) + ' MB';
    } else {
      const gb = mb / 1024;
      return gb.toFixed(2) + ' GB';
    }
  }
};

// 处理资源预览
const handlePreview = (resource: Resource) => {
  currentResource.value = resource;
  previewVisible.value = true;
};

// 处理资源下载
const handleDownload = (resource: Resource | null) => {
  if (!resource) return;
  
  // 创建一个临时链接并点击它来下载文件
  const link = document.createElement('a');
  link.href = resource.fileUrl;
  link.download = resource.title;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  
  // 增加下载计数（这里仅演示，实际应该调用API）
  ElMessage.success('开始下载资源');
};

// 处理删除
const handleDelete = (resource: Resource) => {
  currentResource.value = resource;
  deleteDialogVisible.value = true;
};

// 从预览对话框删除
const handleDeleteFromPreview = () => {
  if (!currentResource.value) return;
  
  previewVisible.value = false;
  deleteDialogVisible.value = true;
};

// 确认删除
const confirmDelete = async () => {
  if (!currentResource.value) return;
  
  try {
    const response = await adminApi.deleteResource(currentResource.value.id);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('资源删除成功');
      
      // 更新本地数据
      const index = resourceList.value.findIndex(r => r.id === currentResource.value!.id);
      if (index !== -1) {
        resourceList.value.splice(index, 1);
      }
      
      // 如果是从预览对话框删除的，关闭预览对话框
      if (previewVisible.value) {
        previewVisible.value = false;
      }
      
      deleteDialogVisible.value = false;
    } else {
      ElMessage.error((response.data && response.data.message) ?? '删除失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message ?? '删除失败');
  }
};

// 加载标签
const loadTags = async () => {
  try {
    tagsLoading.value = true;
    const response = await tagApi.getAllTags();
    if (response.data.code === 200) {
      allTags.value = response.data.data;
      console.log('标签列表加载成功:', allTags.value);
    }
  } catch (error: any) {
    console.error('加载标签失败:', error);
    ElMessage.error(error.response?.data?.message || '加载标签失败');
  } finally {
    tagsLoading.value = false;
  }
};

onMounted(() => {
  fetchResources();
  loadTags();
});
</script>

<style scoped>
.resource-management {
  max-width: 1400px;
  margin: 0 auto;
}

h1 {
  margin-bottom: 30px;
  color: #303133;
  border-bottom: 1px solid #EBEEF5;
  padding-bottom: 15px;
}

.search-container {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
}

.filter-select {
  width: 150px;
}

.date-picker {
  width: 350px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.resource-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50px;
}

.resource-thumbnail {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.file-icon {
  color: #909399;
}

.resource-detail {
  padding: 10px;
}

.resource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
}

.uploader-name {
  font-weight: bold;
  font-size: 16px;
}

.upload-time {
  color: #909399;
  font-size: 14px;
}

.resource-stats {
  display: flex;
  gap: 20px;
}

.resource-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
}

.resource-description {
  margin-bottom: 20px;
}

.description-label {
  font-weight: bold;
  margin-bottom: 10px;
}

.resource-content {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.image-preview, .video-preview, .audio-preview, .file-preview {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.full-image {
  max-width: 100%;
  max-height: 400px;
}

.full-video, .full-audio {
  width: 100%;
  max-width: 600px;
}

.file-preview {
  flex-direction: column;
  gap: 20px;
}

.big-file-icon {
  color: #409EFF;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  color: #606266;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.resource-tag {
  margin-right: 3px;
  margin-bottom: 3px;
}

.no-tags {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}
</style> 