package com.animesocial.platform.model.dto;

import lombok.Data;

/**
 * WebSocket聊天消息对象
 * 用于在WebSocket通信中传输消息内容
 */
@Data
public class ChatMessage {
    /** 消息类型：CHAT-聊天消息，JOIN-加入，LEAVE-离开 */
    private MessageType type;
    
    /** 发送者ID */
    private Integer senderId;
    
    /** 接收者ID */
    private Integer receiverId;
    
    /** 消息内容 */
    private String content;
    
    /** 消息时间戳 */
    private Long timestamp;
    
    /** 消息类型枚举 */
    public enum MessageType {
        CHAT,    // 聊天消息
        JOIN,    // 用户上线
        LEAVE    // 用户离线
    }
} 