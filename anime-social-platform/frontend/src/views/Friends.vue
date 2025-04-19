<template>
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
            <div class="action-btn">
              <el-button 
                v-if="isFollowing(user.id)" 
                type="danger" 
                size="small" 
                @click="unfollow(user.id)"
              >取消关注</el-button>
              <el-button 
                v-else 
                type="primary" 
                size="small" 
                @click="follow(user.id)"
              >关注</el-button>
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
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { getFollowings, getFollowers, getMutualFriends, followUser, unfollowUser } from '@/api/friendship';
import type { FollowingUser } from '@/types/user';

// 定义用户接口，扩展FollowingUser
interface User extends FollowingUser {}

const router = useRouter();
const userStore = useUserStore();
const userId = userStore.userInfo?.id;

const activeTab = ref('following');
const followings = ref<User[]>([]);
const followers = ref<User[]>([]);
const mutualFriends = ref<User[]>([]);
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
    const followingRes = await getFollowings(userId as number);
    // 使用any类型断言绕过类型检查
    const followingData = (followingRes as any);
    if (followingData.code === 200) {
      followings.value = followingData.data;
    }
    
    // 更新关注ID集合
    followingIds.value = new Set(followings.value.map(user => user.id));
    
    // 获取粉丝列表
    const followerRes = await getFollowers(userId as number);
    // 使用any类型断言绕过类型检查
    const followerData = (followerRes as any);
    if (followerData.code === 200) {
      followers.value = followerData.data;
    }
    
    // 获取互相关注列表
    const mutualRes = await getMutualFriends(userId as number);
    // 使用any类型断言绕过类型检查
    const mutualData = (mutualRes as any);
    if (mutualData.code === 200) {
      mutualFriends.value = mutualData.data;
    }
  } catch (error) {
    console.error('加载数据失败', error);
    ElMessage.error('加载数据失败');
  }
}

// 判断是否已关注
function isFollowing(id: number): boolean {
  return followingIds.value.has(id);
}

// 关注用户
async function follow(friendId: number) {
  try {
    await followUser(friendId);
    ElMessage.success('关注成功');
    await loadData(); // 重新加载数据
  } catch (error) {
    console.error('关注失败', error);
    ElMessage.error('关注失败');
  }
}

// 取消关注
async function unfollow(friendId: number) {
  try {
    await unfollowUser(friendId);
    ElMessage.success('已取消关注');
    await loadData(); // 重新加载数据
  } catch (error) {
    console.error('取消关注失败', error);
    ElMessage.error('取消关注失败');
  }
}

// 跳转到用户主页
function goToProfile(id: number) {
  router.push(`/profile/${id}`);
}

// 发送私信
function sendMessage(id: number) {
  router.push(`/messages/${id}`);
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
  color: #999;
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
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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
  color: #409EFF;
}

.bio {
  margin-top: 5px;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}

.action-btn {
  display: flex;
  gap: 10px;
}
</style> 