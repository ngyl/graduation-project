package com.animesocial.platform.repository;

import com.animesocial.platform.model.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 消息数据访问接口
 */
@Mapper
public interface MessageRepository {

    /**
     * 插入一条消息
     * 
     * @param message 消息对象
     * @return 影响的行数
     */
    int insertMessage(Message message);
    
    /**
     * 根据ID获取消息
     * 
     * @param id 消息ID
     * @return 消息对象
     */
    Message getMessageById(Integer id);
    
    /**
     * 获取用户的收件箱消息
     * 
     * @param userId 用户ID
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 消息列表
     */
    List<Message> getInboxMessages(Integer userId, Integer offset, Integer limit);
    
    /**
     * 获取用户的发件箱消息
     * 
     * @param userId 用户ID
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 消息列表
     */
    List<Message> getOutboxMessages(Integer userId, Integer offset, Integer limit);
    
    /**
     * 获取两个用户之间的聊天记录
     * 
     * @param userId 当前用户ID
     * @param friendId 好友用户ID
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 消息列表
     */
    List<Message> getChatHistory(Integer userId, Integer friendId, Integer offset, Integer limit);
    
    /**
     * 标记消息为已读
     * 
     * @param messageId 消息ID
     * @return 影响的行数
     */
    int markMessageAsRead(Integer messageId);
    
    /**
     * 批量标记消息为已读
     * 
     * @param receiverId 接收者ID
     * @param senderId 发送者ID，可为null表示所有发送者
     * @return 影响的行数
     */
    int batchMarkAsRead(Integer receiverId, Integer senderId);
    
    /**
     * 删除消息
     * 
     * @param messageId 消息ID
     * @return 影响的行数
     */
    int deleteMessage(Integer messageId);
    
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
    
    /**
     * 获取与用户有聊天记录的好友最新消息
     * 
     * @param userId 用户ID
     * @return 最新消息列表
     */
    List<Message> getLatestMessagesWithUsers(Integer userId);
} 