<template>
  <div class="messages-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>私信列表</h2>
        <span v-if="messageStore.hasUnreadMessages" class="unread-badge">
          {{ messageStore.unreadTotal }}
        </span>
      </div>
      
      <div class="friend-list">
        <div 
          v-for="friend in messageStore.sortedChatFriends" 
          :key="friend.user.id" 
          class="friend-item"
          :class="{ 'active': currentFriendId === friend.user.id, 'unread': friend.unreadCount > 0 }"
          @click="selectFriend(friend.user.id)"
        >
          <div class="avatar">
            <img :src="friend.user.avatar || '/default-avatar.png'" alt="头像">
            <span v-if="friend.unreadCount > 0" class="unread-count">{{ friend.unreadCount }}</span>
          </div>
          <div class="info">
            <div class="name">{{ friend.user.username }}</div>
            <div class="last-message">{{ friend.lastMessage.content }}</div>
          </div>
          <div class="time">{{ formatTime(friend.lastMessage.sendTime) }}</div>
        </div>
        
        <div v-if="messageStore.loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon> 加载中...
        </div>
        
        <div v-else-if="messageStore.chatFriends.length === 0" class="empty-state">
          暂无私信记录
        </div>
      </div>
    </div>
    
    <div class="chat-area">
      <template v-if="currentFriendId">
        <div class="chat-header">
          <h3>{{ currentFriend?.username || 'Chat' }}</h3>
          <div class="chat-header-actions">
            <el-tooltip content="清除未读">
              <el-button 
                type="text"
                @click="markAsRead" 
                :disabled="!hasUnreadFromCurrentFriend"
              >
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
        
        <div class="message-list" ref="messageListRef" @scroll="handleScroll">
          <div v-if="messageStore.loading" class="loading-messages">
            <el-icon class="is-loading"><Loading /></el-icon> 加载中...
          </div>
          
          <div v-if="canLoadMore && !messageStore.loading" class="load-more">
            <el-button type="text" @click="loadMoreMessages">加载更多消息</el-button>
          </div>
          
          <template v-else>
            <div v-if="messageStore.currentChatMessages.length === 0" class="empty-chat">
              开始新的对话吧
            </div>
            
            <div 
              v-for="message in messageStore.currentChatMessages" 
              :key="message.id" 
              class="message-item"
              :class="{ 'sent': message.senderId === currentUserId, 'received': message.senderId !== currentUserId }"
            >
              <div class="message-avatar">
                <img :src="message.senderId === currentUserId ? currentUserAvatar : currentFriend?.avatar || '/default-avatar.png'" alt="头像">
              </div>
              <div class="message-content">
                <div class="message-bubble">{{ message.content }}</div>
                <div class="message-time">
                  {{ formatTime(message.sendTime) }}
                  <span v-if="message.senderId === currentUserId" class="message-status">
                    <el-icon v-if="message.readStatus === 1" style="color: #409eff; font-size: 12px;"><Check /></el-icon>
                  </span>
                </div>
              </div>
            </div>
          </template>
          
          <div v-if="isTyping" class="typing-indicator">
            <div class="typing-bubble">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
        
        <div class="message-input">
          <div class="input-actions">
            <el-tooltip content="发送表情">
              <el-button type="text" @click="toggleEmojiPicker">
                <el-icon><ChatDotRound /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
          
          <el-input
            v-model="messageText"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keyup.enter.ctrl="sendMessage"
            @input="handleTyping"
          />
          
          <el-button type="primary" :disabled="!messageText.trim()" @click="sendMessage">
            发送
          </el-button>
          
          <div v-if="showEmojiPicker" class="emoji-picker">
            <div class="emoji-list">
              <span 
                v-for="emoji in emojis" 
                :key="emoji" 
                class="emoji-item" 
                @click="insertEmoji(emoji)"
              >
                {{ emoji }}
              </span>
            </div>
          </div>
        </div>
      </template>
      
      <div v-else class="select-chat">
        <div class="empty-state-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <p>选择一个联系人开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useMessageStore } from '../stores/message';
