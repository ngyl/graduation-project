package com.animesocial.platform.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 用户详情响应DTO
 * 用于返回用户详细信息和相关统计数据
 */
@Data
public class UserDetailResponse {
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
    private Integer favoriteCount;
    private Integer followingCount;
    private Integer followerCount;
    
    // 标签信息
    private List<TagDTO> tags;
    
    // 关系状态
    private Boolean isFollowing;  // 当前登录用户是否已关注该用户
} 