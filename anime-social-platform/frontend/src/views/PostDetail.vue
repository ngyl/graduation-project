<template>
  <app-layout>
    <div class="post-detail-container" v-loading="loading">
      <!-- 帖子内容区域 -->
      <div class="post-content-wrapper" v-if="post">
        <!-- 帖子标题和操作区 -->
        <div class="post-header">
          <div class="header-left">
            <el-button @click="goBack" icon="ArrowLeft" circle plain class="back-button"></el-button>
            <h1 class="post-title">{{ post.title }}</h1>
          </div>
          <div class="post-actions" v-if="isLoggedIn">
            <el-dropdown v-if="canEditPost || canDeletePost">
              <el-button type="primary" plain size="small">
                操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleEdit" v-if="canEditPost">编辑帖子</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete" v-if="canDeletePost">删除帖子</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <!-- 作者信息和统计 -->
        <div class="post-meta">
          <div class="author-info">
            <router-link :to="`/profile/${post.userDTO.id}`" class="author-link">
              <el-avatar :src="post.userDTO.avatar || '/images/default-avatar.png'" :size="40"></el-avatar>
              <span class="author-name">{{ post.userDTO.username }}</span>
            </router-link>
          </div>
          <div class="post-stats">
            <span class="post-time">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(post.createdAt) }}
            </span>
            <span class="post-views">
              <el-icon><View /></el-icon>
              {{ post.viewCount }} 次浏览
            </span>
          </div>
        </div>
        
        <!-- 标签列表 -->
        <div class="post-tags" v-if="post.tags && post.tags.length > 0">
          <el-tag
            v-for="tag in post.tags"
            :key="tag.id"
            class="post-tag"
            size="small"
            effect="plain"
          >
            {{ tag.name }}
          </el-tag>
        </div>
        
        <!-- 帖子正文 -->
        <div class="post-body" v-html="formatContent(post.content)"></div>
        
        <!-- 点赞区域 -->
        <div class="post-like-section">
          <el-button 
            type="danger" 
            :plain="!isLiked" 
            size="large" 
            round 
            @click="toggleLike"
            :disabled="!isLoggedIn"
          >
            <el-icon><Star /></el-icon>
            {{ isLiked ? '已点赞' : '点赞' }} ({{ post.likeCount }})
          </el-button>
        </div>
        
        <!-- 评论区域 -->
        <div class="post-comments-section">
          <h2 class="section-title">
            评论 ({{ post.commentCount }})
          </h2>
          
          <!-- 评论输入框 -->
          <div class="comment-input" v-if="isLoggedIn">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="发表您的评论..."
              @keyup.ctrl.enter="submitComment"
            />
            <div class="comment-button">
              <el-button type="primary" @click="submitComment" :disabled="!commentContent.trim()">
                发表评论
              </el-button>
            </div>
          </div>
          <el-alert
            v-else
            title="请登录后发表评论"
            type="info"
            :closable="false"
            show-icon
          />
          
          <!-- 评论列表 -->
          <div class="comments-list" v-loading="loadingComments">
            <div v-if="comments.length === 0" class="no-comments">
              <el-empty description="暂无评论，来发表第一条评论吧！" />
            </div>
            <div v-else class="comments-wrapper">
              <div 
                v-for="comment in comments" 
                :key="comment.id" 
                class="comment-item"
              >
                <div class="comment-user">
                  <router-link :to="`/profile/${comment.user.id}`">
                    <el-avatar 
                      :src="comment.user.avatar || '/images/default-avatar.png'" 
                      :size="32"
                    ></el-avatar>
                  </router-link>
                </div>
                <div class="comment-content">
                  <div class="comment-header">
                    <router-link :to="`/profile/${comment.user.id}`" class="username">
                      {{ comment.user.username }}
                    </router-link>
                    <span class="comment-time">{{ formatDateTime(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-text">
                    {{ comment.content }}
                  </div>
                  <div class="comment-actions">
                    <el-button 
                      type="text" 
                      size="small" 
                      @click="replyToComment(comment)"
                      v-if="isLoggedIn"
                    >
                      回复
                    </el-button>
                    <el-button 
                      type="text" 
                      size="small" 
                      @click="deleteComment(comment.id)"
                      v-if="isAdmin"
                    >
                      删除
                    </el-button>
                  </div>
                  
                  <!-- 嵌套回复 -->
                  <div class="comment-replies" v-if="comment.replies && comment.replies.length > 0">
                    <div 
                      v-for="reply in comment.replies" 
                      :key="reply.id" 
                      class="reply-item"
                    >
                      <div class="reply-user">
                        <router-link :to="`/profile/${reply.user.id}`">
                          <el-avatar 
                            :src="reply.user.avatar || '/images/default-avatar.png'" 
                            :size="24"
                          ></el-avatar>
                        </router-link>
                      </div>
                      <div class="reply-content">
                        <div class="reply-header">
                          <router-link :to="`/profile/${reply.user.id}`" class="username">
                            {{ reply.user.username }}
                          </router-link>
                          <span class="reply-time">{{ formatDateTime(reply.createdAt) }}</span>
                        </div>
                        <div class="reply-text">
                          {{ reply.content }}
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 回复输入框 -->
                  <div class="reply-input" v-if="replyingTo === comment.id">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="`回复 ${comment.user.username}...`"
                    />
                    <div class="reply-buttons">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="submitReply(comment.id)"
                        :disabled="!replyContent.trim()"
                      >
                        提交回复
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 加载更多评论 -->
            <div class="load-more" v-if="hasMoreComments">
              <el-button text @click="loadMoreComments">
                加载更多评论
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 加载失败提示 -->
      <el-result
        v-if="error && !loading"
        icon="error"
        title="加载失败"
        :sub-title="errorMessage"
      >
        <template #extra>
          <el-button type="primary" @click="fetchPostDetail">重试</el-button>
          <el-button @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Clock, View, Star, ArrowDown, ArrowLeft } from '@element-plus/icons-vue';
import { format } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import AppLayout from '@/components/AppLayout.vue';
import * as postApi from '@/api/post';
import * as commentApi from '@/api/comment';
import { useUserStore } from '@/stores/user';
import type { Post } from '@/types/post';
import type { CommentDTO } from '@/types/comment';

// 更新评论类型定义
interface CommentWithUser {
  id: number;
  postId: number;
  content: string;
  createdAt: string;
  parentId: number | null;
  replies?: CommentWithUser[];
  user: {
    id: number;
    username: string;
    avatar: string;
  };
}

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 响应式状态
const post = ref<Post | null>(null);
const loading = ref(true);
const error = ref(false);
const errorMessage = ref('');
const isLiked = ref(false);

// 评论相关状态
const comments = ref<CommentWithUser[]>([]);
const loadingComments = ref(false);
const commentContent = ref('');
const replyContent = ref('');
const replyingTo = ref<number | null>(null);
const commentPage = ref(1);
const commentSize = ref(10);
const hasMoreComments = ref(false);

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn);
const currentUserId = computed(() => userStore.user?.id || 0);
const isAdmin = computed(() => userStore.user?.isAdmin || false);

// 只有帖子作者可以编辑
const canEditPost = computed(() => {
  if (!post.value || !isLoggedIn.value) return false;
  return currentUserId.value === post.value.userDTO.id;
});

// 帖子作者和管理员可以删除
const canDeletePost = computed(() => {
  if (!post.value || !isLoggedIn.value) return false;
  return currentUserId.value === post.value.userDTO.id || userStore.user?.isAdmin;
});

// 获取帖子详情
const fetchPostDetail = async () => {
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
    const response = await postApi.getPostDetail(postId);
    if (response.data.code === 200) {
      post.value = response.data.data;
      document.title = `${post.value.title} - 动漫社区`;
      
      // 获取评论和点赞状态
      fetchComments();
      if (isLoggedIn.value) {
        checkLikeStatus();
      }
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

// 获取评论列表
const fetchComments = async () => {
  if (!post.value) return;
  
  loadingComments.value = true;
  try {
    const response = await commentApi.getCommentsByPost(post.value.id);
    if (response.data.code === 200) {
      // 格式化评论数据以符合前端展示需求
      comments.value = response.data.data.map((comment: CommentDTO) => ({
        id: comment.id,
        postId: comment.postId,
        content: comment.content,
        createdAt: comment.createdAt,
        parentId: comment.parentId,
        user: {
          id: comment.userId,
          username: comment.username,
          avatar: comment.userAvatar
        },
        replies: comment.replies ? comment.replies.map((reply: CommentDTO) => ({
          id: reply.id,
          postId: reply.postId,
          content: reply.content,
          createdAt: reply.createdAt,
          parentId: reply.parentId,
          user: {
            id: reply.userId,
            username: reply.username,
            avatar: reply.userAvatar
          }
        })) : []
      }));
      
      // 判断是否还有更多评论
      hasMoreComments.value = response.data.data.length >= commentSize.value;
    } else {
      ElMessage.error(response.data.message || '获取评论失败');
    }
  } catch (error) {
    console.error('获取评论失败:', error);
    ElMessage.error('获取评论失败');
  } finally {
    loadingComments.value = false;
  }
};

// 加载更多评论
const loadMoreComments = () => {
  commentPage.value++;
  fetchComments();
};

// 检查点赞状态
const checkLikeStatus = async () => {
  if (!post.value || !isLoggedIn.value) return;
  
  try {
    // 这里应该改为实际的检查点赞API
    // const response = await postApi.checkLikeStatus(post.value.id);
    // isLiked.value = response.data.data;
    
    // 模拟点赞状态
    isLiked.value = false;
  } catch (error) {
    console.error('检查点赞状态失败:', error);
  }
};

// 切换点赞状态
const toggleLike = async () => {
  if (!post.value || !isLoggedIn.value) return;
  
  try {
    if (isLiked.value) {
      await postApi.unlikePost(post.value.id);
      if (post.value.likeCount > 0) {
        post.value.likeCount--;
      }
    } else {
      await postApi.likePost(post.value.id);
      post.value.likeCount++;
    }
    isLiked.value = !isLiked.value;
  } catch (error) {
    console.error('操作点赞失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

// 提交评论
const submitComment = async () => {
  if (!post.value || !commentContent.value.trim() || !isLoggedIn.value) return;
  
  try {
    const response = await commentApi.createComment({
      postId: post.value.id,
      content: commentContent.value.trim()
    });
    
    if (response.data.code === 200) {
      // 将新评论添加到列表
      const newComment = response.data.data;
      comments.value.unshift({
        id: newComment.id,
        postId: newComment.postId,
        content: newComment.content,
        createdAt: newComment.createdAt,
        parentId: newComment.parentId,
        user: {
          id: userStore.user?.id || 0,
          username: userStore.user?.username || '',
          avatar: userStore.user?.avatar || '/images/default-avatar.png'
        },
        replies: []
      });
      
      // 清空评论内容
      commentContent.value = '';
      
      // 更新帖子评论数
      if (post.value) {
        post.value.commentCount = (post.value.commentCount || 0) + 1;
      }
      
      ElMessage.success('评论发布成功');
    } else {
      ElMessage.error(response.data.message || '评论发布失败');
    }
  } catch (error) {
    console.error('评论发布失败:', error);
    ElMessage.error('评论发布失败');
  }
};

// 回复评论
const replyToComment = (comment: CommentWithUser) => {
  replyingTo.value = comment.id;
  replyContent.value = '';
};

// 取消回复
const cancelReply = () => {
  replyingTo.value = null;
  replyContent.value = '';
};

// 提交回复
const submitReply = async (parentId: number) => {
  if (!post.value || !replyContent.value.trim() || !isLoggedIn.value) return;
  
  try {
    const response = await commentApi.createComment({
      postId: post.value.id,
      content: replyContent.value.trim(),
      parentId: parentId
    });
    
    if (response.data.code === 200) {
      // 将新回复添加到相应的评论中
      const newReply = response.data.data;
      const parentComment = comments.value.find(c => c.id === parentId);
      
      if (parentComment) {
        if (!parentComment.replies) {
          parentComment.replies = [];
        }
        
        parentComment.replies.push({
          id: newReply.id,
          postId: newReply.postId,
          content: newReply.content,
          createdAt: newReply.createdAt,
          parentId: newReply.parentId,
          user: {
            id: userStore.user?.id || 0,
            username: userStore.user?.username || '',
            avatar: userStore.user?.avatar || '/images/default-avatar.png'
          }
        });
      }
      
      // 清空回复内容并关闭回复框
      replyContent.value = '';
      replyingTo.value = null;
      
      // 更新帖子评论数
      if (post.value) {
        post.value.commentCount = (post.value.commentCount || 0) + 1;
      }
      
      ElMessage.success('回复发布成功');
    } else {
      ElMessage.error(response.data.message || '回复发布失败');
    }
  } catch (error) {
    console.error('回复发布失败:', error);
    ElMessage.error('回复发布失败');
  }
};

// 删除评论
const deleteComment = async (commentId: number) => {
  try {
    ElMessageBox.confirm('确定要删除此评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      const response = await commentApi.deleteComment(commentId);
      
      if (response.data.code === 200) {
        // 找到要删除的评论
        const commentIndex = comments.value.findIndex(c => c.id === commentId);
        
        // 如果是一级评论
        if (commentIndex !== -1) {
          // 更新帖子评论数（减去评论及其所有回复）
          if (post.value) {
            const replyCount = comments.value[commentIndex].replies?.length || 0;
            post.value.commentCount = (post.value.commentCount || 0) - 1 - replyCount;
          }
          
          // 从评论列表中移除
          comments.value.splice(commentIndex, 1);
          ElMessage.success('评论已删除');
          return;
        }
        
        // 如果是回复评论，遍历所有一级评论找到对应的回复
        for (const comment of comments.value) {
          if (comment.replies) {
            const replyIndex = comment.replies.findIndex(r => r.id === commentId);
            if (replyIndex !== -1) {
              // 更新帖子评论数
              if (post.value) {
                post.value.commentCount = (post.value.commentCount || 0) - 1;
              }
              
              // 从回复列表中移除
              comment.replies.splice(replyIndex, 1);
              ElMessage.success('回复已删除');
              break;
            }
          }
        }
      } else {
        ElMessage.error(response.data.message || '删除评论失败');
      }
    }).catch(() => {
      // 用户取消删除操作
    });
  } catch (error) {
    console.error('删除评论失败:', error);
    ElMessage.error('删除评论失败');
  }
};

// 判断是否可以管理评论（只有管理员可以删除评论）
const canManageComment = (comment: CommentWithUser) => {
  if (!isLoggedIn.value) return false;
  return isAdmin.value;
};

// 编辑帖子
const handleEdit = () => {
  if (!post.value || !canEditPost.value) return;
  router.push(`/posts/${post.value.id}/edit`);
};

// 删除帖子
const handleDelete = async () => {
  if (!post.value || !canDeletePost.value) return;
  
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？此操作不可撤销。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await postApi.deletePost(post.value.id);
    ElMessage.success('帖子已删除');
    router.push('/posts');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error);
      ElMessage.error('删除帖子失败，请重试');
    }
  }
};

// 格式化日期时间
const formatDateTime = (dateTimeString: string) => {
  try {
    const date = new Date(dateTimeString);
    return format(date, 'yyyy年MM月dd日 HH:mm', { locale: zhCN });
  } catch (error) {
    return dateTimeString;
  }
};

// 格式化帖子内容（处理换行等）
const formatContent = (content: string) => {
  if (!content) return '';
  return content.replace(/\n/g, '<br>');
};

// 返回上一页
const goBack = () => {
  router.push('/posts');
};

// 在组件挂载时获取数据
onMounted(() => {
  fetchPostDetail();
});
</script>

<style scoped>
.post-detail-container {
  max-width: var(--container-width);
  margin: 0 auto;
  padding: var(--spacing-4);
}

.post-content-wrapper {
  background-color: var(--bg-white);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-6);
  box-shadow: var(--shadow-sm);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-4);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.back-button {
  flex-shrink: 0;
}

.post-title {
  font-size: var(--font-size-2xl);
  color: var(--text-primary);
  margin: 0;
  word-break: break-word;
  flex: 1;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4);
  padding-bottom: var(--spacing-4);
  border-bottom: 1px solid var(--gray-200);
}

