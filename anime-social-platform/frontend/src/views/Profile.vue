<template>
  <app-layout>
    <div class="profile-container" :key="`profile-${route.params.id}`">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
      </div>

      <!-- 用户信息卡片 -->
      <el-card class="user-info-card">
        <div class="user-header">
          <el-avatar :size="100" :src="userDetail.avatar || '/default-avatar.png'" />
          <div class="user-details">
            <div class="username-section">
              <h2>{{ userDetail.username }}</h2>
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
            <p class="bio">{{ userDetail.bio || '这个人很懒，什么都没写~' }}</p>
            
            <!-- 标签展示区域 -->
            <div class="tags-section">
              <h3>兴趣标签</h3>
              <div class="tags-container">
                <div class="tag-group" v-if="userDetail.tags.length > 0">
                  <el-tag
                    v-for="tag in userDetail.tags"
                    :key="tag.id"
                    class="tag-item"
                    type="success"
                  >
                    {{ tag.name }}
                  </el-tag>
                </div>
                <div v-if="userDetail.tags.length === 0">
                  <p class="no-tags">还没有添加任何标签</p>
                </div>
              </div>
            </div>
            
            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-value">{{ userDetail.postCount }}</span>
                <span class="stat-label">帖子</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ userDetail.favoriteCount }}</span>
                <span class="stat-label">收藏</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ userDetail.followingCount }}</span>
                <span class="stat-label">关注</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ userDetail.followerCount }}</span>
                <span class="stat-label">粉丝</span>
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
        <el-tabs v-model="editActiveTab" class="edit-tabs">
          <el-tab-pane label="基本资料" name="basic">
            <!-- 头像编辑 -->
            <div class="edit-section">
              <h3>头像</h3>
              <el-upload
                class="avatar-uploader"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleAvatarChange"
              >
                <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar" alt="用户头像" />
                <img v-else-if="userDetail.avatar" :src="userDetail.avatar" class="avatar" alt="用户头像" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">支持jpg、png格式，大小不超过2MB</div>
            </div>

            <!-- 个人简介编辑 -->
            <div class="edit-section">
              <h3>个人简介</h3>
              <el-input
                v-model="editForm.bio"
                type="textarea"
                :rows="3"
                placeholder="介绍一下自己吧~"
              />
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="兴趣标签" name="tags">
            <!-- 帖子标签编辑 -->
            <div class="edit-section">
              <h3>帖子标签</h3>
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
            </div>

            <!-- 资源标签编辑 -->
            <div class="edit-section">
              <h3>资源标签</h3>
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
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showEditDialog = false">取消</el-button>
            <el-button 
              type="primary" 
              @click="handleSaveProfile" 
              :loading="saveLoading"
              :disabled="!isFormChanged"
            >
              保存
            </el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 内容区域 -->
      <div class="content-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="我的帖子" name="posts">
            <div class="empty-state" v-if="posts.length === 0 && !loadingPosts">
              <el-empty description="还没有发布过帖子" />
            </div>
            <div v-else-if="loadingPosts" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>
            <div class="post-list" v-else>
              <el-card v-for="post in posts" :key="post.id" class="post-card" @click="navigateToPost(post.id)" :class="{ clickable: true }">
                <h3>{{ post.title }}</h3>
                <p class="post-content">{{ post.content }}</p>
                <div class="post-meta">
                  <span>{{ formatDate(post.createdAt) }}</span>
                  <span>浏览: {{ post.viewCount }}</span>
                  <span>点赞: {{ post.likeCount }}</span>
                </div>
              </el-card>
              
              <!-- 分页 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="postsPage"
                  v-model:page-size="postsPageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  :total="postsTotal"
                  @size-change="handlePostsSizeChange"
                  @current-change="handlePostsPageChange"
                />
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="我的资源" name="resources">
            <div class="empty-state" v-if="resources.length === 0 && !loadingResources">
              <el-empty description="还没有上传任何资源" />
            </div>
            <div v-else-if="loadingResources" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>
            <div class="resource-list" v-else>
              <el-card v-for="resource in resources" :key="resource.id" class="resource-card" @click="navigateToResource(resource.id)" :class="{ clickable: true }">
                <h3>{{ resource.title }}</h3>
                <p class="resource-description">{{ resource.description }}</p>
                <div class="resource-meta">
                  <span>{{ formatDate(resource.uploadTime) }}</span>
                  <span>{{ resource.fileType }}</span>
                </div>
              </el-card>
              
              <!-- 分页 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="resourcesPage"
                  v-model:page-size="resourcesPageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  :total="resourcesTotal"
                  @size-change="handleResourcesSizeChange"
                  @current-change="handleResourcesPageChange"
                />
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="我的收藏" name="favorites">
            <div class="empty-state" v-if="favorites.length === 0 && !loadingFavorites">
              <el-empty description="还没有收藏任何资源" />
            </div>
            <div v-else-if="loadingFavorites" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>
            <div class="favorite-list" v-else>
              <el-card v-for="resource in favorites" :key="resource.id" class="resource-card" @click="navigateToResource(resource.id)" :class="{ clickable: true }">
                <h3>{{ resource.title }}</h3>
                <p class="resource-description">{{ resource.description }}</p>
                <div class="resource-meta">
                  <span>{{ formatDate(resource.uploadTime) }}</span>
                  <span>{{ resource.fileType }}</span>
                </div>
              </el-card>
              
              <!-- 分页 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="favoritesPage"
                  v-model:page-size="favoritesPageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  :total="favoritesTotal"
                  @size-change="handleFavoritesSizeChange"
                  @current-change="handleFavoritesPageChange"
                />
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="我的关注" name="following">
            <div class="empty-state" v-if="following.length === 0 && !loadingFollowing">
              <el-empty description="还没有关注任何人" />
            </div>
            <div v-else-if="loadingFollowing" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>
            <div class="following-list" v-else>
              <el-card v-for="user in following" :key="user.id" class="user-card" @click="navigateToUserProfile(user.id)" :class="{ clickable: true }">
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
  </app-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { getUserDetail, getUserPosts, getUserFavorites, getUserFollowing, updateUserInfo, uploadAvatar, toggleFollow, getUserResources } from '@/api/user';
