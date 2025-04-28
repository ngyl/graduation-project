package com.animesocial.platform.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 活动DTO
 * 用于返回活动信息
 */
@Data
public class EventDTO {
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
     * 活动开始日期
     */
    private LocalDate startTime;
    
    /**
     * 活动结束日期
     */
    private LocalDate endTime;
    
    /**
     * 活动状态(0下线,1上线)
     */
    private Integer status;
    
    /**
     * 创建者ID
     */
    private Integer createdBy;
    
    /**
     * 创建者用户名
     */
    private String creatorName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 当前是否进行中
     * 根据当前时间计算
     */
    private Boolean isOngoing;
} 