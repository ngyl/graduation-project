package com.animesocial.platform.model.dto;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 用户信息DTO
 * 整合了用户基本信息、统计信息和扩展信息
 */
@Data
public class UserDTO {
    // 基本信息
    private Integer id;
    private String username;
    private String avatar;
    private String bio;
    private Boolean isAdmin;
    private Integer status;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    
    // 统计信息
    private Integer postCount;
    private Integer followingCount;
    private Integer followerCount;
    private Integer favoriteCount;
    
    // 扩展信息（按需加载）
    private List<TagDTO> tags;
    private List<PostDTO> recentPosts;
    
    // 用于前端展示的辅助字段
    private Boolean isFollowing;  // 当前登录用户是否关注了该用户
} 