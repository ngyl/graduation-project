import service from './axios';
import type { ApiResponse } from '@/types/api';
import type { ChatFriend, Message } from '@/types/message';

/**
 * 获取聊天好友列表（包含最新消息）
 */
export const getChatFriends = () => {
  return service.get<ApiResponse<ChatFriend[]>>('/messages/chat-friends');
};

/**
 * 获取与指定用户的聊天历史
 * @param friendId 好友ID
 * @param page 页码
 * @param size 每页记录数
 */
export const getChatHistory = (friendId: number, page: number = 1, size: number = 20) => {
  return service.get<ApiResponse<Message[]>>(`/messages/chat/${friendId}`, {
    params: { page, size }
  });
};

/**
 * 获取收件箱消息
 * @param page 页码
 * @param size 每页记录数
 */
export const getInboxMessages = (page: number = 1, size: number = 20) => {
  return service.get<ApiResponse<Message[]>>('/messages/inbox', {
    params: { page, size }
  });
};

/**
 * 发送消息(HTTP方式)
 * @param receiverId 接收者ID
 * @param content 消息内容
 */
export const sendMessage = (receiverId: number, content: string) => {
  return service.post<ApiResponse<void>>('/messages/send', null, {
    params: { receiverId, content }
  });
};

/**
 * 标记消息为已读
 * @param messageId 消息ID
 */
export const markMessageAsRead = (messageId: number) => {
  return service.put<ApiResponse<void>>(`/messages/${messageId}/read`);
};

/**
 * 批量标记来自某用户的消息为已读
 * @param senderId 发送者ID
 */
export const markAllAsRead = (senderId: number) => {
  return service.put<ApiResponse<void>>('/messages/read-all', null, {
    params: { senderId }
  });
};

/**
 * 获取未读消息数量
 */
export const getUnreadCount = () => {
  return service.get<ApiResponse<number>>('/messages/unread-count');
};

/**
 * 删除消息
 * @param messageId 消息ID
 */
export const deleteMessage = (messageId: number) => {
  return service.delete<ApiResponse<void>>(`/messages/${messageId}`);
}; 