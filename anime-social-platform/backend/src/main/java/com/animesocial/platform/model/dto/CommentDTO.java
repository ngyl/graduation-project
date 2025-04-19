package com.animesocial.platform.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 评论DTO
 * 用于返回评论信息
 */
@Data
public class CommentDTO {
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
     * 评论用户名
     */
    private String username;
    
    /**
     * 评论用户头像
     */
    private String userAvatar;
    
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
    
    /**
     * 子评论/回复列表
     */
    private List<CommentDTO> replies;
} 