.author-info {
  display: flex;
  align-items: center;
}

.author-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: var(--text-primary);
}

.author-name {
  margin-left: var(--spacing-2);
  font-weight: 500;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.post-time, .post-views {
  display: flex;
  align-items: center;
}

.post-time .el-icon, .post-views .el-icon {
  margin-right: var(--spacing-1);
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-2);
  margin-bottom: var(--spacing-4);
}

.post-body {
  font-size: var(--font-size-md);
  line-height: 1.8;
  color: var(--text-primary);
  margin-bottom: var(--spacing-6);
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}

.post-like-section {
  display: flex;
  justify-content: center;
  margin: var(--spacing-6) 0;
  padding: var(--spacing-4) 0;
  border-top: 1px solid var(--gray-200);
  border-bottom: 1px solid var(--gray-200);
}

.post-comments-section {
  margin-top: var(--spacing-6);
}

.section-title {
  font-size: var(--font-size-lg);
  color: var(--text-primary);
  margin-bottom: var(--spacing-4);
}

.comment-input {
  margin-bottom: var(--spacing-6);
}

.comment-button {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-2);
}

.comments-list {
  margin-top: var(--spacing-4);
}

.no-comments {
  padding: var(--spacing-6) 0;
  color: var(--text-hint);
  text-align: center;
}

