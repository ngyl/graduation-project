package com.animesocial.platform.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计数据DTO
 * 包含用户的帖子数量、收藏数量、关注数量、粉丝数量等统计信息
 */
@Data
@NoArgsConstructor
public class UserStatsDTO {
    /**
     * 帖子数量
     */
    private Integer postCount;
    
    /**
     * 收藏数量
     */
    private Integer favoriteCount;
    
    /**
     * 关注数量
     */
    private Integer followingCount;
    
    /**
     * 粉丝数量
     */
    private Integer followerCount;
} 