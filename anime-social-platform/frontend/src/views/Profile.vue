<template>
  <div class="profile-container">
    <!-- 用户信息卡片 -->
    <el-card class="user-info-card">
      <div class="user-header">
        <el-avatar :size="100" :src="userInfo.avatar || '/default-avatar.png'" />
        <div class="user-details">
          <h2>{{ userInfo.username }}</h2>
          <p class="bio">{{ userInfo.bio || '这个人很懒，什么都没写~' }}</p>
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
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { getUserDetail, getUserPosts, getUserFavorites, getUserFollowing } from '@/api/user';
import type { UserStats, Post, FavoriteResource, FollowingUser } from '@/types/user';
import type { UserInfo } from '@/types/auth';
import { ElMessage } from 'element-plus';

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
  followingCount: 0
});

const activeTab = ref('posts');
const posts = ref<Post[]>([]);
const favorites = ref<FavoriteResource[]>([]);
const following = ref<FollowingUser[]>([]);

// 格式化日期
const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString();
};

// 加载用户数据
const loadUserData = async () => {
  try {
    const userId = Number(route.params.id);
    const [detailRes, postsRes, favoritesRes, followingRes] = await Promise.all([
      getUserDetail(userId),
      getUserPosts(userId),
      getUserFavorites(userId),
      getUserFollowing(userId)
    ]);

    if (detailRes.data.code === 200) {
      userInfo.value = detailRes.data.data.user;
      stats.value = detailRes.data.data.stats;
    }

    if (postsRes.data.code === 200) {
      posts.value = postsRes.data.data;
    }

    if (favoritesRes.data.code === 200) {
      favorites.value = favoritesRes.data.data;
    }

    if (followingRes.data.code === 200) {
      following.value = followingRes.data.data;
    }
  } catch (error) {
    ElMessage.error('加载用户数据失败');
    console.error('Error loading user data:', error);
  }
};

onMounted(loadUserData);
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
</style> 