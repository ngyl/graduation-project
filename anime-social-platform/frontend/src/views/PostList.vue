<template>
  <div class="post-list-container">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="left">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>发布新帖子
        </el-button>
      </div>
      <div class="right">
        <el-select v-model="filter.tagId" placeholder="选择标签" clearable :loading="tagsLoading">
          <el-option
            v-for="tag in postTags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
        <el-select v-model="filter.sort" placeholder="排序方式">
          <el-option label="最新发布" value="latest" />
          <el-option label="最多浏览" value="views" />
          <el-option label="最多点赞" value="likes" />
        </el-select>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list" v-loading="loading">
      <template v-if="posts.length > 0">
        <el-card v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-header">
            <div class="user-info">
              <el-avatar :size="40" :src="post.user.avatar || '/default-avatar.png'" />
              <div class="user-details">
                <span class="username">{{ post.user.username }}</span>
                <span class="time">{{ formatDate(post.created_at) }}</span>
              </div>
            </div>
            <div class="post-tags">
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
                {{ post.view_count }}
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ post.like_count }}
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.comment_count }}
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
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 发布帖子对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="发布新帖子"
      width="600px"
    >
      <el-form :model="newPost" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="newPost.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="newPost.content"
            type="textarea"
            :rows="6"
            placeholder="请输入帖子内容"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="newPost.tagIds"
            multiple
            filterable
            placeholder="请选择标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in postTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="createPost" :loading="creating">
            发布
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus, View, Star, ChatDotRound } from '@element-plus/icons-vue';
import { getPosts, createPost as createPostApi } from '@/api/post';
import { getAllTags } from '@/api/tag';
import type { Post, CreatePostRequest } from '@/types/post';
import type { TagDTO } from '@/types/tag';

const router = useRouter();

// 帖子列表数据
const posts = ref<Post[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 标签数据
const allTags = ref<TagDTO[]>([]);
const tagsLoading = ref(false);

// 筛选条件
const filter = ref({
  tagId: null as number | null,
  sort: 'latest'
});

// 发布帖子
const showCreateDialog = ref(false);
const creating = ref(false);
const newPost = ref<CreatePostRequest>({
  title: '',
  content: '',
  tagIds: []
});

// 计算属性：帖子标签
const postTags = computed(() => {
  return allTags.value.filter(tag => tag.type === 'post');
});

// 加载帖子列表
const loadPosts = async () => {
  try {
    loading.value = true;
    const response = await getPosts({
      page: currentPage.value,
      size: pageSize.value,
      tagId: filter.value.tagId,
      sort: filter.value.sort
    });
    
    if (response.data.code === 200) {
      posts.value = response.data.data.items;
      total.value = response.data.data.total;
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载帖子失败');
  } finally {
    loading.value = false;
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

// 创建帖子
const createPost = async () => {
  if (!newPost.value.title || !newPost.value.content) {
    ElMessage.warning('请填写标题和内容');
    return;
  }

  try {
    creating.value = true;
    const response = await createPostApi(newPost.value);
    if (response.data.code === 200) {
      ElMessage.success('发布成功');
      showCreateDialog.value = false;
      newPost.value = {
        title: '',
        content: '',
        tagIds: []
      };
      loadPosts();
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '发布失败');
  } finally {
    creating.value = false;
  }
};

// 查看帖子详情
const viewPost = (postId: number) => {
  router.push(`/post/${postId}`);
};

// 格式化日期
const formatDate = (date: string) => {
  return new Date(date).toLocaleString();
};

// 处理分页
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  loadPosts();
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  loadPosts();
};

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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 10px;
}

.toolbar .right {
  display: flex;
  gap: 10px;
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

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .toolbar .right {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .post-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .post-tags {
    width: 100%;
  }
}
</style> 