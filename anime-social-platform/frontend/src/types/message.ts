/**
 * 消息DTO
 */
export interface MessageDTO {
  id: number;
  senderId: number;
  senderName: string;
  senderAvatar: string;
  receiverId: number;
  receiverName: string;
  receiverAvatar: string;
  content: string;
  sendTime: string;
  readStatus: number;
}

/**
 * 聊天好友信息（包含最新消息）
 */
export interface ChatFriend {
  user: {
    id: number;
    username: string;
    avatar: string;
  };
  lastMessage: MessageDTO;
  unreadCount: number;
}

/**
 * 发送消息表单数据
 */
export interface SendMessageForm {
  content: string;
}

/**
 * 消息列表响应
 */
export interface MessagesResponse {
  data: {
    list: MessageDTO[];
    total: number;
    pageNum: number;
    pageSize: number;
  };
  code: number;
  message: string;
}

/**
 * 消息数据中包含的用户
 */
export interface MessageUser {
  id: number;
  username: string;
  avatar: string;
} 