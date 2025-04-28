<template>
  <app-layout>
    <div class="events-container">
      <div class="page-header">
        <h1>活动中心</h1>
        <p class="page-description">了解最新动漫相关活动，不错过任何精彩</p>
      </div>
      
      <div class="events-content">
        <!-- 我参与的活动 -->
        <div class="events-section" v-if="isLoggedIn">
          <div class="section-header">
            <h2>我参与的活动</h2>
            <div class="line"></div>
          </div>
          
          <el-row :gutter="20" v-loading="loadingMyEvents">
            <template v-if="myEvents.length > 0">
              <el-col :xs="24" :sm="12" :md="8" v-for="event in myEvents" :key="event.id || 0">
                <el-card class="event-card" shadow="hover" @click="handleViewEvent(event)">
                  <div class="event-status">
                    <el-tag :type="event.isOngoing ? 'success' : 'warning'" effect="dark">
                      {{ event.isOngoing ? '进行中' : '即将开始' }}
                    </el-tag>
                    <span v-if="!event.isOngoing" class="start-in">{{ getStartTimeText(event.startTime) }}</span>
                  </div>
                  <h3 class="event-title">{{ event.title }}</h3>
                  <div class="event-time">
                    <el-icon><Calendar /></el-icon>
                    <div class="time-info">
                      <div>开始: {{ formatDate(event.startTime) }}</div>
                      <div>结束: {{ formatDate(event.endTime) }}</div>
                    </div>
                  </div>
                  <div class="event-desc">{{ truncateText(event.description, 100) }}</div>
                </el-card>
              </el-col>
            </template>
            <el-col :span="24" v-else>
              <el-empty description="您还没有参与任何活动" />
            </el-col>
          </el-row>
        </div>
        
        <!-- 当前进行中的活动 -->
        <div class="events-section">
          <div class="section-header">
            <h2>进行中的活动</h2>
            <div class="line"></div>
          </div>
          
          <el-row :gutter="20" v-loading="loadingCurrent">
            <template v-if="currentEvents.length > 0">
              <el-col :xs="24" :sm="12" :md="8" v-for="event in currentEvents" :key="event.id || 0">
                <el-card class="event-card" shadow="hover" @click="handleViewEvent(event)">
                  <div class="event-status">
                    <el-tag type="success" effect="dark">进行中</el-tag>
                  </div>
                  <h3 class="event-title">{{ event.title }}</h3>
                  <div class="event-time">
                    <el-icon><Calendar /></el-icon>
                    <div class="time-info">
                      <div>开始: {{ formatDate(event.startTime) }}</div>
                      <div>结束: {{ formatDate(event.endTime) }}</div>
                    </div>
                  </div>
                  <div class="event-desc">{{ truncateText(event.description, 100) }}</div>
                </el-card>
              </el-col>
            </template>
            <el-col :span="24" v-else>
              <el-empty description="当前没有进行中的活动" />
            </el-col>
          </el-row>
        </div>
        
        <!-- 即将开始的活动 -->
        <div class="events-section">
          <div class="section-header">
            <h2>即将开始的活动</h2>
            <div class="line"></div>
          </div>
          
          <el-row :gutter="20" v-loading="loadingUpcoming">
            <template v-if="upcomingEvents.length > 0">
              <el-col :xs="24" :sm="12" :md="8" v-for="event in upcomingEvents" :key="event.id || 0">
                <el-card class="event-card" shadow="hover" @click="handleViewEvent(event)">
                  <div class="event-status">
                    <el-tag type="warning" effect="dark">即将开始</el-tag>
                    <span class="start-in">{{ getStartTimeText(event.startTime) }}</span>
                  </div>
                  <h3 class="event-title">{{ event.title }}</h3>
                  <div class="event-time">
                    <el-icon><Calendar /></el-icon>
                    <div class="time-info">
                      <div>开始: {{ formatDate(event.startTime) }}</div>
                      <div>结束: {{ formatDate(event.endTime) }}</div>
                    </div>
                  </div>
                  <div class="event-desc">{{ truncateText(event.description, 100) }}</div>
                </el-card>
              </el-col>
            </template>
            <el-col :span="24" v-else>
              <el-empty description="当前没有即将开始的活动" />
            </el-col>
          </el-row>
        </div>
      </div>
      
      <!-- 活动详情对话框 -->
      <el-dialog
        v-model="eventDetailVisible"
        :title="currentEvent?.title || '活动详情'"
        width="70%"
        class="event-detail-dialog"
      >
        <div v-if="currentEvent" class="event-detail">
          <div class="event-detail-header">
            <div class="event-status-tag">
              <el-tag :type="currentEvent.isOngoing ? 'success' : 'warning'" effect="dark">
                {{ currentEvent.isOngoing ? '进行中' : '即将开始' }}
              </el-tag>
              <span v-if="!currentEvent.isOngoing" class="start-in">
                {{ getStartTimeText(currentEvent.startTime) }}
              </span>
            </div>
            <div class="event-time-info">
              <div><strong>开始时间：</strong>{{ formatDate(currentEvent.startTime, true) }}</div>
              <div><strong>结束时间：</strong>{{ formatDate(currentEvent.endTime, true) }}</div>
            </div>
          </div>
          
          <div class="creator-info">
            <strong>创建者：</strong>{{ currentEvent.createdByUsername || currentEvent.creatorName || '未知' }}
          </div>
          
          <div class="event-detail-content">
            <h3>活动描述</h3>
            <div class="description" v-html="formatDescription(currentEvent.description)"></div>
          </div>
          
          <div class="event-actions" v-if="isLoggedIn">
            <el-button 
              type="primary" 
              @click="handleParticipate" 
              :disabled="isParticipated"
            >
              {{ isParticipated ? '已报名' : '立即报名' }}
            </el-button>
          </div>
        </div>
      </el-dialog>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Calendar } from '@element-plus/icons-vue';