import { getTagsByType, updateUserTags } from '@/api/tag';
import type { UserDetail, Post, FavoriteResource, UserDTO, Resource } from '@/types/user';
import type { TagDTO } from '@/types/tag';
import { ElMessage } from 'element-plus';
import { Plus, ArrowLeft } from '@element-plus/icons-vue';
import AppLayout from '@/components/AppLayout.vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const userDetail = ref<UserDetail>({
  id: 0,
  username: '',
  avatar: '',
  bio: '',
  isAdmin: false,
  status: 0,
  registerTime: '',
  lastLoginTime: '',
  tags: [],
  postCount: 0,
  favoriteCount: 0,
  followingCount: 0,
  followerCount: 0,
  isFollowing: false
});

const activeTab = ref('posts');
const editActiveTab = ref('basic');
const posts = ref<Post[]>([]);
const resources = ref<Resource[]>([]);
const favorites = ref<FavoriteResource[]>([]);
const following = ref<UserDTO[]>([]);

// 加载状态
const loadingPosts = ref(false);
const loadingResources = ref(false);
const loadingFavorites = ref(false);
const loadingFollowing = ref(false);

// 分页相关
// 帖子分页
const postsPage = ref(1);
const postsPageSize = ref(10);
const postsTotal = ref(0);

// 资源分页
const resourcesPage = ref(1);
const resourcesPageSize = ref(10);
const resourcesTotal = ref(0);

// 收藏分页
const favoritesPage = ref(1);
const favoritesPageSize = ref(10);
const favoritesTotal = ref(0);

const showEditDialog = ref(false);
const saveLoading = ref(false);
const followLoading = ref(false);
const isFollowing = ref(false);

// 用于比较的原始用户数据
const originalUserData = ref<{
  avatar?: string;
  bio?: string;
  postTagIds?: number[];
  resourceTagIds?: number[];
}>({});

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

