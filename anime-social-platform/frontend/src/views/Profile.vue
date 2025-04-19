<template>
  <div class="profile-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
    </div>

    <!-- 用户信息卡片 -->
    <el-card class="user-info-card">
      <div class="user-header">
        <el-avatar :size="100" :src="userInfo.avatar || '/default-avatar.png'" />
        <div class="user-details">
          <div class="username-section">
            <h2>{{ userInfo.username }}</h2>
            <el-button 
              v-if="isCurrentUser" 
              type="primary" 
              size="small" 
              @click="showEditDialog = true"
            >
              编辑资料
            </el-button>
            <el-button 
              v-else 
              type="primary" 
              size="small" 
              @click="handleFollow"
              :loading="followLoading"
            >
              {{ isFollowing ? '取消关注' : '关注' }}
            </el-button>
          </div>
          <p class="bio">{{ userInfo.bio || '这个人很懒，什么都没写~' }}</p>
          
          <!-- 标签展示区域 -->
          <div class="tags-section">
            <h3>兴趣标签</h3>
            <div class="tags-container">
              <div class="tag-group" v-if="userTags.postTags.length > 0">
                <h4>帖子标签</h4>
                <el-tag
                  v-for="tag in userTags.postTags"
                  :key="tag.id"
                  class="tag-item"
                  type="success"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
              <div class="tag-group" v-if="userTags.resourceTags.length > 0">
                <h4>资源标签</h4>
                <el-tag
                  v-for="tag in userTags.resourceTags"
                  :key="tag.id"
                  class="tag-item"
                  type="warning"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
              <div v-if="userTags.postTags.length === 0 && userTags.resourceTags.length === 0">
                <p class="no-tags">还没有添加任何标签</p>
              </div>
            </div>
          </div>
          
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">{{ stats.postCount }}</span>
              <span class="stat-label">帖子</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.favoriteCount }}</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.followingCount }}</span>
              <span class="stat-label">关注</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 编辑资料对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑个人资料"
      width="500px"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar" alt="用户头像" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input
            v-model="editForm.bio"
            type="textarea"
            :rows="3"
            placeholder="介绍一下自己吧~"
          />
        </el-form-item>
        
        <!-- 标签编辑区域 -->
        <el-form-item label="帖子标签">
          <el-select
            v-model="editForm.postTagIds"
            multiple
            filterable
            placeholder="请选择帖子标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in allTags.postTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源标签">
          <el-select
            v-model="editForm.resourceTagIds"
            multiple
            filterable
            placeholder="请选择资源标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in allTags.resourceTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSaveProfile" :loading="saveLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 内容区域 -->
    <div class="content-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的帖子" name="posts">
          <div class="empty-state" v-if="posts.length === 0">
            <el-empty description="还没有发布过帖子" />
          </div>
          <div class="post-list" v-else>
            <el-card v-for="post in posts" :key="post.id" class="post-card">
              <h3>{{ post.title }}</h3>
              <p class="post-content">{{ post.content }}</p>
              <div class="post-meta">
                <span>{{ formatDate(post.created_at) }}</span>
                <span>浏览: {{ post.view_count }}</span>
                <span>点赞: {{ post.like_count }}</span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="我的收藏" name="favorites">
          <div class="empty-state" v-if="favorites.length === 0">
            <el-empty description="还没有收藏任何资源" />
          </div>
          <div class="favorite-list" v-else>
            <el-card v-for="resource in favorites" :key="resource.id" class="resource-card">
              <h3>{{ resource.title }}</h3>
              <p class="resource-description">{{ resource.description }}</p>
              <div class="resource-meta">
                <span>{{ formatDate(resource.created_at) }}</span>
                <span>{{ resource.file_type }}</span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="我的关注" name="following">
          <div class="empty-state" v-if="following.length === 0">
            <el-empty description="还没有关注任何人" />
          </div>
          <div class="following-list" v-else>
            <el-card v-for="user in following" :key="user.id" class="user-card">
              <div class="user-info">
                <el-avatar :size="40" :src="user.avatar || '/default-avatar.png'" />
                <div class="user-details">
                  <h4>{{ user.username }}</h4>
                  <p>{{ user.bio || '这个人很懒，什么都没写~' }}</p>
                </div>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { getUserDetail, getUserPosts, getUserFavorites, getUserFollowing, updateUserInfo } from '@/api/user';