.comment-item {
  display: flex;
  padding: var(--spacing-3) 0;
  border-bottom: 1px solid var(--gray-100);
}

.comment-content {
  flex: 1;
  margin-left: var(--spacing-3);
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-1);
}

.username {
  font-weight: 500;
  color: var(--text-primary);
  text-decoration: none;
  margin-right: var(--spacing-2);
}

.comment-time, .reply-time {
  font-size: var(--font-size-xs);
  color: var(--text-hint);
}

.comment-text, .reply-text {
  line-height: 1.6;
  margin-bottom: var(--spacing-2);
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: var(--spacing-2);
}

.comment-replies {
  margin-top: var(--spacing-2);
  margin-left: var(--spacing-4);
  padding: var(--spacing-2);
  background-color: var(--gray-50);
  border-radius: var(--border-radius-md);
}

.reply-item {
  display: flex;
  padding: var(--spacing-2) 0;
  border-bottom: 1px solid var(--gray-100);
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-content {
  flex: 1;
  margin-left: var(--spacing-2);
}

.reply-input {
  margin-top: var(--spacing-2);
}

.reply-buttons {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-2);
  margin-top: var(--spacing-2);
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-4);
}

@media (max-width: 768px) {
  .post-content-wrapper {
    padding: var(--spacing-4);
  }
  
  .post-header {
    flex-direction: column;
  }
  
  .post-actions {
    margin-top: var(--spacing-2);
  }
  
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .post-stats {
    margin-top: var(--spacing-2);
  }
  
  .post-title {
    font-size: var(--font-size-xl);
  }
}
</style> 