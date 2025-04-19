package com.animesocial.platform.service.impl;

import com.animesocial.platform.model.Message;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.MessageDTO;
import com.animesocial.platform.repository.MessageRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 发送私信
     */
    @Override
    @Transactional
    public Message sendMessage(Integer senderId, Integer receiverId, String content) {
        // 验证发送者和接收者是否存在
        User sender = userRepository.findById(senderId);
        User receiver = userRepository.findById(receiverId);
        
        if (sender == null) {
            throw new RuntimeException("发送者不存在");
        }
        
        if (receiver == null) {
            throw new RuntimeException("接收者不存在");
        }
        
        // 创建消息
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setSendTime(LocalDateTime.now());
        message.setReadStatus(0); // 初始为未读状态
        
        // 保存消息
        messageRepository.insertMessage(message);
        
        return message;
    }

    /**
     * 获取用户的收件箱消息
     */
    @Override
    public List<MessageDTO> getInboxMessages(Integer userId, Integer page, Integer size) {
        // 计算分页偏移量
        int offset = (page - 1) * size;
        
        // 查询收件箱消息
        List<Message> messages = messageRepository.getInboxMessages(userId, offset, size);
        
        // 转换为DTO
        return convertToDTO(messages);
    }

    /**
     * 获取用户的发件箱消息
     */
    @Override
    public List<MessageDTO> getOutboxMessages(Integer userId, Integer page, Integer size) {
        // 计算分页偏移量
        int offset = (page - 1) * size;
        
        // 查询发件箱消息
        List<Message> messages = messageRepository.getOutboxMessages(userId, offset, size);
        
        // 转换为DTO
        return convertToDTO(messages);
    }

    /**
     * 获取与指定用户的聊天记录
     */
    @Override
    public List<MessageDTO> getChatHistory(Integer userId, Integer friendId, Integer page, Integer size) {
        // 计算分页偏移量
        int offset = (page - 1) * size;
        
        // 查询聊天记录
        List<Message> messages = messageRepository.getChatHistory(userId, friendId, offset, size);
        
        // 转换为DTO
        return convertToDTO(messages);
    }

    /**
     * 标记消息为已读
     */
    @Override
    @Transactional
    public void markAsRead(Integer messageId, Integer userId) {
        // 查询消息
        Message message = messageRepository.getMessageById(messageId);
        
        // 验证消息是否存在
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        
        // 验证用户是否是接收者
        if (!message.getReceiverId().equals(userId)) {
            throw new RuntimeException("没有权限标记此消息为已读");
        }
        
        // 标记为已读
        messageRepository.markMessageAsRead(messageId);
    }

    /**
     * 批量标记消息为已读
     */
    @Override
    @Transactional
    public int batchMarkAsRead(Integer receiverId, Integer senderId) {
        return messageRepository.batchMarkAsRead(receiverId, senderId);
    }

    /**
     * 删除消息
     */
    @Override
    @Transactional
    public void deleteMessage(Integer messageId, Integer userId) {
        // 查询消息
        Message message = messageRepository.getMessageById(messageId);
        
        // 验证消息是否存在
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        
        // 验证用户是否有权限删除
        if (!message.getSenderId().equals(userId) && !message.getReceiverId().equals(userId)) {
            throw new RuntimeException("没有权限删除此消息");
        }
        
        // 删除消息
        messageRepository.deleteMessage(messageId);
    }

    /**
     * 获取与用户有聊天记录的好友列表
     */
    @Override
    public List<MessageDTO> getChatFriends(Integer userId) {
        // 查询聊天好友
        List<Message> latestMessages = messageRepository.getLatestMessagesWithUsers(userId);
        
        // 转换为DTO
        return convertToDTO(latestMessages);
    }

    /**
     * 获取用户未读消息数量
     */
    @Override
    public int getUnreadCount(Integer userId) {
        return messageRepository.getUnreadCount(userId);
    }

    /**
     * 获取来自特定发送者的未读消息数量
     */
    @Override
    public int getUnreadCountFromSender(Integer receiverId, Integer senderId) {
        return messageRepository.getUnreadCountFromSender(receiverId, senderId);
    }
    
    /**
     * 将消息实体转换为DTO
     */
    private List<MessageDTO> convertToDTO(List<Message> messages) {
        return messages.stream().map(message -> {
            MessageDTO dto = new MessageDTO();
            
            dto.setId(message.getId());
            dto.setSenderId(message.getSenderId());
            dto.setReceiverId(message.getReceiverId());
            dto.setContent(message.getContent());
            dto.setSendTime(message.getSendTime());
            dto.setReadStatus(message.getReadStatus());
            
            // 填充发送者信息
            if (message.getSender() != null) {
                dto.setSenderName(message.getSender().getUsername());
                dto.setSenderAvatar(message.getSender().getAvatar());
            } else {
                User sender = userRepository.findById(message.getSenderId());
                if (sender != null) {
                    dto.setSenderName(sender.getUsername());
                    dto.setSenderAvatar(sender.getAvatar());
                }
            }
            
            // 填充接收者信息
            if (message.getReceiver() != null) {
                dto.setReceiverName(message.getReceiver().getUsername());
                dto.setReceiverAvatar(message.getReceiver().getAvatar());
            } else {
                User receiver = userRepository.findById(message.getReceiverId());
                if (receiver != null) {
                    dto.setReceiverName(receiver.getUsername());
                    dto.setReceiverAvatar(receiver.getAvatar());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
} 