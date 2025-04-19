package com.animesocial.platform.model;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 私信实体类
 * 
 * 表示两个用户之间的私信消息，包含：
 * 1. 消息ID和内容信息
 * 2. 发送者和接收者信息
 * 3. 时间和状态信息
 */
@Data
public class Message {
    /** 消息ID */
    private Integer id;
    
    /** 发送者ID */
    private Integer senderId;
    
    /** 接收者ID */
    private Integer receiverId;
    
    /** 消息内容 */
    private String content;
    
    /** 发送时间 */
    private LocalDateTime sendTime;
    
    /** 阅读状态：0-未读，1-已读 */
    private Integer readStatus;
    
    /** 发送者信息（非数据库字段，查询时填充） */
    private User sender;
    
    /** 接收者信息（非数据库字段，查询时填充） */
    private User receiver;
} 