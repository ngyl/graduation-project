package com.animesocial.platform.service;

import java.util.List;

import com.animesocial.platform.model.Message;
import com.animesocial.platform.model.dto.MessageDTO;

/**
 * 消息服务接口
 * 定义消息相关的业务操作，包括消息发送、查询、标记已读等功能
 */
public interface MessageService {
    /**
     * 发送私信
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 消息内容
     * @return 发送的消息对象
     * @throws RuntimeException 如果发送者或接收者不存在
     */
    Message sendMessage(Integer senderId, Integer receiverId, String content);
    
    /**
     * 获取用户的收件箱消息
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 收件箱消息列表，按发送时间降序排序
     */
    List<MessageDTO> getInboxMessages(Integer userId, Integer page, Integer size);
    
    /**
     * 获取用户的发件箱消息
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 发件箱消息列表，按发送时间降序排序
     */
    List<MessageDTO> getOutboxMessages(Integer userId, Integer page, Integer size);
    
    /**
     * 获取与指定用户的聊天记录
     * 
     * @param userId 当前用户ID
     * @param friendId 聊天对象用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 聊天记录列表，按发送时间降序排序
     */
    List<MessageDTO> getChatHistory(Integer userId, Integer friendId, Integer page, Integer size);
    
    /**
     * 标记消息为已读
     * 
     * @param messageId 消息ID
     * @param userId 用户ID（必须是接收者）
     * @throws RuntimeException 如果消息不存在或用户不是接收者
     */
    void markAsRead(Integer messageId, Integer userId);
    
    /**
     * 批量标记消息为已读
     * 
     * @param receiverId 接收者ID
     * @param senderId 发送者ID（可为null，表示所有发送者）
     * @return 标记的消息数量
     */
    int batchMarkAsRead(Integer receiverId, Integer senderId);
    
    /**
     * 删除消息
     * 
     * @param messageId 消息ID
     * @param userId 用户ID（必须是发送者或接收者）
     * @throws RuntimeException 如果消息不存在或用户既不是发送者也不是接收者
     */
    void deleteMessage(Integer messageId, Integer userId);
    
    /**
     * 获取与用户有聊天记录的好友列表
     * 
     * @param userId 用户ID
     * @return 聊天好友列表，每个好友附带最新一条消息
     */
    List<MessageDTO> getChatFriends(Integer userId);
    
    /**
     * 获取用户未读消息数量
     * 
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int getUnreadCount(Integer userId);
    
    /**
     * 获取来自特定发送者的未读消息数量
     * 
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     * @return 未读消息数量
     */
    int getUnreadCountFromSender(Integer receiverId, Integer senderId);
} 