// 计算表单是否有变化
const isFormChanged = computed(() => {
  const avatarChanged = editForm.value.avatar !== originalUserData.value.avatar;
  const bioChanged = editForm.value.bio !== originalUserData.value.bio;
  
  // 检查帖子标签是否有变化
  let postTagsChanged = false;
  if (editForm.value.postTagIds?.length !== originalUserData.value.postTagIds?.length) {
    postTagsChanged = true;
  } else {
    const currentPostTags = editForm.value.postTagIds || [];
    const originalPostTags = originalUserData.value.postTagIds || [];
    postTagsChanged = !currentPostTags.every(id => originalPostTags.includes(id)) || 
                      !originalPostTags.every(id => currentPostTags.includes(id));
  }
  
  // 检查资源标签是否有变化
  let resourceTagsChanged = false;
  if (editForm.value.resourceTagIds?.length !== originalUserData.value.resourceTagIds?.length) {
    resourceTagsChanged = true;
  } else {
    const currentResourceTags = editForm.value.resourceTagIds || [];
    const originalResourceTags = originalUserData.value.resourceTagIds || [];
    resourceTagsChanged = !currentResourceTags.every(id => originalResourceTags.includes(id)) || 
                          !originalResourceTags.every(id => currentResourceTags.includes(id));
  }
  
  return avatarChanged || bioChanged || postTagsChanged || resourceTagsChanged;
});

const isCurrentUser = computed(() => {
  return userStore.userInfo.id === Number(route.params.id);
});

// 格式化日期
const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString();
};

// 加载用户详情数据
const loadUserDetail = async () => {
  try {
    const userId = Number(route.params.id);
    const response = await getUserDetail(userId);
    
    if (response.data.code === 200) {
      userDetail.value = response.data.data;
      isFollowing.value = userDetail.value.isFollowing || false;
      
      // 初始化编辑表单和原始数据
      const postTagIds = userDetail.value.tags
        .filter(tag => tag.type === 'post')
        .map(tag => tag.id);
      
      const resourceTagIds = userDetail.value.tags
        .filter(tag => tag.type === 'resource')
        .map(tag => tag.id);
      
      editForm.value = {
        avatar: userDetail.value.avatar,
        bio: userDetail.value.bio,
        postTagIds: [...postTagIds],
        resourceTagIds: [...resourceTagIds]
      };
      
      originalUserData.value = {
        avatar: userDetail.value.avatar,
        bio: userDetail.value.bio,
        postTagIds: [...postTagIds],
        resourceTagIds: [...resourceTagIds]
      };
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '获取用户数据失败');
  }
};

// 加载用户发布的帖子
const loadUserPosts = async () => {
  loadingPosts.value = true;
  try {
    const userId = Number(route.params.id);
    const response = await getUserPosts(userId, postsPage.value, postsPageSize.value);
    
    if (response.data.code === 200) {
      posts.value = response.data.data;
      // 假设后端返回了总数
      postsTotal.value = posts.value.length;
    }
  } catch (error: any) {
    console.error('加载用户帖子失败', error);
    ElMessage.error('加载用户帖子失败');
  } finally {
    loadingPosts.value = false;
  }
};

// 加载用户上传的资源
const loadUserResources = async () => {
  loadingResources.value = true;
  try {
    const userId = Number(route.params.id);
    const response = await getUserResources(userId, resourcesPage.value, resourcesPageSize.value);
    
    if (response.data.code === 200) {
      resources.value = response.data.data;
      // 假设后端返回了总数
      resourcesTotal.value = resources.value.length;
    }
  } catch (error: any) {
    console.error('加载用户资源失败', error);
    ElMessage.error('加载用户资源失败');
  } finally {
    loadingResources.value = false;
  }
};

// 加载用户收藏的资源
const loadUserFavorites = async () => {
  loadingFavorites.value = true;
  try {
    const userId = Number(route.params.id);
    const response = await getUserFavorites(userId, favoritesPage.value, favoritesPageSize.value);
    
    if (response.data.code === 200) {
      favorites.value = response.data.data;
      // 假设后端返回了总数
      favoritesTotal.value = favorites.value.length;
    }
  } catch (error: any) {
    console.error('加载收藏资源失败', error);
    ElMessage.error('加载收藏资源失败');
  } finally {
    loadingFavorites.value = false;
  }
};

// 加载用户关注的用户
const loadUserFollowing = async () => {
  loadingFollowing.value = true;
  try {
    const userId = Number(route.params.id);
    const response = await getUserFollowing(userId);
    
    if (response.data.code === 200) {
      following.value = response.data.data;
    }
  } catch (error: any) {
    console.error('加载关注列表失败', error);
    ElMessage.error('加载关注列表失败');
  } finally {
    loadingFollowing.value = false;
  }
};

