package com.animesocial.platform.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 帖子实体类
 * 对应数据库中的posts表
 */
@Data
public class Post {
    /**
     * 帖子ID
     */
    private Integer id;
    
    /**
     * 发帖用户ID
     */
    private Integer userId;
    
    /**
     * 帖子标题
     */
    private String title;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 是否置顶
     */
    private Boolean isTop;
} 