import { followUser, unfollowUser, checkFriendshipStatus, getFriendshipCounts } from '../api/friendship';
import { getAllTags, getUserTags, updateUserTags } from '@/api/tag';
import type { UserStats, Post, FavoriteResource, FollowingUser } from '@/types/user';
import type { UserInfo } from '@/types/auth';
import type { TagDTO, UserTagsDTO } from '@/types/tag';
import type { ApiResponse, FriendshipStatus, FriendshipCounts } from '../types/api';
import { ElMessage } from 'element-plus';
import { Plus, ArrowLeft } from '@element-plus/icons-vue';
import axios from 'axios';

const route = useRoute();
const userStore = useUserStore();

const userInfo = ref<UserInfo>({
  id: 0,
  username: '',
  avatar: '',
  bio: '',
  isAdmin: false
});

const stats = ref<UserStats>({
  postCount: 0,
  favoriteCount: 0,
  followingCount: 0,
  followersCount: 0
});

const activeTab = ref('posts');
const posts = ref<Post[]>([]);
const favorites = ref<FavoriteResource[]>([]);
const following = ref<FollowingUser[]>([]);

const showEditDialog = ref(false);
const saveLoading = ref(false);
const followLoading = ref(false);
const isFollowing = ref(false);

const userTags = ref<UserTagsDTO>({
  postTags: [],
  resourceTags: []
});

const allTags = ref<{
  postTags: TagDTO[];
  resourceTags: TagDTO[];
}>({
  postTags: [],
  resourceTags: []
});

const editForm = ref<{
  avatar?: string;
  bio?: string;
  postTagIds?: number[];
  resourceTagIds?: number[];
}>({
  avatar: '',
  bio: '',
  postTagIds: [],
  resourceTagIds: []
});

const isCurrentUser = computed(() => {
  return userStore.userInfo.id === Number(route.params.id);
});

// 格式化日期
const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString();
};

