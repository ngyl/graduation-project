package com.animesocial.platform.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子数据传输对象
 */
@Data
public class PostDTO {
    // 基本信息
    private Integer id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计信息
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    
    // 关联信息
    private UserDTO user;
    private List<TagDTO> tags;
    
    // 状态信息
    private Boolean isTop;
    private Boolean isLiked;  // 当前用户是否点赞
} 