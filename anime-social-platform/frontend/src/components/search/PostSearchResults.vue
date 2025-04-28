<template>
  <div class="post-search-results">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="totalPosts === 0" class="empty-state">
      <el-empty description="没有找到相关帖子" />
    </div>
    <template v-else>
      <div class="search-stats">
        共找到 <span class="highlight">{{ totalPosts }}</span> 个相关帖子
      </div>
      
      <div class="post-list">
        <div v-for="post in posts?.items" :key="post.id" class="post-item" @click="viewPostDetail(post)">
          <div class="post-title">{{ post.title }}</div>
          <div class="post-content">{{ post.content }}</div>
          <div class="post-meta">
            <span class="author">作者: {{ post.user.username }}</span>
            <span class="date">{{ formatDate(post.createdAt) }}</span>
            <div class="tags">
              <el-tag v-for="tag in post.tags" :key="tag" size="small">{{ tag }}</el-tag>
            </div>
          </div>
          <div class="post-stats">
            <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
            <span><el-icon><Star /></el-icon> {{ post.likeCount || 0 }}</span>
          </div>
        </div>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          layout="prev, pager, next"
          :total="totalPosts"
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
import { View, ChatDotRound, Star } from '@element-plus/icons-vue';
import { searchPosts } from '@/api/search';
import { Post, PostListResponse } from '@/types/post';


const props = defineProps<{
  keyword: string
}>();

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const posts = ref<PostListResponse>();
const totalPosts = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 监听关键词变化
watch(() => props.keyword, (newKeyword) => {
  if (newKeyword) {
    // 重置页码
    currentPage.value = 1;
    // 查询数据
    fetchPosts();
  }
}, { immediate: true });

// 从URL获取页码
onMounted(() => {
  const queryPage = route.query.page ? parseInt(route.query.page as string) : 1;
  if (queryPage && queryPage !== currentPage.value) {
    currentPage.value = queryPage;
  }
  
  if (props.keyword) {
    fetchPosts();
  }
});

// 监听页码变化
watch(() => route.query.page, (newPage) => {
  if (newPage && parseInt(newPage as string) !== currentPage.value) {
    currentPage.value = parseInt(newPage as string);
    if (props.keyword) {
      fetchPosts();
    }
  }
});

// 查询帖子数据
async function fetchPosts() {
  if (!props.keyword.trim()) {
    return;
  }
  
  loading.value = true;
  try {
    const response = await searchPosts(props.keyword, currentPage.value, pageSize.value);
    if (response.data.code === 200) {
      posts.value = response.data.data || [];
      totalPosts.value = response.data.data.total || 0;
    } else {
      ElMessage.error(response.data.message || '搜索失败');
    }
  } catch (error) {
    console.error('搜索帖子失败', error);
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
      tab: 'posts'
    }
  });
}

// 查看帖子详情
function viewPostDetail(post: Post) {
  router.push(`/posts/${post.id}`);
}

// 格式化日期
function formatDate(dateStr: string) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString();
}
</script>

<style scoped>
.post-search-results {
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

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-item {
  padding: 15px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.post-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.post-title {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.post-content {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.tags {
  display: flex;
  gap: 5px;
  margin-left: auto;
  flex-wrap: wrap;
}

.post-stats {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 14px;
  max-width: 300px;
}

.post-stats span {
  display: flex;
  align-items: center;
}

.post-stats .el-icon {
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
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .tags {
    margin-left: 0;
    margin-top: 5px;
  }
}
</style> 