// 加载用户数据
const loadUserData = async () => {
  try {
    const userId = Number(route.params.id);
    if (!userId) {
      ElMessage.error('用户ID无效');
      return;
    }

    // 加载用户详情
    const detailResponse = await getUserDetail(userId);
    if (detailResponse.data.code === 200) {
      userInfo.value = detailResponse.data.data.user;
      stats.value = detailResponse.data.data.stats;
    }

    // 加载帖子列表
    const postsResponse = await getUserPosts(userId);
    if (postsResponse.data.code === 200) {
      posts.value = postsResponse.data.data;
    }

    // 加载收藏列表
    const favoritesResponse = await getUserFavorites(userId);
    if (favoritesResponse.data.code === 200) {
      favorites.value = favoritesResponse.data.data;
    }

    // 加载关注列表
    const followingResponse = await getUserFollowing(userId);
    if (followingResponse.data.code === 200) {
      following.value = followingResponse.data.data;
    }

    // 检查是否已关注
    if (!isCurrentUser.value) {
      const currentUserId = userStore.userInfo.id;
      if (currentUserId) {
        const isFollowingResponse = await checkFollowing(currentUserId, userId);
        isFollowing.value = isFollowingResponse.data.data;
      }
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载数据失败');
  }
};

// 检查是否已关注
const checkFollowing = async (userId: number, targetId: number) => {
  return axios.get<ApiResponse<boolean>>(`/api/users/${userId}/following/${targetId}`);
};

// 获取关注状态
async function getFollowStatus() {
  if (!isCurrentUser.value && userInfo.value.id) {
    try {
      const response = await checkFriendshipStatus(userInfo.value.id);
      // 使用类型断言
      const data = (response as any).data;
      if (data && data.isFollowing !== undefined) {
        isFollowing.value = data.isFollowing;
      }
    } catch (error) {
      console.error('获取关注状态失败', error);
    }
  }
}

// 获取关注和粉丝数量
async function getFollowCounts() {
  if (userInfo.value.id) {
    try {
      const response = await getFriendshipCounts(userInfo.value.id);
      // 使用类型断言
      const data = (response as any).data;
      if (data) {
        stats.value.followingCount = data.following || 0;
        stats.value.followersCount = data.followers || 0;
      }
    } catch (error) {
      console.error('获取关注数量失败', error);
    }
  }
}

// 关注/取消关注
async function handleFollow() {
  if (!userInfo.value.id) return;
  
  followLoading.value = true;
  try {
    if (isFollowing.value) {
      // 取消关注
      const response = await unfollowUser(userInfo.value.id);
      // 使用类型断言
      if ((response as any).code === 200) {
        isFollowing.value = false;
        ElMessage.success('已取消关注');
      } else {
        ElMessage.error((response as any).message || '操作失败');
      }
    } else {
      // 关注
      const response = await followUser(userInfo.value.id);
      // 使用类型断言
      if ((response as any).code === 200) {
        isFollowing.value = true;
        ElMessage.success('关注成功');
      } else {
        ElMessage.error((response as any).message || '操作失败');
      }
    }
    // 更新关注数量
    await getFollowCounts();
  } catch (error) {
    console.error('关注操作失败', error);
    ElMessage.error('操作失败，请稍后重试');
  } finally {
    followLoading.value = false;
  }
}

// 处理头像上传
const handleAvatarSuccess = (response: any) => {
  if (response.code === 200) {
    editForm.value.avatar = response.data.url;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.message || '头像上传失败');
  }
};

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 加载标签数据
const loadTags = async () => {
  try {
    // 加载用户标签
    const userTagsResponse = await getUserTags(Number(route.params.id));
    if (userTagsResponse.data.code === 200) {
      userTags.value = userTagsResponse.data.data;
    }

    // 加载所有可用标签
    const [postTagsResponse, resourceTagsResponse] = await Promise.all([
      getAllTags(),
      getAllTags()
    ]);

    if (postTagsResponse.data.code === 200) {
      allTags.value.postTags = postTagsResponse.data.data.filter(tag => tag.type === 'post');
    }
    if (resourceTagsResponse.data.code === 200) {
      allTags.value.resourceTags = resourceTagsResponse.data.data.filter(tag => tag.type === 'resource');
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载标签失败');
  }
};

// 修改保存个人资料方法
const handleSaveProfile = async () => {
  try {
    saveLoading.value = true;
    
    // 更新用户基本信息
    const userInfoResponse = await updateUserInfo(Number(route.params.id), {
      avatar: editForm.value.avatar,
      bio: editForm.value.bio
    });
    
    // 更新用户标签
    const tagsResponse = await updateUserTags(Number(route.params.id), {
      postTagIds: editForm.value.postTagIds,
      resourceTagIds: editForm.value.resourceTagIds
    });
    
    if (userInfoResponse.data.code === 200 && tagsResponse.data.code === 200) {
      ElMessage.success('保存成功');
      showEditDialog.value = false;
      loadUserData();
      loadTags();
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '保存失败');
  } finally {
    saveLoading.value = false;
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadUserData();
  loadTags();
  getFollowStatus();
  getFollowCounts();
});
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.user-info-card {
  margin-bottom: 20px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-details {
  flex: 1;
}

.user-details h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.bio {
  margin: 10px 0;
  color: #666;
}

.user-stats {
  display: flex;
  gap: 20px;
  margin-top: 15px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.content-tabs {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.post-card,
.resource-card,
.user-card {
  margin-bottom: 15px;
}

.post-content,
.resource-description {
  margin: 10px 0;
  color: #666;
}

.post-meta,
.resource-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info h4 {
  margin: 0;
  font-size: 16px;
}

.username-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
}

.avatar-uploader .avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
}

.avatar-uploader .avatar-uploader-icon:hover {
  border-color: #409EFF;
}

.tags-section {
  margin: 20px 0;
}

.tags-section h3 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #333;
}

.tags-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.tag-group h4 {
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.no-tags {
  color: #999;
  font-size: 14px;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }

  .user-header {
    flex-direction: column;
    text-align: center;
  }

  .user-stats {
    justify-content: center;
  }

  .post-meta,
  .resource-meta {
    flex-direction: column;
    gap: 5px;
  }
}

.back-button {
  margin-bottom: 20px;
}
</style> 