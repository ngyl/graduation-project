<template>
  <app-layout>
    <div class="post-edit-container">
      <el-card>
        <template #header>
          <div class="card-header">
            <h2>{{ isNewPost ? '发布新帖子' : '编辑帖子' }}</h2>
            <div class="header-actions">
              <el-tooltip content="返回" placement="top">
                <el-button @click="goBack" icon="ArrowLeft" circle plain></el-button>
              </el-tooltip>
            </div>
          </div>
        </template>
        
        <div class="form-wrapper" v-loading="loading">
          <el-form :model="postForm" label-width="80px" v-if="!error" ref="formRef" :rules="rules">
            <el-form-item label="标题" prop="title">
              <el-input 
                v-model="postForm.title" 
                placeholder="请输入帖子标题" 
                maxlength="100" 
                show-word-limit 
                :autofocus="isNewPost"
              />
            </el-form-item>
            
            <el-form-item label="标签" prop="tagIds">
              <el-select
                v-model="postForm.tagIds"
                multiple
                filterable
                placeholder="请选择标签"
                style="width: 100%"
                :loading="tagsLoading"
              >
                <el-option
                  v-for="tag in postTags"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="内容" prop="content">
              <div class="editor-tips" v-if="isNewPost">
                <el-alert
                  title="编辑提示"
                  type="info"
                  description="在此编辑您的帖子内容，支持多段落文本。内容丰富的帖子更容易引起讨论！"
                  :closable="false"
                  show-icon
                />
              </div>
              <el-input
                v-model="postForm.content"
                type="textarea"
                :rows="12"
                placeholder="请输入帖子内容"
                maxlength="5000"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item>
              <div class="form-actions">
                <el-button @click="goBack">取消</el-button>
                <el-button 
                  type="primary" 
                  @click="savePost" 
                  :loading="saving"
                  :disabled="!postForm.title.trim() || !postForm.content.trim()"
                >
                  {{ isNewPost ? '发布帖子' : '保存修改' }}
                </el-button>
              </div>
            </el-form-item>
          </el-form>
          
          <!-- 错误提示 -->
          <el-result
            v-if="error"
            icon="error"
            title="加载失败"
            :sub-title="errorMessage"
          >
            <template #extra>
              <el-button type="primary" @click="fetchPostData">重试</el-button>
              <el-button @click="goBack">返回</el-button>
            </template>
          </el-result>
        </div>
      </el-card>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import AppLayout from '@/components/AppLayout.vue';
import { getPostDetail, updatePost, createPost as createPostApi } from '@/api/post';
import { getAllTags } from '@/api/tag';
import type { CreatePostRequest } from '@/types/post';
import type { TagDTO } from '@/types/tag';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在2到100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 5, max: 5000, message: '内容长度应在5到5000个字符之间', trigger: 'blur' }
  ]
};

// 响应式状态
const loading = ref(true);
const saving = ref(false);
const error = ref(false);
const errorMessage = ref('');
const tagsLoading = ref(false);
const allTags = ref<TagDTO[]>([]);
const originalPostData = ref<any>(null);

// 判断是否是新帖子
const isNewPost = computed(() => {
  // 从路由判断是否是新建帖子页面
  if (route.name === 'PostNew') return true;
  
  if (!originalPostData.value) return false;
  // 如果内容为空或内容非常少，视为新帖子
  return !originalPostData.value.content || originalPostData.value.content.length < 10;
});

// 表单数据
const postForm = ref<CreatePostRequest>({
  title: '',
  content: '',
  tagIds: []
});

// 计算属性：帖子标签
const postTags = computed(() => {
  return allTags.value.filter(tag => tag.type === 'post');
});

// 获取帖子数据
const fetchPostData = async () => {
  // 如果是新建帖子页面，不需要获取数据
  if (route.name === 'PostNew') {
    loading.value = false;
    document.title = '发布新帖子 - 动漫社区';
    return;
  }

  const postId = parseInt(route.params.id as string);
  
  if (isNaN(postId)) {
    error.value = true;
    errorMessage.value = '无效的帖子ID';
    loading.value = false;
    return;
  }
  
  loading.value = true;
  error.value = false;
  
  try {
    // 获取帖子详情
    const response = await getPostDetail(postId);
    if (response.data.code === 200) {
      const postData = response.data.data;
      originalPostData.value = postData;
      
      // 检查权限 - 只有帖子作者可以编辑
      if (postData.user.id !== userStore.user?.id) {
        router.push(`/posts/${postId}`);
        ElMessage.warning('您没有权限编辑此帖子');
        return;
      }
      
      // 填充表单
      postForm.value.title = postData.title;
      postForm.value.content = postData.content;
      postForm.value.tagIds = postData.tags.map((tag: any) => tag.id);
      
      // 设置页面标题
      document.title = isNewPost.value 
        ? '发布新帖子 - 动漫社区' 
        : `编辑: ${postData.title} - 动漫社区`;
    } else {
      error.value = true;
      errorMessage.value = response.data.message || '获取帖子详情失败';
    }
  } catch (err: any) {
    error.value = true;
    errorMessage.value = err.message || '获取帖子详情失败';
    console.error('获取帖子详情失败:', err);
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

// 保存帖子
const savePost = async () => {
  // 验证表单
  if (!postForm.value.title.trim()) {
    ElMessage.warning('请输入帖子标题');
    return;
  }
  
  if (!postForm.value.content.trim()) {
    ElMessage.warning('请输入帖子内容');
    return;
  }
  
  saving.value = true;
  
  try {
    // 新建帖子流程
    if (isNewPost.value) {
      const response = await createPostApi(postForm.value);
      if (response.data.code === 200) {
        ElMessage.success('发布成功');
        // 跳转到帖子详情页
        const newPostId = response.data.data.id;
        router.push(`/posts/${newPostId}`);
      } else {
        ElMessage.error(response.data.message || '发布失败');
      }
    } 
    // 更新现有帖子流程
    else {
      const postId = parseInt(route.params.id as string);
      const response = await updatePost(postId, postForm.value);
      
      if (response.data.code === 200) {
        ElMessage.success('保存成功');
        // 跳转到帖子详情页
        router.push(`/posts/${postId}`);
      } else {
        ElMessage.error(response.data.message || '保存失败');
      }
    }
  } catch (error: any) {
    console.error('操作失败:', error);
    ElMessage.error(error.response?.data?.message || '操作失败');
  } finally {
    saving.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 初始化
onMounted(() => {
  // 检查是否登录
  if (!userStore.isLoggedIn) {
    router.push('/login');
    return;
  }
  
  fetchPostData();
  loadTags();
});
</script>

<style scoped>
.post-edit-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.form-wrapper {
  padding: 10px 0;
}

.editor-tips {
  margin-bottom: 15px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .post-edit-container {
    padding: 10px;
  }
}
</style> 