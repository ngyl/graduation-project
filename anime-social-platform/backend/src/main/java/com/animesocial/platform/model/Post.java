package com.animesocial.platform.model;

import lombok.Data;

import com.animesocial.platform.listener.PostEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

/**
 * 帖子实体类
 * 对应数据库中的posts表
 */
@Data
@Entity
@Table(name = "posts")
@EntityListeners(PostEntityListener.class)
public class Post {
    /**
     * 帖子ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    /**
     * 发帖用户ID
     */
    @Column(name = "user_id")
    private Integer userId;
    
    /**
     * 帖子标题
     */
    @Column
    private String title;
    
    /**
     * 帖子内容
     */
    @Column
    private String content;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 浏览次数
     */
    @Column(name = "view_count")
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    @Column(name = "like_count")
    private Integer likeCount;
    
    /**
     * 是否置顶
     */
    @Column(name = "is_top")
    private Boolean isTop;
} 