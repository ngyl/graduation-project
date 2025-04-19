package com.animesocial.platform.model;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 好友关系实体类
 * 
 * 表示两个用户之间的关注关系，包含：
 * 1. 关系ID
 * 2. 用户ID（关注者）
 * 3. 好友ID（被关注者）
 * 4. 关系状态：0-单向关注，1-互相关注
 * 5. 创建时间
 */
@Data
public class Friendship {
    /** 关系ID */
    private Integer id;
    
    /** 用户ID（关注者） */
    private Integer userId;
    
    /** 好友ID（被关注者） */
    private Integer friendId;
    
    /** 关系状态：0-单向关注，1-互相关注 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
} 