// 加载标签列表
const loadTags = async () => {
  try {
    const postTagsResponse = await getTagsByType('post');
    const resourceTagsResponse = await getTagsByType('resource');
    
    if (postTagsResponse.data.code === 200) {
      allTags.value.postTags = postTagsResponse.data.data;
    }
    
    if (resourceTagsResponse.data.code === 200) {
      allTags.value.resourceTags = resourceTagsResponse.data.data;
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '获取标签失败');
  }
};

// 关注/取消关注
async function handleFollow() {
  if (!userDetail.value.id) return;
  
  followLoading.value = true;
  try {
    const response = await toggleFollow(userDetail.value.id);
    if (response.data.code === 200) {
      ElMessage.success(isFollowing.value ? '已取消关注' : '关注成功');
      // 重新加载用户数据
      await loadUserDetail();
    } else {
      ElMessage.error(response.data.message || '操作失败');
    }
  } catch (error) {
    console.error('关注操作失败', error);
    ElMessage.error('操作失败，请稍后重试');
  } finally {
    followLoading.value = false;
  }
}

// 处理头像变更
const selectedAvatarFile = ref<File | null>(null);

// 处理头像选择
const handleAvatarChange = (file: any) => {
  // 检查文件类型和大小
  const isImage = file.raw.type.startsWith('image/');
  const isLt2M = file.raw.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return;
  }
  
  selectedAvatarFile.value = file.raw;
  // 上传头像
  uploadAvatarFile();
};

// 上传头像文件
const uploadAvatarFile = async () => {
  if (!selectedAvatarFile.value) return;
  
  try {
    const response = await uploadAvatar(selectedAvatarFile.value);
    
    if (response.data.code === 200) {
      editForm.value.avatar = response.data.data.url;
      ElMessage.success('头像上传成功');
    } else {
      ElMessage.error(response.data.message || '头像上传失败');
    }
  } catch (error: any) {
    console.error('头像上传失败', error);
    ElMessage.error(error.response?.data?.message || '头像上传失败');
  }
};

// 保存所有修改
const handleSaveProfile = async () => {
  saveLoading.value = true;
  try {
    const userId = Number(route.params.id);
    let successMessage = '保存成功';
    
    // 收集需要更新的数据
    const needUpdateInfo = editForm.value.avatar !== originalUserData.value.avatar || 
                          editForm.value.bio !== originalUserData.value.bio;
    
    const needUpdateTags = !editForm.value.postTagIds?.every(id => originalUserData.value.postTagIds?.includes(id)) || 
                          !originalUserData.value.postTagIds?.every(id => editForm.value.postTagIds?.includes(id)) ||
                          !editForm.value.resourceTagIds?.every(id => originalUserData.value.resourceTagIds?.includes(id)) ||
                          !originalUserData.value.resourceTagIds?.every(id => editForm.value.resourceTagIds?.includes(id));
    
    // 更新用户基本信息
    if (needUpdateInfo) {
      const userInfoResponse = await updateUserInfo(userId, {
        avatar: editForm.value.avatar,
        bio: editForm.value.bio
      });
      
      if (userInfoResponse.data.code !== 200) {
        successMessage = '基本资料更新成功，但标签更新失败';
        ElMessage.error(userInfoResponse.data.message || '基本资料更新失败');
      }
    }
    
    // 更新用户标签
    if (needUpdateTags) {
      const tagsResponse = await updateUserTags(userId, {
        postTagIds: editForm.value.postTagIds,
        resourceTagIds: editForm.value.resourceTagIds
      });
      
      if (tagsResponse.data.code !== 200) {
        successMessage = '标签更新成功，但基本资料更新失败';
        ElMessage.error(tagsResponse.data.message || '标签更新失败');
      }
    }
    
    if ((needUpdateInfo || needUpdateTags) && successMessage === '保存成功') {
      ElMessage.success(successMessage);
      showEditDialog.value = false;
      await loadUserDetail();  // 重新加载用户详情
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '保存失败');
  } finally {
    saveLoading.value = false;
  }
};

// 跳转相关的方法
const navigateToPost = (postId: number) => {
  router.push(`/posts/${postId}`);
}

const navigateToResource = (resourceId: number) => {
  router.push(`/resources/${resourceId}`);
}

