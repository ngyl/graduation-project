package com.animesocial.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    
    /**
     * 收藏ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 资源ID
     */
    private Integer resourceId;
    
    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;
} 