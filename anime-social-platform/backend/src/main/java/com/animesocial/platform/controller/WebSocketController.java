package com.animesocial.platform.controller;

import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.animesocial.platform.model.Message;
import com.animesocial.platform.model.dto.ChatMessage;
import com.animesocial.platform.service.MessageService;

/**
 * WebSocket控制器
 * 处理WebSocket消息，包括私信、上线、离线通知
 * 
 * 注意：WebSocket使用的是STOMP协议，不同于RESTful API
 * - 客户端发送消息到 /app/... 端点
 * - 客户端订阅 /user/{userId}/queue/... 或 /topic/... 接收消息
 */
@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    /**
     * 处理客户端发送的聊天消息
     * 
     * @param chatMessage 聊天消息对象
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 验证用户身份
            Integer sessionUserId = (Integer) headerAccessor.getSessionAttributes().get("userId");
            if (sessionUserId == null) {
                logger.warn("用户未登录，拒绝消息");
                return;
            }
            
            if (!sessionUserId.equals(chatMessage.getSenderId())) {
                logger.warn("消息发送者ID与会话用户ID不匹配，拒绝消息. 会话ID: {}, 消息发送者ID: {}", 
                         sessionUserId, chatMessage.getSenderId());
                return;
            }
            
            // 记录日志
            logger.info("收到来自用户{}的消息，发送给用户{}", chatMessage.getSenderId(), chatMessage.getReceiverId());
            
            // 保存消息到数据库
            Message message = messageService.sendMessage(
                chatMessage.getSenderId(), 
                chatMessage.getReceiverId(), 
                chatMessage.getContent()
            );
            
            // 设置时间戳
            chatMessage.setTimestamp(
                message.getSendTime().toInstant(ZoneOffset.UTC).toEpochMilli()
            );
            
            // 将消息发送给接收者
            messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverId().toString(),
                "/queue/messages",
                chatMessage
            );
        } catch (Exception e) {
            logger.error("处理WebSocket消息时发生错误", e);
        }
    }

    /**
     * 处理用户加入聊天
     * 
     * @param chatMessage 聊天消息对象
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/chat.join")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 记录日志
            logger.info("用户{}加入聊天", chatMessage.getSenderId());
            
            // 在WebSocket会话中存储用户ID
            headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());
        } catch (Exception e) {
            logger.error("处理用户加入聊天时发生错误", e);
        }
    }
} 