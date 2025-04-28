package com.animesocial.platform.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动参与实体类
 * 用于记录用户参加活动的信息
 */
@Data
public class EventParticipation {
    
    /**
     * 参与记录ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 活动ID
     */
    private Integer eventId;
    
    /**
     * 参与时间
     */
    private LocalDateTime participationTime;
    
    /**
     * 参与状态(0已取消，1已报名)
     */
    private Integer status;
} 