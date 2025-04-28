<template>
  <div class="user-search-results">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="users.length === 0" class="empty-state">
      <el-empty description="没有找到相关用户" />
    </div>
    <template v-else>
      <div class="search-stats">
        共找到 <span class="highlight">{{ totalUsers }}</span> 个相关用户
      </div>
      
      <div class="user-list">
        <div v-for="user in users" :key="user.id" class="user-item" @click="viewUserProfile(user)">
          <div class="user-avatar">
            <el-avatar :size="60" :src="user.avatar || defaultAvatar"></el-avatar>
          </div>
          <div class="user-info">
            <div class="user-name">{{ user.username }}</div>
            <div class="user-bio">{{ user.bio || '这个人很懒，什么都没写~' }}</div>
            <div class="user-meta">
              <span>注册于: {{ formatDate(user.registerTime) }}</span>
              <span>帖子: {{ user.postCount || 0 }}</span>
              <span>粉丝: {{ user.followerCount || 0 }}</span>
            </div>
            <div class="tags" v-if="user.tags && user.tags.length > 0">
              <el-tag v-for="tag in user.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          layout="prev, pager, next"
          :total="totalUsers"
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
import { searchUsers } from '@/api/search';
import { UserDetail } from '@/types/user';

const props = defineProps<{
  keyword: string
}>();

const route = useRoute();
const router = useRouter();
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const loading = ref(false);
const users = ref<UserDetail[]>([]);
const totalUsers = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 监听关键词变化
watch(() => props.keyword, (newKeyword) => {
  if (newKeyword) {
    // 重置页码
    currentPage.value = 1;
    // 查询数据
    fetchUsers();
  }
}, { immediate: true });

// 从URL获取页码
onMounted(() => {
  const queryPage = route.query.page ? parseInt(route.query.page as string) : 1;
  if (queryPage && queryPage !== currentPage.value) {
    currentPage.value = queryPage;
  }
  
  if (props.keyword) {
    fetchUsers();
  }
});

// 监听页码变化
watch(() => route.query.page, (newPage) => {
  if (newPage && parseInt(newPage as string) !== currentPage.value) {
    currentPage.value = parseInt(newPage as string);
    if (props.keyword) {
      fetchUsers();
    }
  }
});

// 查询用户数据
async function fetchUsers() {
  if (!props.keyword.trim()) {
    return;
  }
  
  loading.value = true;
  try {
    const response = await searchUsers(props.keyword, currentPage.value, pageSize.value);
    if (response.data.code === 200) {
      users.value = response.data.data.data || [];
      totalUsers.value = response.data.data.total || 0;
    } else {
      ElMessage.error(response.data.message || '搜索失败');
    }
  } catch (error) {
    console.error('搜索用户失败', error);
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
      tab: 'users'
    }
  });
}

// 查看用户资料
function viewUserProfile(user: UserDetail) {
  router.push(`/profile/${user.id}`);
}

// 格式化日期
function formatDate(dateStr: string) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString();
}
</script>

<style scoped>
.user-search-results {
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

.user-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-item {
  display: flex;
  padding: 15px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid #409EFF;
}

.user-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.user-avatar {
  margin-right: 15px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.user-bio {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  line-height: 1.5;
}

.user-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.tags {
  display: flex;
  gap: 5px;
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

@media (max-width: 768px) {
  .user-item {
    flex-direction: column;
  }
  
  .user-avatar {
    margin-right: 0;
    margin-bottom: 10px;
    align-self: center;
  }
  
  .user-meta {
    flex-direction: column;
    gap: 5px;
  }
}
</style> 