import { useUserStore } from '../stores/user';
import { ElMessage } from 'element-plus';
import { Loading, ChatDotRound, View, Check } from '@element-plus/icons-vue';
import * as messageApi from '../api/message';

// 初始化store
const messageStore = useMessageStore();
const userStore = useUserStore();

// 状态
const messageText = ref('');
const messageListRef = ref<HTMLElement | null>(null);
const currentFriendId = ref<number | null>(null);
const currentPage = ref(1);
const pageSize = ref(20);
const canLoadMore = ref(false);
const isTyping = ref(false);
const typingTimeout = ref<number | null>(null);
const showEmojiPicker = ref(false);
const emojis = ref(['😀', '😂', '😍', '🤔', '😎', '👍', '❤️', '😊', '🎉', '🌟', '😢', '😡', '🤯', '🙏', '✨']);

// 计算属性
const currentUserId = computed(() => userStore.user?.id);
const currentUserAvatar = computed(() => userStore.user?.avatar || '/default-avatar.png');

const currentFriend = computed(() => {
  if (!currentFriendId.value) return null;
  return messageStore.chatFriends.find(f => f.user.id === currentFriendId.value)?.user;
});

const hasUnreadFromCurrentFriend = computed(() => {
  if (!currentFriendId.value) return false;
  const friend = messageStore.chatFriends.find(f => f.user.id === currentFriendId.value);
  return friend?.unreadCount ? friend.unreadCount > 0 : false;
});

// 选择好友
async function selectFriend(friendId: number) {
  currentFriendId.value = friendId;
  currentPage.value = 1; // 重置页码
  canLoadMore.value = true; // 假设可以加载更多
  await messageStore.fetchChatHistory(friendId);
  
  // 滚动到最新消息
  await nextTick();
  scrollToBottom();
}

// 加载更多消息
async function loadMoreMessages() {
  if (messageStore.loading || !currentFriendId.value) return;
  
  currentPage.value++;
  const response = await messageStore.fetchChatHistory(
    currentFriendId.value, 
    currentPage.value,
    pageSize.value
  );
  
  // 检查是否还有更多消息
  if (response?.data?.length < pageSize.value) {
    canLoadMore.value = false;
  }
}

// 处理滚动事件
function handleScroll(event: Event) {
  const target = event.target as HTMLElement;
  if (target.scrollTop <= 50 && !messageStore.loading && canLoadMore.value) {
    loadMoreMessages();
  }
}

// 发送消息
function sendMessage() {
  if (!messageText.value.trim() || !currentFriendId.value) return;
  
  const success = messageStore.sendMessage(currentFriendId.value, messageText.value);
  if (success) {
    messageText.value = '';
    showEmojiPicker.value = false;
    
    // 滚动到最新消息
    nextTick(() => {
      scrollToBottom();
    });
  } else {
    ElMessage.error('消息发送失败，请稍后重试');
  }
}

// 滚动到底部
function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
  }
}

// 标记消息为已读
async function markAsRead() {
  if (!currentFriendId.value) return;
  
  try {
    await messageApi.markAllAsRead(currentFriendId.value);
    // 更新本地状态
    messageStore.updateUnreadStatusForUser(currentFriendId.value);
    ElMessage.success('已标记为已读');
  } catch (error) {
    console.error('标记已读失败:', error);
    ElMessage.error('操作失败，请稍后重试');
  }
}

// 处理正在输入状态
function handleTyping() {
  // 设置正在输入状态
  isTyping.value = true;
  
  if (typingTimeout.value) {
    clearTimeout(typingTimeout.value);
  }
  
  // 可以在这里添加发送"正在输入"状态到WebSocket服务器
  // websocketService.sendTypingStatus(currentFriendId.value);
  
  // 2秒后自动取消"正在输入"状态
  typingTimeout.value = window.setTimeout(() => {
    isTyping.value = false;
    typingTimeout.value = null;
  }, 2000);
}

// 切换表情选择器
function toggleEmojiPicker() {
  showEmojiPicker.value = !showEmojiPicker.value;
}

// 插入表情
function insertEmoji(emoji: string) {
  messageText.value += emoji;
}