import { formatDistance, format } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { useUserStore } from '@/stores/user';
import AppLayout from '@/components/AppLayout.vue';
import * as eventApi from '../api/event';
import type { Event } from '@/types/event';

// 扩展Event接口包含isOngoing字段
interface EventWithStatus extends Event {
  isOngoing: boolean;
}

const userStore = useUserStore();
const isLoggedIn = computed(() => userStore.isLoggedIn);

const currentEvents = ref<EventWithStatus[]>([]);
const upcomingEvents = ref<EventWithStatus[]>([]);
const myEvents = ref<EventWithStatus[]>([]);
const loadingCurrent = ref(false);
const loadingUpcoming = ref(false);
const loadingMyEvents = ref(false);

// 活动详情
const eventDetailVisible = ref(false);
const currentEvent = ref<EventWithStatus | null>(null);
const isParticipated = ref(false);

// 获取我参与的活动
const fetchMyEvents = async () => {
  if (!isLoggedIn.value) return;
  
  loadingMyEvents.value = true;
  try {
    const response = await eventApi.getUserParticipations();
    if (response.data && response.data.code === 200) {
      // 处理活动状态
      const now = new Date();
      
      myEvents.value = response.data.data.map((participation: any) => {
        const startTime = new Date(participation.eventStartTime);
        const endTime = new Date(participation.eventEndTime);
        const isOngoing = now >= startTime && now <= endTime;
        
        return {
          id: participation.eventId,
          title: participation.eventTitle,
          startTime: participation.eventStartTime,
          endTime: participation.eventEndTime,
          description: participation.eventDescription || '暂无描述',
          status: participation.status,
          createdBy: participation.eventCreatedBy,
          creatorName: participation.eventCreatorName,
          isOngoing
        };
      });
    } else {
      ElMessage.error((response.data && response.data.message) ?? '获取已参与的活动失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取已参与的活动失败');
  } finally {
    loadingMyEvents.value = false;
  }
};

// 获取当前进行中的活动
const fetchCurrentEvents = async () => {
  loadingCurrent.value = true;
  try {
    const response = await eventApi.getCurrentEvents();
    if (response.data && response.data.code === 200) {
      // 设置进行中活动的isOngoing标志
      currentEvents.value = response.data.data.map((event: Event) => ({
        ...event,
        isOngoing: true
      }));
    } else {
      ElMessage.error((response.data && response.data.message) ?? '获取进行中的活动失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取进行中的活动失败');
  } finally {
    loadingCurrent.value = false;
  }
};

// 获取即将开始的活动
const fetchUpcomingEvents = async () => {
  loadingUpcoming.value = true;
  try {
    const response = await eventApi.getUpcomingEvents();
    if (response.data && response.data.code === 200) {
      // 设置即将开始活动的isOngoing标志
      upcomingEvents.value = response.data.data.map((event: Event) => ({
        ...event,
        isOngoing: false
      }));
    } else {
      ElMessage.error((response.data && response.data.message) ?? '获取即将开始的活动失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取即将开始的活动失败');
  } finally {
    loadingUpcoming.value = false;
  }
};

// 处理用户报名参加活动
const handleParticipate = async () => {
  if (!currentEvent.value || !currentEvent.value.id) return;
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录');
    return;
  }
  
  try {
    const response = await eventApi.participateEvent(currentEvent.value.id);
    if (response.data && response.data.code === 200) {
      isParticipated.value = true;
      ElMessage.success('报名成功');
      
      // 重新获取我参与的活动
      fetchMyEvents();
    } else {
      ElMessage.error((response.data && response.data.message) ?? '报名失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '报名失败');
  }
};

// 检查用户是否已报名活动
const checkEventParticipation = async (eventId: number) => {
  if (!isLoggedIn.value) return false;
  
  try {
    const response = await eventApi.checkParticipation(eventId);
    if (response.data && response.data.code === 200) {
      return response.data.data;
    }
    return false;
  } catch (error) {
    console.error('检查活动参与状态失败:', error);
    return false;
  }
};

// 格式化日期
const formatDate = (dateStr: string, showTime = false) => {
  const date = new Date(dateStr);
  return showTime 
    ? format(date, 'yyyy年MM月dd日 HH:mm', { locale: zhCN })
    : format(date, 'yyyy年MM月dd日', { locale: zhCN });
};

// 获取开始时间文本（相对时间）
const getStartTimeText = (dateStr: string) => {
  const startDate = new Date(dateStr);
  return formatDistance(startDate, new Date(), { 
    addSuffix: true,
    locale: zhCN
  });
};

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

// 格式化描述文本（将换行符转换为<br>）
const formatDescription = (description: string) => {
  if (!description) return '';
  return description.replace(/\n/g, '<br>');
};

// 处理查看活动详情
const handleViewEvent = async (event: EventWithStatus) => {
  currentEvent.value = event;
  eventDetailVisible.value = true;
  
  // 检查用户是否已参与该活动
  if (isLoggedIn.value && event.id) {
    isParticipated.value = await checkEventParticipation(event.id);
  }
};

onMounted(() => {
  fetchCurrentEvents();
  fetchUpcomingEvents();
  
  if (isLoggedIn.value) {
    fetchMyEvents();
  }
});

// 监听登录状态变化
userStore.$subscribe(() => {
  if (isLoggedIn.value) {
    fetchMyEvents();
  } else {
    myEvents.value = [];
  }
});
</script>

<style scoped>
.events-container {
  max-width: var(--container-width);
  margin: 0 auto;
  padding: var(--spacing-4);
}

.page-header {
  text-align: center;
  margin-bottom: var(--spacing-8);
}

.page-header h1 {
  font-size: var(--font-size-3xl);
  color: var(--text-primary);
  margin-bottom: var(--spacing-2);
}

.page-description {
  color: var(--text-secondary);
  font-size: var(--font-size-md);
}

.events-section {
  margin-bottom: var(--spacing-12);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-6);
}

.section-header h2 {
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  margin: 0;
  margin-right: var(--spacing-4);
  white-space: nowrap;
}

.section-header .line {
  flex-grow: 1;
  height: 1px;
  background-color: var(--gray-200);
}

.event-card {
  height: 100%;
  margin-bottom: var(--spacing-4);
  cursor: pointer;
  transition: var(--transition-base);
  border-radius: var(--border-radius-lg) !important;
  overflow: hidden;
}

.event-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg) !important;
}

.event-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4);
}