const navigateToUserProfile = (userId: number) => {
  // 如果当前已经是这个用户的主页，不需要跳转
  const currentUserId = Number(route.params.id);
  if (currentUserId === userId) {
    return;
  }
  
  // 使用replace而不是push可以避免浏览历史中堆积过多相似页面
  router.replace(`/profile/${userId}`);
}

// 帖子分页处理
const handlePostsSizeChange = (size: number) => {
  postsPageSize.value = size;
  loadUserPosts();
};

const handlePostsPageChange = (page: number) => {
  postsPage.value = page;
  loadUserPosts();
};

// 资源分页处理
const handleResourcesSizeChange = (size: number) => {
  resourcesPageSize.value = size;
  loadUserResources();
};

const handleResourcesPageChange = (page: number) => {
  resourcesPage.value = page;
  loadUserResources();
};

// 收藏分页处理
const handleFavoritesSizeChange = (size: number) => {
  favoritesPageSize.value = size;
  loadUserFavorites();
};

const handleFavoritesPageChange = (page: number) => {
  favoritesPage.value = page;
  loadUserFavorites();
};

// 组件挂载时加载数据
onMounted(async () => {
  try {
    await loadUserDetail();
    if (route.query.tab) {
      activeTab.value = String(route.query.tab);
    }
    
    // 根据活动标签加载对应数据
    if (activeTab.value === 'posts') {
      await loadUserPosts();
    } else if (activeTab.value === 'resources') {
      await loadUserResources();
    } else if (activeTab.value === 'favorites') {
      await loadUserFavorites();
    } else if (activeTab.value === 'following') {
      await loadUserFollowing();
    }
    await loadTags();

  } catch (error) {
    console.error('获取用户数据失败', error);
    ElMessage.error('获取用户数据失败');
  }
});

// 监听路由参数变化，当用户ID变化时重新加载数据
watch(() => route.params.id, async (newId) => {
  if (newId) {
    console.log('用户ID变化，重新加载数据:', newId);
    // 重置数据
    posts.value = [];
    resources.value = [];
    favorites.value = [];
    following.value = [];
    
    // 重置分页
    postsPage.value = 1;
    resourcesPage.value = 1;
    favoritesPage.value = 1;
    
    // 重新加载数据
    try {
      await loadUserDetail();
      
      // 根据当前活动标签加载对应数据
      if (activeTab.value === 'posts') {
        await loadUserPosts();
      } else if (activeTab.value === 'resources') {
        await loadUserResources();
      } else if (activeTab.value === 'favorites') {
        await loadUserFavorites();
      } else if (activeTab.value === 'following') {
        await loadUserFollowing();
      }
    } catch (error) {
      console.error('重新加载用户数据失败', error);
      ElMessage.error('获取用户数据失败');
    }
  }
}, { immediate: false });

// 监听标签变化，加载对应数据
watch(activeTab, async (newTab) => {
  if (newTab === 'posts' && posts.value.length === 0) {
    await loadUserPosts();
  } else if (newTab === 'resources' && resources.value.length === 0) {
    await loadUserResources();
  } else if (newTab === 'favorites' && favorites.value.length === 0) {
    await loadUserFavorites();
  } else if (newTab === 'following' && following.value.length === 0) {
    await loadUserFollowing();
  } 
});

// 监听编辑对话框状态
watch(showEditDialog, (newVal) => {
  if (newVal) {
    // 打开对话框时，重置表单为当前用户信息
    const postTagIds = userDetail.value.tags
      .filter(tag => tag.type === 'post')
      .map(tag => tag.id);
    
    const resourceTagIds = userDetail.value.tags
      .filter(tag => tag.type === 'resource')
      .map(tag => tag.id);
    
    editForm.value = {
      avatar: userDetail.value.avatar,
      bio: userDetail.value.bio,
      postTagIds: [...postTagIds],
      resourceTagIds: [...resourceTagIds]
    };
    
    originalUserData.value = {
      avatar: userDetail.value.avatar,
      bio: userDetail.value.bio,
      postTagIds: [...postTagIds],
      resourceTagIds: [...resourceTagIds]
    };
  }
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

/* 编辑表单样式 */
.edit-tabs {
  width: 100%;
}

.edit-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.edit-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.edit-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.clickable {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.clickable:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.loading-container {
  padding: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 