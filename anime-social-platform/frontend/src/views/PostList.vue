<template>
  <app-layout>
    <div class="post-list-container">
      <div class="page-header">
        <h1>交流讨论</h1>
        <div class="filter-bar">
          <el-select v-model="filter.tagId" placeholder="标签" clearable :loading="tagsLoading" class="filter-select">
            <el-option
              v-for="tag in postTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
          <el-select v-model="filter.sort" placeholder="排序" class="filter-select">
            <el-option label="最新发布" value="latest" />
            <el-option label="最多浏览" value="views" />
            <el-option label="最多点赞" value="likes" />
          </el-select>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="post-list" v-loading="loading">
        <template v-if="sortedPosts.length > 0">
          <el-card v-for="post in sortedPosts" :key="post.id" class="post-card" :class="{ 'is-top': post.isTop }">
            <div class="post-header">
              <div class="user-info">
                <el-avatar :size="40" :src="post.userDTO.avatar || '/default-avatar.png'" />
                <div class="user-details">
                  <span class="username">{{ post.userDTO.username }}</span>
                  <span class="time">{{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
              <div class="post-tags">
                <el-tag v-if="post.isTop" type="danger" effect="dark" class="top-tag">置顶</el-tag>
                <el-tag
                  v-for="tag in post.tags"
                  :key="tag.id"
                  size="small"
                  class="tag-item"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
            </div>
            <h3 class="post-title" @click="viewPost(post.id)">{{ post.title }}</h3>
            <p class="post-content">{{ post.content }}</p>
            <div class="post-footer">
              <div class="stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ post.viewCount }}
                </span>
                <span class="stat-item">
                  <el-icon><Star /></el-icon>
                  {{ post.likeCount }}
                </span>
                <span class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ post.commentCount }}
                </span>
              </div>
              <el-button type="text" @click="viewPost(post.id)">查看详情</el-button>
            </div>
          </el-card>
        </template>
        <el-empty v-else description="暂无帖子" />
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="displayTotal"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 新增帖子按钮 -->
      <el-button 
        v-if="userStore.isLoggedIn" 
        type="primary"
        class="create-button"
        @click="createNewPost"
      >
        <el-icon><Plus /></el-icon>
        发布新帖子
      </el-button>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus, View, Star, ChatDotRound } from '@element-plus/icons-vue';
import { getPosts } from '@/api/post';
import { getAllTags } from '@/api/tag';
import type { Post } from '@/types/post';
import type { TagDTO } from '@/types/tag';
import AppLayout from '@/components/AppLayout.vue';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

// 帖子列表数据
const posts = ref<Post[]>([]);
const topPosts = ref<Post[]>([]); // 存储置顶帖子
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 计算属性：完整的帖子列表（置顶帖子始终在前）
const sortedPosts = computed(() => {
  return [...topPosts.value, ...posts.value.filter(post => !post.isTop)];
});

// 标签数据
const allTags = ref<TagDTO[]>([]);
const tagsLoading = ref(false);

// 筛选条件
const filter = ref({
  tagId: null as number | null,
  sort: 'latest'
});

// 计算属性：帖子标签
const postTags = computed(() => {
  return allTags.value.filter(tag => tag.type === 'post');
});

// 加载帖子列表
const loadPosts = async () => {
  try {
    loading.value = true;
    
    // 1. 先加载所有置顶帖子（不进行分页）
    await loadTopPosts();
    
    // 2. 加载当前页的普通帖子
    const response = await getPosts({
      page: currentPage.value,
      size: pageSize.value,
      tagId: filter.value.tagId,
      sort: filter.value.sort
    });
    
    if (response.data.code === 200) {
      // 过滤掉置顶帖子，避免重复显示
      posts.value = response.data.data.items.filter(post => !post.isTop);
      
      // 调整总数，减去置顶帖子的数量避免分页错误
      let totalCount = response.data.data.total;
      if (topPosts.value.length > 0) {
        // 减去置顶帖子数量，确保分页计算正确
        totalCount = Math.max(0, totalCount - topPosts.value.length);
      }
      total.value = totalCount;
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载帖子失败');
  } finally {
    loading.value = false;
  }
};

// 单独加载置顶帖子
const loadTopPosts = async () => {
  try {
    // 请求第一页数据，尽可能多，然后筛选出所有置顶帖子
    const topResponse = await getPosts({
      page: 1,
      size: 50, // 假设置顶帖子不会超过50个
      tagId: filter.value.tagId,
      sort: filter.value.sort
    });
    
    if (topResponse.data.code === 200) {
      // 筛选出所有置顶帖子
      topPosts.value = topResponse.data.data.items.filter(post => post.isTop);
    }
  } catch (error) {
    console.error('加载置顶帖子失败:', error);
    // 出错时置空，不影响普通帖子显示
    topPosts.value = [];
  }
};

// 加载标签
const loadTags = async () => {
  try {
    tagsLoading.value = true;
    const response = await getAllTags();
    if (response.data.code === 200) {
      allTags.value = response.data.data;
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载标签失败');
  } finally {
    tagsLoading.value = false;
  }
};

// 发布新帖子 - 直接跳转到编辑页面
const createNewPost = () => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  // 直接跳转到新建帖子页面
  router.push('/posts/new');
};

// 查看帖子详情
const viewPost = (postId: number) => {
  router.push(`/posts/${postId}`);
};

// 格式化日期
const formatDate = (date: string) => {
  return new Date(date).toLocaleString();
};

// 处理分页
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1; // 修改每页数量时重置为第一页
  loadPosts();
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  loadPosts();
};

// 计算显示的总记录数（总记录数 - 置顶帖子数量）
// 确保分页系统正确工作
const displayTotal = computed(() => {
  // 如果总数小于等于置顶帖子数量，显示0
  if (total.value <= topPosts.value.length) {
    return 0;
  }
  return total.value;
});

// 监听筛选条件变化
watch([() => filter.value.tagId, () => filter.value.sort], () => {
  currentPage.value = 1;
  loadPosts();
});

// 组件挂载时加载数据
onMounted(() => {
  loadPosts();
  loadTags();
});
</script>

<style scoped>
.post-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 16px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-select {
  width: 150px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.post-card {
  transition: all 0.3s;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: bold;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.post-tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.post-title {
  margin: 0 0 10px;
  font-size: 18px;
  color: #333;
  cursor: pointer;
}

.post-title:hover {
  color: #409EFF;
}

.post-content {
  margin: 0 0 15px;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
}

.stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.create-button {
  position: fixed;
  right: 24px;
  bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    gap: 8px;
  }
  
  .filter-select {
    width: 100%;
  }
  
  .post-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .post-tags {
    width: 100%;
  }
  
  .create-button {
    right: 16px;
    bottom: 16px;
  }
}

.post-card.is-top {
  border-left: 4px solid #F56C6C;
  background-color: #FFF9F9;
  position: relative;
  overflow: hidden;
}

.post-card.is-top::before {
  content: "";
  position: absolute;
  top: -10px;
  right: -10px;
  width: 30px;
  height: 30px;
  background-color: #F56C6C;
  transform: rotate(45deg);
  z-index: 1;
}

.top-tag {
  margin-right: 5px;
  z-index: 2;
  position: relative;
}
</style> 