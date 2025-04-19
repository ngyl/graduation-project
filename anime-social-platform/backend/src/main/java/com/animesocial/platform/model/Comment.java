package com.animesocial.platform.model;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 评论实体类
 * 
 * 表示系统中的评论信息，包含：
 * 1. 基本信息：ID、内容等
 * 2. 关联信息：帖子ID、用户ID等
 * 3. 回复关系：父评论ID等
 * 4. 时间信息：创建时间等
 */
@Data
public class Comment {
    /**
     * 评论ID
     */
    private Integer id;
    
    /**
     * 帖子ID
     */
    private Integer postId;
    
    /**
     * 评论用户ID
     */
    private Integer userId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 父评论ID
     * 如果是对评论的回复，则此字段不为空
     */
    private Integer parentId;
} 