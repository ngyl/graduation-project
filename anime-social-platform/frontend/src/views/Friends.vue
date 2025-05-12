<template>
  <app-layout>
    <div class="friends-container">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我关注的" name="following">
          <div v-if="followings.length === 0" class="empty-tip">
            暂无关注的用户
          </div>
          <div v-else class="user-list">
            <div v-for="user in followings" :key="user.id" class="user-card">
              <div class="user-avatar" @click="goToProfile(user.id)">
                <img :src="user.avatar || '/default-avatar.jpg'" alt="用户头像">
              </div>
              <div class="user-info">
                <div class="username" @click="goToProfile(user.id)">{{ user.username }}</div>
                <div class="bio">{{ user.bio || '这个人很懒，什么都没留下' }}</div>
              </div>
              <div class="action-btn">
                <el-button type="danger" size="small" @click="unfollow(user.id)">取消关注</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="关注我的" name="followers">
          <div v-if="followers.length === 0" class="empty-tip">
            暂无粉丝
          </div>
          <div v-else class="user-list">
            <div v-for="user in followers" :key="user.id" class="user-card">
              <div class="user-avatar" @click="goToProfile(user.id)">
                <img :src="user.avatar || '/default-avatar.jpg'" alt="用户头像">
              </div>
              <div class="user-info">
                <div class="username" @click="goToProfile(user.id)">{{ user.username }}</div>
                <div class="bio">{{ user.bio || '这个人很懒，什么都没留下' }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="互相关注" name="mutual">
          <div v-if="mutualFriends.length === 0" class="empty-tip">
            暂无互相关注的好友
          </div>
          <div v-else class="user-list">
            <div v-for="user in mutualFriends" :key="user.id" class="user-card">
              <div class="user-avatar" @click="goToProfile(user.id)">
                <img :src="user.avatar || '/default-avatar.jpg'" alt="用户头像">
              </div>
              <div class="user-info">
                <div class="username" @click="goToProfile(user.id)">{{ user.username }}</div>
                <div class="bio">{{ user.bio || '这个人很懒，什么都没留下' }}</div>
              </div>
              <div class="action-btn">
                <el-button type="success" size="small" @click="sendMessage(user.id)">发消息</el-button>
                <el-button type="danger" size="small" @click="unfollow(user.id)">取消关注</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="推荐用户" name="recommended">
          <div v-if="recommendedUsers.length === 0" class="empty-tip">
            暂无推荐用户
          </div>
          <div v-else class="user-list">
            <div v-for="user in recommendedUsers" :key="user.id" class="user-card">
              <div class="user-avatar" @click="goToProfile(user.id)">
                <img :src="user.avatar || '/default-avatar.jpg'" alt="用户头像">
              </div>
              <div class="user-info">
                <div class="username" @click="goToProfile(user.id)">{{ user.username }}</div>
                <div class="bio">{{ user.bio || '这个人很懒，什么都没留下' }}</div>
              </div>
              <div class="action-btn">
                <el-button 
                  :type="followingIds.has(user.id) ? 'danger' : 'primary'" 
                  size="small" 
                  @click="followingIds.has(user.id) ? unfollow(user.id) : follow(user.id)"
                >
                  {{ followingIds.has(user.id) ? '取消关注' : '关注' }}
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { getUserFollowing, getUserFollowers, getUserMutual, toggleFollow, getRecommendedUsers } from '@/api/user';
import type { UserDTO } from '@/types/user';  
import AppLayout from '@/components/AppLayout.vue';


const router = useRouter();
const userStore = useUserStore();
const userId = userStore.userInfo?.id;

const activeTab = ref('following');
const followings = ref<UserDTO[]>([]);
const followers = ref<UserDTO[]>([]);
const mutualFriends = ref<UserDTO[]>([]);
const recommendedUsers = ref<UserDTO[]>([]);
const followingIds = ref<Set<number>>(new Set());

// 页面加载时获取数据
onMounted(async () => {
  if (!userId) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }
  
  await loadData();
});

