import SockJS from 'sockjs-client';
// 如果上面的导入方式不工作，可以尝试下面这种方式:
// const SockJS = require('sockjs-client');
import { Client, IFrame, IMessage } from '@stomp/stompjs';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../stores/user';

// 消息类型
export enum MessageType {
  CHAT = 'CHAT',
  JOIN = 'JOIN',
  LEAVE = 'LEAVE'
}

// 聊天消息接口
export interface ChatMessage {
  type: MessageType;
  senderId: number;
  receiverId: number;
  content: string;
  timestamp?: number;
}

interface MessageCallback {
  (message: ChatMessage): void;
}

class WebSocketService {
  private client: Client | null = null;
  private messageCallbacks: Array<MessageCallback> = [];
  private connected = false;
  private reconnectAttempts = 0;
  private reconnectTimeout: number | null = null;
  private maxReconnectAttempts = 5;

  // 初始化WebSocket连接
  connect() {
    const userStore = useUserStore();
    if (!userStore.isLoggedIn) {
      console.warn('用户未登录，不能建立WebSocket连接');
      return false;
    }

    if (this.connected) {
      console.warn('WebSocket已连接，无需重复连接');
      return true;
    }

    try {
      // 使用与API相同的基础URL，确保WebSocket连接到正确的后端
      const baseURL = 'http://localhost:8080';
      const sockjsUrl = `${baseURL}/ws`;
      
      console.log('正在连接WebSocket:', sockjsUrl);
      
      // 检测是否有可用的userId，没有则无法建立连接
      if (!userStore.user?.id) {
        console.error('WebSocket连接失败: 用户ID未定义');
        return false;
      }
      
      this.client = new Client({
        webSocketFactory: () => new SockJS(sockjsUrl, null, {
          transports: ['websocket', 'xhr-streaming', 'xhr-polling'],
          withCredentials: true
        }),
        debug: function(str: string) {
          console.log('STOMP: ' + str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        connectionTimeout: 10000, // 连接超时时间
        onStompError: (frame) => {
          console.error('Stomp错误:', frame);
          this.connected = false;
          this.scheduleReconnect();
        }
      });

      this.client.onConnect = this.onConnected.bind(this);
      // 添加WebSocket连接断开的监听器
      this.client.onWebSocketClose = this.onWebSocketClose.bind(this);
      this.client.onDisconnect = this.onDisconnect.bind(this);
      this.client.activate();
      return true;
    } catch (error) {
      console.error('WebSocket连接错误:', error);
      this.scheduleReconnect();
      return false;
    }
  }

  // 连接成功回调
  private onConnected(frame: IFrame) {
    this.connected = true;
    this.reconnectAttempts = 0;
    console.log('WebSocket连接成功');
    
    const userStore = useUserStore();
    const userId = userStore.user?.id;
    
    if (!this.client || !userId) return;

    // 订阅个人消息通道
    this.client.subscribe(`/user/${userId}/queue/messages`, this.onMessageReceived.bind(this));
    
    // 订阅公共通道（可选，用于系统广播等）
    this.client.subscribe('/topic/public', this.onPublicMessage.bind(this));
    
    // 发送上线通知
    this.sendJoinMessage(userId);
  }

  // 发送用户上线消息
  private sendJoinMessage(userId: number) {
    if (!this.client || !this.connected) return;
    
    const joinMessage: ChatMessage = {
      type: MessageType.JOIN,
      senderId: userId,
      receiverId: 0, // 0表示发送给所有人
      content: ''
    };
    
    this.client.publish({
      destination: '/app/chat.join',
      body: JSON.stringify(joinMessage)
    });
  }

  // 接收个人消息
  private onMessageReceived(payload: IMessage) {
    try {
      const message: ChatMessage = JSON.parse(payload.body);
      console.log('收到消息:', message);
      
      // 触发所有注册的回调
      this.messageCallbacks.forEach(callback => {
        callback(message);
      });
    } catch (error) {
      console.error('解析消息错误:', error);
    }
  }

  // 接收公共消息
  private onPublicMessage(payload: IMessage) {
    try {
      const message: ChatMessage = JSON.parse(payload.body);
      console.log('收到公共消息:', message);
      
      // 处理上线/下线通知
      if (message.type === MessageType.JOIN) {
        // 处理上线通知
        console.log(`用户 ${message.senderId} 上线了`);
      } else if (message.type === MessageType.LEAVE) {
        // 处理下线通知
        console.log(`用户 ${message.senderId} 下线了`);
      }
    } catch (error) {
      console.error('解析公共消息错误:', error);
    }
  }

  // 发送消息
  sendMessage(receiverId: number, content: string) {
    if (!this.client || !this.connected) {
      ElMessage.warning('消息发送失败: WebSocket未连接');
      return false;
    }
    
    const userStore = useUserStore();
    const userId = userStore.user?.id;
    
    if (!userId) {
      ElMessage.warning('消息发送失败: 用户未登录');
      return false;
    }
    
    const chatMessage: ChatMessage = {
      type: MessageType.CHAT,
      senderId: userId,
      receiverId: receiverId,
      content: content
    };
    
    try {
      this.client.publish({
        destination: '/app/chat',
        body: JSON.stringify(chatMessage)
      });
      return true;
    } catch (error) {
      console.error('发送消息错误:', error);
      ElMessage.error('消息发送失败');
      return false;
    }
  }

  // 添加消息监听回调
  onMessage(callback: MessageCallback) {
    this.messageCallbacks.push(callback);
  }

  // 移除消息监听回调
  offMessage(callback: MessageCallback) {
    const index = this.messageCallbacks.indexOf(callback);
    if (index !== -1) {
      this.messageCallbacks.splice(index, 1);
    }
  }

  // 错误处理
  private onError(frame: IFrame) {
    console.error('WebSocket连接错误:', frame);
    this.connected = false;
    this.scheduleReconnect();
  }
  
  // WebSocket连接关闭处理
  private onWebSocketClose() {
    console.warn('WebSocket连接已关闭');
    this.connected = false;
    this.scheduleReconnect();
  }
  
  // STOMP断开连接处理
  private onDisconnect() {
    console.warn('STOMP连接已断开');
    this.connected = false;
    this.scheduleReconnect();
  }

  // 计划重连
  private scheduleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      const delay = Math.min(3000 * Math.pow(1.5, this.reconnectAttempts - 1), 15000);
      
      console.log(`尝试在 ${delay}ms 后重新连接... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      
      if (this.reconnectTimeout) {
        clearTimeout(this.reconnectTimeout);
      }
      
      this.reconnectTimeout = window.setTimeout(() => {
        console.log('执行WebSocket重连...');
        this.connect();
      }, delay);
    } else {
      console.error(`已达到最大重连尝试次数 (${this.maxReconnectAttempts})`);
      ElMessage.error('消息连接失败，请刷新页面或手动重连');
    }
  }

  // 检查连接状态并尝试重连
  checkConnectionAndReconnect() {
    if (!this.connected) {
      console.log('WebSocket连接已断开，尝试重新连接...');
      // 重置重连尝试次数，以便有更多机会连接
      this.reconnectAttempts = 0;
      return this.connect();
    }
    return true;
  }

  // 断开连接
  disconnect() {
    console.log('断开WebSocket连接');
    if (this.client) {
      try {
        this.client.deactivate();
      } catch (error) {
        console.error('断开WebSocket连接时出错:', error);
      }
      this.client = null;
    }
    this.connected = false;
    this.messageCallbacks = [];
    
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
  }

  // 检查是否已连接
  isConnected() {
    // 增加更严格的检查 - 确保连接状态正确且client对象有效并处于激活状态
    return this.connected && 
           this.client !== null && 
           this.client.connected && 
           !!this.client.webSocket;
  }
}

// 导出单例
export const websocketService = new WebSocketService();
export default websocketService; 