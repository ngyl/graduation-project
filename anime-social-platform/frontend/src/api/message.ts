import request from './axios'

/**
 * 获取聊天好友列表（包含最新消息）
 * @returns 聊天好友列表
 */
export function getChatFriends() {
  return request({
    url: '/api/messages/chat-friends',
    method: 'get'
  })
}

/**
 * 获取与指定用户的聊天历史
 * @param friendId 好友ID
 * @param page 页码
 * @param size 每页记录数
 * @returns 聊天记录
 */
export function getChatHistory(friendId: number, page: number = 1, size: number = 20) {
  return request({
    url: `/api/messages/chat/${friendId}`,
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取收件箱消息
 * @param page 页码
 * @param size 每页记录数
 * @returns 收件箱消息
 */
export function getInboxMessages(page: number = 1, size: number = 20) {
  return request({
    url: '/api/messages/inbox',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 发送消息(HTTP方式)
 * @param receiverId 接收者ID
 * @param content 消息内容
 * @returns 操作结果
 */
export function sendMessage(receiverId: number, content: string) {
  return request({
    url: '/api/messages/send',
    method: 'post',
    params: { receiverId, content }
  })
}

/**
 * 标记消息为已读
 * @param messageId 消息ID
 * @returns 操作结果
 */
export function markMessageAsRead(messageId: number) {
  return request({
    url: `/api/messages/${messageId}/read`,
    method: 'put'
  })
}

/**
 * 批量标记来自某用户的消息为已读
 * @param senderId 发送者ID
 * @returns 操作结果
 */
export function markAllAsRead(senderId: number) {
  return request({
    url: '/api/messages/read-all',
    method: 'put',
    params: { senderId }
  })
}

/**
 * 获取未读消息数量
 * @returns 未读消息数量
 */
export function getUnreadCount() {
  return request({
    url: '/api/messages/unread-count',
    method: 'get'
  })
}

/**
 * 删除消息
 * @param messageId 消息ID
 * @returns 操作结果
 */
export function deleteMessage(messageId: number) {
  return request({
    url: `/api/messages/${messageId}`,
    method: 'delete'
  })
} 