// 加载数据
async function loadData() {
  try {
    // 获取关注列表
    const followingRes = await getUserFollowing(userId as number);
    if (followingRes.data.code === 200) {
      followings.value = followingRes.data.data;
      // 更新关注ID集合
      followingIds.value = new Set(followings.value.map(user => user.id));
    }
    
    // 获取粉丝列表
    const followerRes = await getUserFollowers(userId as number);
    if (followerRes.data.code === 200) {
      followers.value = followerRes.data.data;
    }
    
    // 获取互相关注列表
    const mutualRes = await getUserMutual(userId as number);
    if (mutualRes.data.code === 200) {
      mutualFriends.value = mutualRes.data.data;
    }
    
    // 获取推荐用户列表
    const recommendedRes = await getRecommendedUsers(10);
    if (recommendedRes.data.code === 200) {
      recommendedUsers.value = recommendedRes.data.data;
    }
  } catch (error) {
    console.error('加载数据失败', error);
    ElMessage.error('加载数据失败');
  }
}

// 统一操作关注状态的函数
async function toggleFollowStatus(friendId: number, isUnfollow: boolean) {
  try {
    const response = await toggleFollow(friendId);
    if (response.data.code === 200) {
      ElMessage.success(isUnfollow ? '已取消关注' : '关注成功');
      await loadData(); // 重新加载数据
    } else {
      ElMessage.error(response.data.message || (isUnfollow ? '取消关注失败' : '关注失败'));
    }
  } catch (error) {
    console.error(isUnfollow ? '取消关注失败' : '关注失败', error);
    ElMessage.error(isUnfollow ? '取消关注失败' : '关注失败');
  }
}

// 取消关注
async function unfollow(friendId: number) {
  await toggleFollowStatus(friendId, true);
}

// 关注
async function follow(friendId: number) {
  await toggleFollowStatus(friendId, false);
}

// 跳转到用户主页
function goToProfile(id: number) {
  router.push(`/profile/${id}`);
}

// 发送私信
function sendMessage(id: number) {
  router.push(`/messages?userId=${id}`);
}
</script>

<style scoped>
.friends-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.empty-tip {
  text-align: center;
  color: var(--gray-500);
  padding: 30px 0;
}

.user-list {
  margin-top: 20px;
}

.user-card {
  display: flex;
  align-items: center;
  padding: 15px;
  margin-bottom: 15px;
  border-radius: var(--border-radius-md);
  background-color: var(--bg-primary);
  box-shadow: var(--shadow-md);
  transition: var(--transition-base);
}

.user-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
  margin-left: 15px;
}

.username {
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  color: var(--primary-color);
}

.bio {
  margin-top: 5px;
  font-size: 14px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}

.action-btn {
  display: flex;
  gap: 10px;
}

/* 自定义Element Plus组件样式 */
:deep(.el-button--primary) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  transition: var(--transition-base);
}

:deep(.el-button--primary:hover),
:deep(.el-button--primary:focus) {
  background-color: var(--primary-light);
  border-color: var(--primary-light);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

:deep(.el-button--danger) {
  background-color: rgba(var(--error-color-rgb), 0.1);
  color: var(--error-color);
  border-color: var(--error-color);
}

:deep(.el-button--danger:hover),
:deep(.el-button--danger:focus) {
  background-color: var(--error-color);
  color: white;
  border-color: var(--error-color);
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(var(--error-color-rgb), 0.3);
}

:deep(.el-button--success) {
  background-color: rgba(var(--success-color-rgb), 0.1);
  color: var(--success-color);
  border-color: var(--success-color);
}

:deep(.el-button--success:hover),
:deep(.el-button--success:focus) {
  background-color: var(--success-color);
  color: white;
  border-color: var(--success-color);
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(var(--success-color-rgb), 0.3);
}

:deep(.el-tabs__item) {
  color: var(--text-secondary);
}

:deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

:deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
}
</style> 