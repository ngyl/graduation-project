package com.animesocial.platform.config;

import com.animesocial.platform.model.dto.ChatMessage;
import com.animesocial.platform.model.dto.ChatMessage.MessageType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket事件监听器
 * 处理WebSocket连接和断开事件
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    /**
     * 监听WebSocket连接建立事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("收到了一个新的WebSocket连接");
    }

    /**
     * 监听WebSocket连接断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 从会话中获取用户信息
        Integer userId = (Integer) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            logger.info("用户断开连接 : " + userId);

            // 创建一个离开消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(MessageType.LEAVE);
            chatMessage.setSenderId(userId);
            
            // 广播用户离开消息
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
} 