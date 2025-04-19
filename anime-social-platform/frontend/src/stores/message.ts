import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as messageApi from '../api/message'
import { MessageDTO, ChatFriend } from '../types/message'
import websocketService, { ChatMessage } from '../utils/websocket'
import { useUserStore } from './user'
import { ElNotification } from 'element-plus'

// 定义API响应类型
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export const useMessageStore = defineStore('message', () => {
  // 状态
  const chatFriends = ref<ChatFriend[]>([])
  const currentChatMessages = ref<MessageDTO[]>([])
  const currentChatUserId = ref<number | null>(null)
  const unreadTotal = ref(0)
  const loading = ref(false)
  const initialized = ref(false)

  // 计算属性
  const sortedChatFriends = computed(() => {
    return [...chatFriends.value].sort((a, b) => {
      // 首先按有无未读消息排序
      if (a.unreadCount > 0 && b.unreadCount === 0) return -1;
      if (a.unreadCount === 0 && b.unreadCount > 0) return 1;
      
      // 然后按最后消息时间排序
      const timeA = new Date(a.lastMessage?.sendTime || 0).getTime();
      const timeB = new Date(b.lastMessage?.sendTime || 0).getTime();
      return timeB - timeA;
    });
  });
  
  const hasUnreadMessages = computed(() => unreadTotal.value > 0);

  // 获取聊天好友列表
  async function fetchChatFriends() {
    try {
      loading.value = true;
      const response = await messageApi.getChatFriends();
      const data = response.data as ApiResponse<MessageDTO[]>;
      
      if (data.code === 200) {
        chatFriends.value = processChatFriends(data.data);
        updateUnreadTotal();
      }
      return data;
    } catch (error) {
      console.error('获取聊天好友列表失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  // 处理聊天好友数据
  function processChatFriends(messages: MessageDTO[]): ChatFriend[] {
    const userStore = useUserStore();
    const currentUserId = userStore.user?.id;
    if (!currentUserId) return [];
    
    const friendsMap = new Map<number, ChatFriend>();
    
    messages.forEach(message => {
      // 确定聊天对象ID
      const friendId = message.senderId === currentUserId 
        ? message.receiverId 
        : message.senderId;
      
      // 提取聊天对象信息
      const friendInfo = {
        id: friendId,
        username: message.senderId === currentUserId 
          ? message.receiverName
          : message.senderName,
        avatar: message.senderId === currentUserId
          ? message.receiverAvatar
          : message.senderAvatar
      };
      
      // 如果是新的聊天对象，创建记录
      if (!friendsMap.has(friendId)) {
        friendsMap.set(friendId, {
          user: friendInfo,
          lastMessage: message,
          unreadCount: message.senderId !== currentUserId && message.readStatus === 0 ? 1 : 0
        });
      } else {
        // 更新现有聊天对象的最新消息
        const existing = friendsMap.get(friendId)!;
        const existingTime = new Date(existing.lastMessage.sendTime).getTime();
        const newTime = new Date(message.sendTime).getTime();
        
        if (newTime > existingTime) {
          existing.lastMessage = message;
        }
        
        // 累计未读消息
        if (message.senderId !== currentUserId && message.readStatus === 0) {
          existing.unreadCount += 1;
        }
      }
    });
    
    return Array.from(friendsMap.values());
  }

  // 获取与指定用户的聊天记录
  async function fetchChatHistory(friendId: number, page: number = 1, size: number = 20) {
    try {
      loading.value = true;
      const response = await messageApi.getChatHistory(friendId, page, size);
      const data = response.data as ApiResponse<MessageDTO[]>;
      
      if (data.code === 200) {
        if (page === 1) {
          // 第一页，替换所有消息
          currentChatMessages.value = data.data;
        } else {
          // 加载更多，追加消息
          currentChatMessages.value = [...currentChatMessages.value, ...data.data];
        }
        currentChatUserId.value = friendId;
        
        // 更新未读状态
        updateUnreadStatusForUser(friendId);
      }
      return data;
    } catch (error) {
      console.error('获取聊天记录失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  // 发送消息(WebSocket方式)
  function sendMessage(receiverId: number, content: string) {
    // 通过WebSocket发送消息
    const sent = websocketService.sendMessage(receiverId, content);
    
    if (sent) {
      // 如果发送成功，先乐观更新UI
      const userStore = useUserStore();
      const currentUserId = userStore.user?.id;
      if (!currentUserId) return false;
      
      // 创建临时消息对象
      const tempMessage: MessageDTO = {
        id: -Date.now(), // 临时ID，负数避免冲突
        senderId: currentUserId,
        senderName: userStore.user?.username || '',
        senderAvatar: userStore.user?.avatar || '',
        receiverId: receiverId,
        receiverName: '',  // 不重要，因为是自己发的
        receiverAvatar: '',
        content: content,
        sendTime: new Date().toISOString(),
        readStatus: 1  // 已读，因为是自己发的
      };
      
      // 添加到当前聊天记录
      if (currentChatUserId.value === receiverId) {
        currentChatMessages.value = [tempMessage, ...currentChatMessages.value];
      }
      
      // 更新聊天好友列表
      updateChatFriendWithMessage(tempMessage);
    }
    
    return sent;
  }

  // 接收新消息处理
  function handleNewMessage(message: ChatMessage) {
    const userStore = useUserStore();
    const currentUserId = userStore.user?.id;
    if (!currentUserId) return;
    
    // 创建标准消息格式
    const newMessage: MessageDTO = {
      id: Date.now(), // 临时ID，服务端返回的可能没有id
      senderId: message.senderId,
      senderName: '', // 可能需要从用户缓存获取
      senderAvatar: '',
      receiverId: message.receiverId,
      receiverName: '',
      receiverAvatar: '',
      content: message.content,
      sendTime: new Date(message.timestamp || Date.now()).toISOString(),
      readStatus: 0 // 默认未读
    };
    
    // 如果是当前聊天对象发的消息，加入到聊天记录
    if (currentChatUserId.value === message.senderId) {
      currentChatMessages.value = [newMessage, ...currentChatMessages.value];
      
      // 标记为已读
      messageApi.markAllAsRead(message.senderId);
      
      // 更新本地状态
      newMessage.readStatus = 1;
    } else {
      // 更新未读计数
      unreadTotal.value++;
      
      // 显示消息通知
      showMessageNotification(newMessage);
    }
    
    // 更新聊天好友列表
    updateChatFriendWithMessage(newMessage);
  }

  // 显示新消息通知
  function showMessageNotification(message: MessageDTO) {
    // 从好友列表中找到发送者信息
    const friend = chatFriends.value.find(f => f.user.id === message.senderId);
    const senderName = friend?.user.username || message.senderName || '有人';
    
    ElNotification({
      title: `来自 ${senderName} 的新消息`,
      message: message.content.length > 50 ? message.content.substring(0, 50) + '...' : message.content,
      type: 'success',
      duration: 4000,
      onClick: () => {
        // 点击通知时跳转到对应的聊天
        currentChatUserId.value = message.senderId;
      }
    });
    
    // 如果浏览器支持，播放提示音
    try {
      const audio = new Audio('/message-notification.mp3');
      audio.play().catch(e => console.log('无法播放提示音:', e));
    } catch (error) {
      console.log('不支持音频播放');
    }
  }

  // 根据新消息更新聊天好友列表
  function updateChatFriendWithMessage(message: MessageDTO) {
    const userStore = useUserStore();
    const currentUserId = userStore.user?.id;
    if (!currentUserId) return;
    
    // 确定聊天对象ID
    const friendId = message.senderId === currentUserId 
      ? message.receiverId 
      : message.senderId;
      
    // 查找此聊天对象是否存在
    const existingFriendIndex = chatFriends.value.findIndex(f => f.user.id === friendId);
    
    if (existingFriendIndex >= 0) {
      // 更新现有聊天对象
      const friend = chatFriends.value[existingFriendIndex];
      
      // 更新最新消息
      const existingTime = new Date(friend.lastMessage.sendTime).getTime();
      const newTime = new Date(message.sendTime).getTime();
      if (newTime > existingTime) {
        friend.lastMessage = message;
      }
      
      // 更新未读计数
      if (message.senderId !== currentUserId && message.readStatus === 0) {
        friend.unreadCount++;
      }
      
      // 移到列表顶部
      const updatedFriend = chatFriends.value.splice(existingFriendIndex, 1)[0];
      chatFriends.value.unshift(updatedFriend);
    } else {
      // 新的聊天对象，需要获取用户信息
      // 这里简化处理，实际应该从用户缓存或API获取详细信息
      const isMessageFromMe = message.senderId === currentUserId;
      
      const newFriend: ChatFriend = {
        user: {
          id: friendId,
          username: isMessageFromMe ? message.receiverName : message.senderName,
          avatar: isMessageFromMe ? message.receiverAvatar : message.senderAvatar
        },
        lastMessage: message,
        unreadCount: isMessageFromMe ? 0 : 1
      };
      
      // 添加到列表顶部
      chatFriends.value.unshift(newFriend);
    }
  }

  // 更新未读消息总数
  function updateUnreadTotal() {
    unreadTotal.value = chatFriends.value.reduce((total, friend) => total + friend.unreadCount, 0);
  }

  // 更新指定用户消息的已读状态
  function updateUnreadStatusForUser(userId: number) {
    const friendIndex = chatFriends.value.findIndex(f => f.user.id === userId);
    if (friendIndex >= 0) {
      chatFriends.value[friendIndex].unreadCount = 0;
      updateUnreadTotal();
    }
  }

  // 初始化WebSocket连接和消息监听
  function initialize() {
    if (initialized.value) return;
    
    const userStore = useUserStore();
    if (!userStore.isLoggedIn) return;
    
    // 连接WebSocket
    websocketService.connect();
    
    // 添加消息监听
    websocketService.onMessage(handleNewMessage);
    
    // 加载聊天好友列表
    fetchChatFriends();
    
    // 获取未读消息数量
    messageApi.getUnreadCount().then(response => {
      const data = response.data as ApiResponse<number>;
      if (data.code === 200) {
        unreadTotal.value = data.data;
      }
    });
    
    initialized.value = true;
  }

  // 清理资源
  function cleanup() {
    websocketService.disconnect();
    chatFriends.value = [];
    currentChatMessages.value = [];
    currentChatUserId.value = null;
    unreadTotal.value = 0;
    initialized.value = false;
  }

  return {
    // 状态
    chatFriends,
    currentChatMessages,
    currentChatUserId,
    unreadTotal,
    loading,
    
    // 计算属性
    sortedChatFriends,
    hasUnreadMessages,
    
    // 操作
    initialize,
    cleanup,
    fetchChatFriends,
    fetchChatHistory,
    sendMessage,
    updateUnreadStatusForUser
  }
}) 