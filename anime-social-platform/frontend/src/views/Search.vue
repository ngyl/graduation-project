<template>
  <app-layout>
    <div class="search-container">
      <!-- 搜索框区域 -->
      <div class="search-header">
        <h1>搜索结果</h1>
        <div class="search-input-box">
          <el-input
            v-model="keyword"
            placeholder="搜索内容..."
            clearable
            @keyup.enter="search"
          >
            <template #append>
              <el-button @click="search">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 标签页切换 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="全部" name="all">
          <div v-if="loading" class="loading-state">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="searchResult.totalResults === 0" class="empty-state">
            <el-empty description="没有找到相关结果" />
          </div>
          <div v-else class="search-results">
            <div class="search-stats">
              共找到 <span class="highlight">{{ searchResult.totalResults }}</span> 个结果
              (帖子: {{ searchResult.totalPosts }}, 资源: {{ searchResult.totalResources }}, 用户: {{ searchResult.totalUsers }})
            </div>
            
            <!-- 展示帖子结果 -->
            <div v-if="searchResult.posts && searchResult.posts.length > 0" class="result-section">
              <h2>帖子</h2>
              <div class="result-list">
                <div v-for="item in searchResult.posts" :key="`post-${item.id}`" class="result-item" @click="navigate(item)">
                  <div class="result-title">{{ item.title }}</div>
                  <div class="result-content">{{ item.content }}</div>
                  <div class="result-meta">
                    <span class="author">作者: {{ item.author }}</span>
                    <span class="date">{{ formatDate(item.createdAt) }}</span>
                    <div class="tags">
                      <el-tag v-for="tag in item.tags" :key="tag" size="small">{{ tag }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 展示资源结果 -->
            <div v-if="searchResult.resources && searchResult.resources.length > 0" class="result-section">
              <h2>资源</h2>
              <div class="result-list">
                <div v-for="item in searchResult.resources" :key="`resource-${item.id}`" class="result-item" @click="navigate(item)">
                  <div class="result-title">{{ item.title }}</div>
                  <div class="result-content">{{ item.content }}</div>
                  <div class="result-meta">
                    <span class="author">上传者: {{ item.author }}</span>
                    <span class="date">{{ formatDate(item.createdAt) }}</span>
                    <div class="tags">
                      <el-tag v-for="tag in item.tags" :key="tag" size="small" type="success">{{ tag }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 展示用户结果 -->
            <div v-if="searchResult.users && searchResult.users.length > 0" class="result-section">
              <h2>用户</h2>
              <div class="result-list">
                <div v-for="item in searchResult.users" :key="`user-${item.id}`" class="result-item user-item" @click="navigate(item)">
                  <div class="result-title">{{ item.title }}</div>
                  <div class="result-content">{{ item.content }}</div>
                  <div class="result-meta">
                    <span class="date">注册于: {{ formatDate(item.createdAt) }}</span>
                    <div class="tags">
                      <el-tag v-for="tag in item.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 分页器 -->
            <div class="pagination-container">
              <el-pagination
                layout="prev, pager, next"
                :total="searchResult.totalResults"
                :page-size="pageSize"
                :current-page="currentPage"
                @current-change="handlePageChange"
              />
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="帖子" name="posts">
          <post-search-results :keyword="keyword" />
        </el-tab-pane>
        
        <el-tab-pane label="资源" name="resources">
          <resource-search-results :keyword="keyword" />
        </el-tab-pane>
        
        <el-tab-pane label="用户" name="users">
          <user-search-results :keyword="keyword" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

import AppLayout from '@/components/AppLayout.vue';
import PostSearchResults from '@/components/search/PostSearchResults.vue';
import ResourceSearchResults from '@/components/search/ResourceSearchResults.vue';
import UserSearchResults from '@/components/search/UserSearchResults.vue';

import { searchAll } from '@/api/search';
import type { SearchResult, SearchResultItem } from '@/types/search';

const route = useRoute();
const router = useRouter();

// 搜索相关状态
const keyword = ref('');
const loading = ref(false);
const activeTab = ref('all');
const currentPage = ref(1);
const pageSize = ref(10);
const searchResult = ref<SearchResult>({
  totalResults: 0,
  posts: [],
  totalPosts: 0,
  resources: [],
  totalResources: 0,
  users: [],
  totalUsers: 0
});

// 从URL中获取搜索参数
onMounted(() => {
  const queryKeyword = route.query.keyword as string;
  const queryTab = route.query.tab as string;
  const queryPage = route.query.page ? parseInt(route.query.page as string) : 1;
  
  if (queryKeyword) {
    keyword.value = queryKeyword;
    if (queryTab && ['all', 'posts', 'resources', 'users'].includes(queryTab)) {
      activeTab.value = queryTab;
    }
    currentPage.value = queryPage;
    
    // 执行搜索
    if (activeTab.value === 'all') {
      performSearch();
    }
  }
});

// 监听路由参数变化
watch(() => route.query, (newQuery) => {
  const newKeyword = newQuery.keyword as string;
  const newTab = newQuery.tab as string;
  const newPage = newQuery.page ? parseInt(newQuery.page as string) : 1;
  
  if (newKeyword && newKeyword !== keyword.value) {
    keyword.value = newKeyword;
  }
  
  if (newTab && ['all', 'posts', 'resources', 'users'].includes(newTab) && newTab !== activeTab.value) {
    activeTab.value = newTab;
  }
  
  if (newPage && newPage !== currentPage.value) {
    currentPage.value = newPage;
  }
  
  // 当关键词或页码变化时重新搜索
  if (activeTab.value === 'all' && (newKeyword !== keyword.value || newPage !== currentPage.value)) {
    performSearch();
  }
}, { immediate: true });

// 执行搜索
async function performSearch() {
  if (!keyword.value.trim()) {
    return;
  }
  
  loading.value = true;
  try {
    const response = await searchAll(keyword.value, currentPage.value, pageSize.value);
    if (response.data.code === 200) {
      searchResult.value = response.data.data;
    } else {
      ElMessage.error(response.data.message || '搜索失败');
    }
  } catch (error) {
    console.error('搜索失败', error);
    ElMessage.error('搜索失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 处理搜索
function search() {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }
  
  // 重置页码
  currentPage.value = 1;
  
  // 更新URL参数
  updateUrlParams();
  
  // 如果当前是全部搜索，执行搜索
  if (activeTab.value === 'all') {
    performSearch();
  }
}

// 处理标签切换
function handleTabChange() {
  updateUrlParams();
}

// 处理页码变化
function handlePageChange(page: number) {
  currentPage.value = page;
  updateUrlParams();
  
  if (activeTab.value === 'all') {
    performSearch();
  }
}

// 更新URL参数
function updateUrlParams() {
  router.push({
    path: '/search',
    query: {
      keyword: keyword.value,
      tab: activeTab.value,
      page: currentPage.value.toString()
    }
  });
}

// 导航到详情页
function navigate(item: SearchResultItem) {
  router.push(item.url);
}

// 格式化日期
function formatDate(dateStr: string) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString();
}
</script>

<style scoped>
.search-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  margin-bottom: 20px;
}

.search-header h1 {
  margin-bottom: 15px;
}

.search-input-box {
  max-width: 600px;
}

.search-stats {
  margin-bottom: 20px;
  color: #666;
}

.highlight {
  color: #409EFF;
  font-weight: bold;
}

.result-section {
  margin-bottom: 30px;
}

.result-section h2 {
  font-size: 18px;
  margin-bottom: 15px;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.result-item {
  padding: 15px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.result-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.result-title {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.result-content {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  line-height: 1.5;
}

.result-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #999;
}

.tags {
  display: flex;
  gap: 5px;
  margin-left: auto;
  flex-wrap: wrap;
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

.user-item {
  border-left: 3px solid #67C23A;
}

@media (max-width: 768px) {
  .search-container {
    padding: 10px;
  }
  
  .result-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .tags {
    margin-left: 0;
    margin-top: 5px;
  }
}
</style> 