.start-in {
  font-size: var(--font-size-sm);
  color: var(--text-hint);
}

.event-title {
  font-size: var(--font-size-lg);
  margin-top: 0;
  margin-bottom: var(--spacing-4);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.event-time {
  display: flex;
  align-items: flex-start;
  margin-bottom: var(--spacing-4);
  color: var(--text-secondary);
}

.event-time .el-icon {
  margin-right: var(--spacing-2);
  margin-top: 3px;
}

.time-info {
  font-size: var(--font-size-sm);
}

.event-desc {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

/* 活动详情对话框样式 */
.event-detail {
  padding: var(--spacing-2);
}

.event-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-6);
  padding-bottom: var(--spacing-6);
  border-bottom: 1px solid var(--gray-200);
}

.event-status-tag {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
}

.event-time-info {
  color: var(--text-secondary);
  font-size: var(--font-size-md);
  text-align: right;
}

.creator-info {
  margin-bottom: var(--spacing-6);
  color: var(--text-secondary);
  font-size: var(--font-size-md);
}

.event-detail-content h3 {
  font-size: var(--font-size-lg);
  margin-bottom: var(--spacing-4);
  color: var(--text-primary);
}

.description {
  line-height: 1.8;
  color: var(--text-secondary);
  white-space: pre-wrap;
}

.event-actions {
  margin-top: var(--spacing-6);
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .event-detail-header {
    flex-direction: column;
    gap: var(--spacing-4);
  }
  
  .event-time-info {
    text-align: left;
  }
}
</style> 