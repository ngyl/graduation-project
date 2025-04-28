<template>
  <admin-layout>
    <div class="post-management">
      <h1>帖子管理</h1>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索帖子标题或内容"
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
          v-model="postStatus"
          placeholder="帖子状态"
          clearable
          @change="handleSearch"
          class="filter-select"
        >
          <el-option label="全部状态" value="" />
          <el-option label="已置顶" value="top" />
          <el-option label="普通帖子" value="normal" />
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
            v-for="tag in postTags" 
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
          <el-option label="最新发布" value="latest" />
          <el-option label="浏览最多" value="views" />
          <el-option label="点赞最多" value="likes" />
          <el-option label="评论最多" value="comments" />
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
      
      <!-- 帖子列表 -->
      <el-table
        v-loading="loading"
        :data="postList"
        style="width: 100%; margin-top: 20px;"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip>
          <template #default="scope">
            <div class="post-title">
              <el-tag v-if="scope.row.isTop" type="danger" size="small">置顶</el-tag>
              {{ scope.row.title }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column prop="viewCount" label="浏览量" width="100" sortable />
        <el-table-column prop="likeCount" label="点赞数" width="100" sortable />
        <el-table-column prop="commentCount" label="评论数" width="100" sortable />
        <el-table-column label="标签" width="180">
          <template #default="scope">
            <div class="tag-list">
              <el-tag 
                v-for="tag in scope.row.tags" 
                :key="tag.id"
                size="small"
                effect="plain"
                class="post-tag"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="!scope.row.tags || scope.row.tags.length === 0" class="no-tags">
                无标签
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
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
                :type="scope.row.isTop ? 'warning' : 'primary'"
                @click="handleTopChange(scope.row)"
              >
                {{ scope.row.isTop ? '取消置顶' : '置顶' }}
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
      
      <!-- 帖子预览对话框 -->
      <el-dialog
        v-model="previewVisible"
        :title="currentPost?.title || '帖子预览'"
        width="70%"
      >
        <div v-if="currentPost" class="post-preview">
          <div class="post-header">
            <div class="post-author">
              <el-avatar :size="40" :src="currentPost.authorAvatar"></el-avatar>
              <div class="author-info">
                <div class="author-name">{{ currentPost.authorName }}</div>
                <div class="post-time">{{ currentPost.createdAt }}</div>
              </div>
            </div>
            <div class="post-stats">
              <span><el-icon><view /></el-icon> {{ currentPost.viewCount }}</span>
              <span><el-icon><star /></el-icon> {{ currentPost.likeCount }}</span>
              <span><el-icon><chat-dot-round /></el-icon> {{ currentPost.commentCount }}</span>
            </div>
          </div>
          
          <div class="post-content" v-html="currentPost.content"></div>
          
          <div class="post-tags" v-if="currentPost.tags && currentPost.tags.length > 0">
            <span class="tag-label">话题标签：</span>
            <el-tag 
              v-for="tag in currentPost.tags" 
              :key="tag.id"
              size="small"
              class="post-tag"
            >
              {{ tag.name }}
            </el-tag>
          </div>
          
          <div class="post-images" v-if="currentPost.images && currentPost.images.length > 0">
            <div class="image-label">帖子图片：</div>
            <div class="image-grid">
              <div 
                v-for="(image, index) in currentPost.images" 
                :key="index"
                class="image-item"
              >
                <el-image 
                  :src="image.url" 
                  :preview-src-list="currentPost.images.map(img => img.url)"
                  :initial-index="index"
                  fit="cover"
                ></el-image>
              </div>
            </div>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="previewVisible = false">关闭</el-button>
            <el-button 
              :type="currentPost?.isTop ? 'warning' : 'primary'"
              @click="handleTopChange(currentPost)"
            >
              {{ currentPost?.isTop ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button 
              type="danger"
              @click="handleDeleteFromPreview"
            >
              删除帖子
            </el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 删除确认对话框 -->
      <el-dialog
        v-model="deleteDialogVisible"
        title="删除帖子"
        width="30%"
      >
        <p>确定要删除帖子"{{ currentPost?.title }}"吗？此操作不可撤销。</p>
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
import { Search, View, Star, ChatDotRound } from '@element-plus/icons-vue';
import AdminLayout from '@/components/AdminLayout.vue';
import type { TagDTO } from '@/types/tag';

interface PostTag {
  id: number;
  name: string;
}

interface PostImage {
  id: number;
  url: string;
}

interface Post {
  id: number;
  title: string;
  content: string;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  createdAt: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isTop: boolean;
  tags?: PostTag[];
  images?: PostImage[];
}

// 状态
const loading = ref(false);
const postList = ref<Post[]>([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchKeyword = ref('');
const postStatus = ref('');
const sortBy = ref('latest');
const dateRange = ref([]);

// 帖子预览
const previewVisible = ref(false);
const currentPost = ref<Post | null>(null);

// 删除确认
const deleteDialogVisible = ref(false);

// 标签筛选
const selectedTagId = ref<number | string>('');
const allTags = ref<TagDTO[]>([]);
const tagsLoading = ref(false);

// 计算属性：帖子标签
const postTags = computed(() => {
  return allTags.value.filter(tag => tag.type === 'post');
});

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

// 添加帖子数据适配器，统一处理不同格式的帖子数据
const adaptPostData = (post: any): Post => {
  return {
    id: getSafeValue(post, 'id', 0),
    title: getSafeValue(post, 'title', '未知标题'),
    content: getSafeValue(post, 'content', ''),
    authorId: getSafeValue(post, 'authorId', 0) || getSafeValue(post, 'user.id', 0),
    authorName: getSafeValue(post, 'authorName', '') || getSafeValue(post, 'user.username', '未知用户'),
    authorAvatar: getSafeValue(post, 'authorAvatar', '') || getSafeValue(post, 'user.avatar', ''),
    createdAt: getSafeValue(post, 'createdAt', '') || getSafeValue(post, 'created_at', ''),
    viewCount: getSafeValue(post, 'viewCount', 0) || getSafeValue(post, 'view_count', 0),
    likeCount: getSafeValue(post, 'likeCount', 0) || getSafeValue(post, 'like_count', 0),
    commentCount: getSafeValue(post, 'commentCount', 0) || getSafeValue(post, 'comment_count', 0),
    isTop: getSafeValue(post, 'isTop', false) || getSafeValue(post, 'is_top', false),
    tags: getSafeValue(post, 'tags', []),
    images: getSafeValue(post, 'images', [])
  };
};

// 获取帖子列表
const fetchPosts = async () => {
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
    console.log('获取帖子列表，筛选条件:', { 
      searchKeyword: searchKeyword.value,
      postStatus: postStatus.value,
      selectedTagId: selectedTagId.value,
      sortBy: sortBy.value,
      dateRange: dateRange.value,
      ...params
    });
    
    let response;
    
    if (searchKeyword.value) {
      // 调用搜索API
      console.log('正在搜索帖子:', searchKeyword.value);
      response = await adminApi.searchPosts(searchKeyword.value, page.value, pageSize.value);
    } else {
      // 调用获取全部API
      console.log('正在获取全部帖子列表');
      response = await adminApi.getAllPosts(
        page.value, 
        pageSize.value, 
        selectedTagId.value ? Number(selectedTagId.value) : null,
        sortBy.value
      );
    }
    
    // 打印完整响应，帮助调试
    console.log('帖子API完整响应:', response);
    
    if (response.data && response.data.code === 200) {
      const data = response.data.data as any;
      
      // 添加调试信息
      console.log('帖子数据详细内容:', data);
      
      // 特殊处理：检查data是否为null或undefined
      if (!data) {
        console.warn('返回的帖子数据为空');
        postList.value = [];
        total.value = 0;
        return;
      }
      
      let rawPosts: any[] = [];
      
      if (data && data.list && Array.isArray(data.list)) {
        // 返回格式是 {list: [...], total: number}
        console.log('处理list格式数据');
        rawPosts = data.list;
        total.value = data.total || 0;
      } else if (data && Array.isArray(data)) {
        // 返回格式是数组
        console.log('处理数组格式数据');
        rawPosts = data;
        total.value = data.length;
      } else if (data && data.items && Array.isArray(data.items)) {
        // 返回格式是 {items: [...], total: number}
        console.log('处理items格式数据');
        rawPosts = data.items;
        total.value = data.total || 0;
      } else if (data && data.content && Array.isArray(data.content)) {
        // Spring Data JPA格式 {content: [...], totalElements: number}
        console.log('处理content格式数据');
        rawPosts = data.content;
        total.value = data.totalElements || 0;
      } else if (typeof data === 'object' && Object.keys(data).length > 0) {
        // 如果是对象且有键，尝试查找第一个数组类型的属性
        console.log('尝试自动检测数组属性');
        const arrayProps = Object.keys(data).filter(key => Array.isArray(data[key]));
        if (arrayProps.length > 0) {
          const arrayProp = arrayProps[0];
          console.log(`找到数组属性: ${arrayProp}`);
          rawPosts = data[arrayProp];
          
          // 尝试查找可能的总数属性
          const possibleTotalProps = ['total', 'totalCount', 'totalElements', 'count', 'size'];
          const totalProp = possibleTotalProps.find(prop => typeof data[prop] === 'number');
          total.value = totalProp ? data[totalProp] : rawPosts.length;
        } else {
          rawPosts = [];
          total.value = 0;
          console.error('无法在返回数据中找到数组属性');
        }
      } else {
        rawPosts = [];
        total.value = 0;
        console.error('返回的帖子数据格式不正确:', data);
      }
      
      // 使用适配器处理每一个帖子项
      let processedPosts = rawPosts.map(item => adaptPostData(item));
      
      // 前端筛选：根据帖子状态进行筛选
      if (postStatus.value) {
        console.log(`应用帖子状态筛选: ${postStatus.value}`);
        if (postStatus.value === 'top') {
          processedPosts = processedPosts.filter(post => post.isTop);
        } else if (postStatus.value === 'normal') {
          processedPosts = processedPosts.filter(post => !post.isTop);
        }
      }
      
      // 前端筛选：根据日期范围进行筛选
      if (dateRange.value && dateRange.value.length === 2) {
        console.log(`应用日期筛选: ${dateRange.value[0]} 至 ${dateRange.value[1]}`);
        const startDate = new Date(dateRange.value[0]);
        const endDate = new Date(dateRange.value[1]);
        endDate.setHours(23, 59, 59, 999); // 设置为当天结束时间
        
        processedPosts = processedPosts.filter(post => {
          const postDate = new Date(post.createdAt);
          return postDate >= startDate && postDate <= endDate;
        });
      }
      
      // 前端排序：根据选择的排序方式进行排序
      if (sortBy.value) {
        console.log(`应用排序方式: ${sortBy.value}`);
        switch (sortBy.value) {
          case 'latest':
            processedPosts.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
            break;
          case 'views':
            processedPosts.sort((a, b) => b.viewCount - a.viewCount);
            break;
          case 'likes':
            processedPosts.sort((a, b) => b.likeCount - a.likeCount);
            break;
          case 'comments':
            processedPosts.sort((a, b) => b.commentCount - a.commentCount);
            break;
        }
      }
      
      postList.value = processedPosts;
      
      // 打印最终结果
      console.log('最终处理后的帖子列表:', postList.value);
      console.log('总数:', postList.value.length);
      total.value = postList.value.length; // 更新过滤后的总数
    } else {
      const errorMsg = (response.data && response.data.message) ?? '获取帖子列表失败';
      console.error('API响应错误:', errorMsg);
      ElMessage.error(errorMsg);
    }
  } catch (error: any) {
    console.error('获取帖子列表出错:', error);
    ElMessage.error(error.message ?? '获取帖子列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  page.value = 1;
  fetchPosts();
};

// 处理页面大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchPosts();
};

// 处理页码变化
const handleCurrentChange = (p: number) => {
  page.value = p;
  fetchPosts();
};

// 处理帖子预览
const handlePreview = (post: Post) => {
  currentPost.value = post;
  previewVisible.value = true;
};

// 处理置顶状态变更
const handleTopChange = async (post: Post | null) => {
  if (!post) return;
  
  try {
    const newTopStatus = post.isTop ? 0 : 1;
    const response = await adminApi.updatePostTopStatus(post.id, newTopStatus);
    
    if (response.data && response.data.code === 200) {
      // 更新本地数据
      if (post === currentPost.value) {
        currentPost.value!.isTop = !post.isTop;
      }
      
      const index = postList.value.findIndex(p => p.id === post.id);
      if (index !== -1) {
        postList.value[index].isTop = !post.isTop;
      }
      
      ElMessage.success(post.isTop ? '已取消置顶' : '已设置置顶');
    } else {
      ElMessage.error((response.data && response.data.message) ?? '操作失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message ?? '操作失败');
  }
};

// 处理删除
const handleDelete = (post: Post) => {
  currentPost.value = post;
  deleteDialogVisible.value = true;
};

// 从预览对话框删除
const handleDeleteFromPreview = () => {
  if (!currentPost.value) return;
  
  previewVisible.value = false;
  deleteDialogVisible.value = true;
};

// 确认删除
const confirmDelete = async () => {
  if (!currentPost.value) return;
  
  try {
    const response = await adminApi.deletePost(currentPost.value.id);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('帖子删除成功');
      
      // 更新本地数据
      const index = postList.value.findIndex(p => p.id === currentPost.value!.id);
      if (index !== -1) {
        postList.value.splice(index, 1);
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
  fetchPosts();
  loadTags();
});
</script>

<style scoped>
.post-management {
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

.post-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.post-preview {
  padding: 10px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: bold;
  font-size: 16px;
}

.post-time {
  color: #909399;
  font-size: 14px;
}

.post-stats {
  display: flex;
  gap: 15px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
}

.post-content {
  margin-bottom: 20px;
  line-height: 1.6;
}

.post-tags {
  display: flex;
  align-items: center;
  margin-top: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.tag-label {
  margin-right: 10px;
  color: #606266;
}

.post-tag {
  margin-right: 3px;
  margin-bottom: 3px;
}

.no-tags {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

.post-images {
  margin-top: 20px;
}

.image-label {
  margin-bottom: 10px;
  color: #606266;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}

.image-item {
  border-radius: 4px;
  overflow: hidden;
  height: 150px;
}

.image-item .el-image {
  width: 100%;
  height: 100%;
}
</style> 