package com.animesocial.platform.model.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 消息DTO，用于前端展示和交互
 */
@Data
public class MessageDTO {
    /** 消息ID */
    private Integer id;
    
    /** 发送者ID */
    private Integer senderId;
    
    /** 发送者用户名 */
    private String senderName;
    
    /** 发送者头像 */
    private String senderAvatar;
    
    /** 接收者ID */
    private Integer receiverId;
    
    /** 接收者用户名 */
    private String receiverName;
    
    /** 接收者头像 */
    private String receiverAvatar;
    
    /** 消息内容 */
    private String content;
    
    /** 发送时间 */
    private LocalDateTime sendTime;
    
    /** 阅读状态：0-未读，1-已读 */
    private Integer readStatus;
} 