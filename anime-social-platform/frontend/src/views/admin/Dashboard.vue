<template>
  <admin-layout>
    <div class="admin-dashboard">
      <h1>管理员控制台</h1>
      
      <el-row v-loading="loading" :gutter="15">
        <!-- 统计卡片 -->
        <el-col :span="4.8" :xs="24" :sm="12" :md="12" :lg="4.8" class="stat-col">
          <el-card class="stat-card user-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>用户数量</span>
                <el-icon class="header-icon"><user /></el-icon>
              </div>
            </template>
            <div class="stat-content">
              <div class="stat-icon-bg">
                <el-icon><user /></el-icon>
              </div>
              <div class="stat-number">
                {{ statistics.userCount || 0 }}
              </div>
              <div class="stat-trend">
                <el-icon color="#409EFF"><arrow-up /></el-icon>
                <span>+5%</span>
              </div>
              <div class="stat-action">
                <el-button type="primary" size="small" @click="$router.push('/admin/users')">
                  管理用户
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="4.8" :xs="24" :sm="12" :md="12" :lg="4.8" class="stat-col">
          <el-card class="stat-card post-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>帖子数量</span>
                <el-icon class="header-icon"><document /></el-icon>
              </div>
            </template>
            <div class="stat-content">
              <div class="stat-icon-bg">
                <el-icon><document /></el-icon>
              </div>
              <div class="stat-number">
                {{ statistics.postCount || 0 }}
              </div>
              <div class="stat-trend">
                <el-icon color="#67C23A"><arrow-up /></el-icon>
                <span>+8%</span>
              </div>
              <div class="stat-action">
                <el-button type="primary" size="small" @click="$router.push('/admin/posts')">
                  管理帖子
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="4.8" :xs="24" :sm="12" :md="12" :lg="4.8" class="stat-col">
          <el-card class="stat-card resource-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>资源数量</span>
                <el-icon class="header-icon"><folder-opened /></el-icon>
              </div>
            </template>
            <div class="stat-content">
              <div class="stat-icon-bg">
                <el-icon><folder-opened /></el-icon>
              </div>
              <div class="stat-number">
                {{ statistics.resourceCount || 0 }}
              </div>
              <div class="stat-trend">
                <el-icon color="#E6A23C"><arrow-up /></el-icon>
                <span>+3%</span>
              </div>
              <div class="stat-action">
                <el-button type="primary" size="small" @click="$router.push('/admin/resources')">
                  管理资源
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="4.8" :xs="24" :sm="12" :md="12" :lg="4.8" class="stat-col">
          <el-card class="stat-card event-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>活动数量</span>
                <el-icon class="header-icon"><calendar /></el-icon>
              </div>
            </template>
            <div class="stat-content">
              <div class="stat-icon-bg">
                <el-icon><calendar /></el-icon>
              </div>
              <div class="stat-number">
                {{ statistics.eventCount || 0 }}
              </div>
              <div class="stat-trend">
                <el-icon color="#F56C6C"><arrow-up /></el-icon>
                <span>+12%</span>
              </div>
              <div class="stat-action">
                <el-button type="primary" size="small" @click="$router.push('/admin/events')">
                  管理活动
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 标签统计卡片 -->
        <el-col :span="4.8" :xs="24" :sm="12" :md="12" :lg="4.8" class="stat-col">
          <el-card class="stat-card tag-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>标签数量</span>
                <el-icon class="header-icon"><price-tag /></el-icon>
              </div>
            </template>
            <div class="stat-content">
              <div class="stat-icon-bg">
                <el-icon><price-tag /></el-icon>
              </div>
              <div class="stat-number">
                {{ statistics.tagCount || 0 }}
              </div>
              <div class="stat-trend">
                <el-icon color="#9B59B6"><arrow-up /></el-icon>
                <span>+7%</span>
              </div>
              <div class="stat-action">
                <el-button type="primary" size="small" @click="$router.push('/admin/tags')">
                  管理标签
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 最新用户 -->
      <el-row class="dashboard-section" v-loading="loading">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最新注册用户</span>
              </div>
            </template>
            <el-table :data="statistics.latestUsers || []" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="registerTime" label="注册时间" width="180" />
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button 
                    size="small" 
                    @click="$router.push(`/profile/${scope.row.id}`)"
                  >
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 置顶帖子 -->
      <el-row class="dashboard-section" v-loading="loading">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>当前置顶帖子</span>
              </div>
            </template>
            <el-table :data="statistics.topPosts || []" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" min-width="200" />
              <el-table-column prop="authorName" label="作者" width="120" />
              <el-table-column prop="viewCount" label="浏览量" width="100" />
              <el-table-column prop="likeCount" label="点赞数" width="100" />
              <el-table-column prop="createdAt" label="创建时间" width="180" />
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button 
                    size="small"
                    type="danger"
                    @click="handleCancelTop(scope.row.id)"
                  >
                    取消置顶
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 当前活动 -->
      <el-row class="dashboard-section" v-loading="loading">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>当前进行中的活动</span>
              </div>
            </template>
            <el-table :data="statistics.currentEvents || []" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" min-width="200" />
              <el-table-column prop="createdByUsername" label="创建者" width="120" />
              <el-table-column prop="startTime" label="开始时间" width="180" />
              <el-table-column prop="endTime" label="结束时间" width="180" />
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                    {{ scope.row.status === 1 ? '进行中' : '已下线' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="scope">
                  <el-button-group>
                    <el-button 
                      size="small" 
                      type="primary"
                      @click="$router.push(`/admin/events?id=${scope.row.id}`)"
                    >
                      编辑
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger"
                      @click="handleEventOffline(scope.row.id)"
                      v-if="scope.row.status === 1"
                    >
                      下线
                    </el-button>
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <!-- 热门标签 -->
      <el-row class="dashboard-section" v-loading="loading">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>热门标签</span>
              </div>
            </template>
            <div v-if="statistics.hotTags && statistics.hotTags.length > 0" class="tag-cloud">
              <el-tag
                v-for="tag in statistics.hotTags"
                :key="tag.id"
                :type="getTagType(tag.type)"
                effect="light"
                class="tag-item"
                size="large"
              >
                {{ tag.name }}
                <span class="tag-count">({{ tag.contentCount || 0 }})</span>
              </el-tag>
            </div>
            <el-empty v-else description="暂无热门标签" />
            <div class="tag-action">
              <el-button type="primary" size="small" @click="$router.push('/admin/tags')">
                管理标签
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import * as adminApi from '@/api/admin';
import { User, Document, FolderOpened, Calendar, PriceTag, ArrowUp } from '@element-plus/icons-vue';
import AdminLayout from '@/components/AdminLayout.vue';
import { StatisticsData } from '@/types/admin';

const loading = ref(false);
const statistics = ref<Partial<StatisticsData>>({});

// 获取系统统计信息
const fetchStatistics = async () => {
  loading.value = true;
  try {
    const response = await adminApi.getDashboardStatistics();
    if (response.data && response.data.code === 200) {
      console.log('统计数据:', response.data.data);
      statistics.value = response.data.data;
    } else {
      ElMessage.error((response.data && response.data.message) ?? '获取统计信息失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message ?? '获取统计信息失败');
  } finally {
    loading.value = false;
  }
};

// 根据标签类型获取标签样式
const getTagType = (type: string): '' | 'success' | 'warning' | 'danger' | 'info' => {
  switch (type) {
    case 'post':
      return 'success';
    case 'resource':
      return 'warning';
    default:
      return '';
  }
};

// 取消置顶帖子
const handleCancelTop = async (postId: number) => {
  try {
    // 根据AdminController.java，接口需要的是整数0，而不是布尔值false
    const response = await adminApi.updatePostTopStatus(postId, 0);
    if (response.data && response.data.code === 200) {
      ElMessage.success('已取消置顶');
      fetchStatistics(); // 刷新数据
    } else {
      ElMessage.error((response.data && response.data.message) ?? '操作失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message ?? '操作失败');
  }
};

// 下线活动
const handleEventOffline = async (eventId: number) => {
  try {
    const response = await adminApi.updateEventStatus(eventId, 0);
    if (response.data && response.data.code === 200) {
      ElMessage.success('活动已下线');
      fetchStatistics(); // 刷新数据
    } else {
      ElMessage.error((response.data && response.data.message) ?? '操作失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message ?? '操作失败');
  }
};

onMounted(() => {
  fetchStatistics();
});
</script>

<style scoped>
.admin-dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 使用flex布局确保卡片平分一行 */
.el-row {
  display: flex;
  flex-wrap: wrap;
  margin-right: -7.5px;
  margin-left: -7.5px;
}

.stat-col {
  margin-bottom: 25px;
  flex: 1;
  padding: 0 7.5px;
  min-width: 200px;
}

@media (max-width: 1200px) {
  .stat-col {
    flex: 0 0 calc(50% - 15px);
    max-width: calc(50% - 15px);
  }
}

@media (max-width: 768px) {
  .stat-col {
    flex: 0 0 100%;
    max-width: 100%;
  }
}

.stat-card {
  height: 200px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.05);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.header-icon {
  font-size: 20px;
}

.stat-content {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 15px 15px;
}

.stat-icon-bg {
  position: absolute;
  right: -20px;
  bottom: -20px;
  opacity: 0.07;
  font-size: 120px;
  z-index: 0;
  transform: rotate(-10deg);
  transition: all 0.5s ease;
}

.stat-card:hover .stat-icon-bg {
  transform: rotate(0deg) scale(1.1);
  opacity: 0.12;
}

.stat-number {
  font-size: 42px;
  font-weight: bold;
  text-align: center;
  margin: 5px 0;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-number {
  transform: scale(1.1);
}

.stat-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  font-size: 14px;
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.6);
  padding: 2px 8px;
  border-radius: 12px;
}

.stat-trend span {
  margin-left: 5px;
  font-weight: 600;
}

.stat-action {
  text-align: center;
  margin-top: 15px;
  position: relative;
  z-index: 1;
}

.stat-action .el-button {
  transition: all 0.3s ease;
  border-radius: 20px;
  padding: 8px 16px;
}

.stat-card:hover .stat-action .el-button {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

/* 卡片颜色方案 */
.user-card {
  background: linear-gradient(135deg, #ffffff 0%, #e8f4ff 100%);
  border-top: 3px solid #409EFF;
}
.user-card .stat-number {
  color: #409EFF;
}
.user-card .stat-trend span {
  color: #409EFF;
}
.user-card .stat-action .el-button {
  background-color: #409EFF;
  border-color: #409EFF;
}

.post-card {
  background: linear-gradient(135deg, #ffffff 0%, #e6ffef 100%);
  border-top: 3px solid #67C23A;
}
.post-card .stat-number {
  color: #67C23A;
}
.post-card .stat-trend span {
  color: #67C23A;
}
.post-card .stat-action .el-button {
  background-color: #67C23A;
  border-color: #67C23A;
}

.resource-card {
  background: linear-gradient(135deg, #ffffff 0%, #fff8e6 100%);
  border-top: 3px solid #E6A23C;
}
.resource-card .stat-number {
  color: #E6A23C;
}
.resource-card .stat-trend span {
  color: #E6A23C;
}
.resource-card .stat-action .el-button {
  background-color: #E6A23C;
  border-color: #E6A23C;
}

.event-card {
  background: linear-gradient(135deg, #ffffff 0%, #ffedf1 100%);
  border-top: 3px solid #F56C6C;
}
.event-card .stat-number {
  color: #F56C6C;
}
.event-card .stat-trend span {
  color: #F56C6C;
}
.event-card .stat-action .el-button {
  background-color: #F56C6C;
  border-color: #F56C6C;
}

.tag-card {
  background: linear-gradient(135deg, #ffffff 0%, #f0e6ff 100%);
  border-top: 3px solid #9B59B6;
}
.tag-card .stat-number {
  color: #9B59B6;
}
.tag-card .stat-trend span {
  color: #9B59B6;
}
.tag-card .stat-action .el-button {
  background-color: #9B59B6;
  border-color: #9B59B6;
}

.dashboard-section {
  margin-top: 30px;
}

h1 {
  margin-bottom: 30px;
  color: #303133;
  border-bottom: 1px solid #EBEEF5;
  padding-bottom: 15px;
  font-size: 24px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.tag-item {
  margin-bottom: 5px;
  font-size: 14px;
}

.tag-count {
  margin-left: 4px;
  font-size: 12px;
  opacity: 0.8;
}

.tag-action {
  text-align: center;
  margin-top: 15px;
}
</style> 