// 格式化时间
function formatTime(timeStr: string) {
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  // 今天内
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
  
  // 过去七天内
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
    return days[date.getDay()];
  }
  
  // 其他
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });
}

// 监听当前聊天用户变化
watch(() => messageStore.currentChatUserId, (newId) => {
  if (newId && newId !== currentFriendId.value) {
    currentFriendId.value = newId;
  }
});

// 监听收到的新消息
watch(() => messageStore.currentChatMessages, () => {
  // 如果是当前聊天用户的消息，自动滚动到底部
  if (currentFriendId.value && messageStore.currentChatUserId && 
      currentFriendId.value === messageStore.currentChatUserId) {
    nextTick(() => {
      scrollToBottom();
    });
  }
}, { deep: true });

// 生命周期
onMounted(() => {
  messageStore.initialize();
});

onUnmounted(() => {
  // 不需要完全清理，只是标记当前页面不活跃
  messageStore.currentChatUserId = null;
  
  if (typingTimeout.value) {
    clearTimeout(typingTimeout.value);
  }
});
</script>

<style scoped>
.messages-container {
  display: flex;
  height: calc(100vh - 120px);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.sidebar {
  width: 300px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
}

.unread-badge {
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 0 8px;
  font-size: 12px;
  min-width: 20px;
  text-align: center;
}

.friend-list {
  flex: 1;
  overflow-y: auto;
}

.friend-item {
  padding: 12px 16px;
  display: flex;
  align-items: flex-start;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.friend-item:hover {
  background-color: #f5f7fa;
}

.friend-item.active {
  background-color: #ecf5ff;
}

.friend-item.unread {
  background-color: #f0f9eb;
}

.avatar {
  position: relative;
  margin-right: 12px;
}

.avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.unread-count {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #f56c6c;
  color: white;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info {
  flex: 1;
  overflow: hidden;
}

.name {
  font-weight: 600;
  margin-bottom: 4px;
}

.last-message {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.loading, .empty-state {
  padding: 20px;
  text-align: center;
  color: #909399;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
}

.chat-header-actions {
  display: flex;
  gap: 8px;
}

.message-list {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.loading-messages, .empty-chat {
  margin: auto;
  padding: 20px;
  text-align: center;
  color: #909399;
}

.load-more {
  text-align: center;
  padding: 10px;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  max-width: 70%;
}

.message-item.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.received {
  align-self: flex-start;
}

.message-avatar {
  margin: 0 8px;
}

.message-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  display: flex;
  flex-direction: column;
}

.message-bubble {
  padding: 8px 12px;
  border-radius: 12px;
  max-width: 100%;
  word-break: break-word;
}

.sent .message-bubble {
  background-color: #409eff;
  color: white;
  border-top-right-radius: 2px;
}

.received .message-bubble {
  background-color: #f2f6fc;
  color: #303133;
  border-top-left-radius: 2px;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  align-self: flex-end;
  display: flex;
  align-items: center;
  gap: 4px;
}

.sent .message-time {
  align-self: flex-start;
}

.message-status {
  display: inline-flex;
}

.message-input {
  padding: 12px;
  border-top: 1px solid #eee;
  display: flex;
  align-items: flex-end;
  position: relative;
}

.message-input .el-input {
  margin-right: 12px;
  flex: 1;
}

.input-actions {
  display: flex;
  margin-right: 8px;
}

.select-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.empty-state-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.typing-indicator {
  align-self: flex-start;
  margin-bottom: 8px;
}

.typing-bubble {
  background-color: #f2f6fc;
  padding: 8px 12px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
}

.typing-bubble span {
  display: inline-block;
  width: 8px;
  height: 8px;
  margin: 0 1px;
  background-color: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-bubble span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-bubble span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.emoji-picker {
  position: absolute;
  bottom: 80px;
  left: 12px;
  background: white;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 240px;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  z-index: 10;
}

.emoji-list {
  display: flex;
  flex-wrap: wrap;
}

.emoji-item {
  font-size: 20px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.emoji-item:hover {
  background-color: #f5f7fa;
}
</style> 