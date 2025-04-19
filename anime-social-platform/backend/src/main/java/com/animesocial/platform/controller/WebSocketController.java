package com.animesocial.platform.controller;

import com.animesocial.platform.model.Message;
import com.animesocial.platform.model.dto.ChatMessage;
import com.animesocial.platform.service.MessageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * WebSocket控制器
 * 处理WebSocket消息，包括私信、上线、离线通知
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
     * 客户端应发送消息到/app/chat
     */
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
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
        // 使用/user/{userId}/queue/messages格式，确保消息只发送给特定用户
        messagingTemplate.convertAndSendToUser(
            chatMessage.getReceiverId().toString(),
            "/queue/messages",
            chatMessage
        );
    }

    /**
     * 处理用户加入聊天
     * 客户端应发送消息到/app/chat.join
     */
    @MessageMapping("/chat.join")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // 记录日志
        logger.info("用户{}加入聊天", chatMessage.getSenderId());
        
        // 在WebSocket会话中存储用户ID
        headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());
        
        // 通知所有用户有新用户上线（可选，根据需求决定是否需要）
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }
} 