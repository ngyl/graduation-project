package com.animesocial.platform.model;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 活动实体类
 * 
 * 表示系统中的活动信息，包含：
 * 1. 基本信息：ID、标题、描述等
 * 2. 时间信息：开始时间、结束时间等
 * 3. 状态信息：活动状态等
 * 4. 创建信息：创建者、创建时间等
 */
@Data
public class Event {
    /**
     * 活动ID
     */
    private Integer id;
    
    /**
     * 活动标题
     */
    private String title;
    
    /**
     * 活动描述
     */
    private String description;
    
    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 活动状态(0下线,1上线)
     */
    private Integer status;
    
    /**
     * 创建者ID
     */
    private Integer createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 