package com.animesocial.platform.model.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 活动参与数据传输对象
 * 用于前后端交互，包含用户和活动的基本信息
 */
@Data
public class EventParticipationDTO {
    
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
     * 用户名称
     */
    private String username;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 活动标题
     */
    private String eventTitle;
    
    /**
     * 活动开始时间
     */
    private LocalDate eventStartTime;
    
    /**
     * 活动结束时间
     */
    private LocalDate eventEndTime;
    
    /**
     * 参与时间
     */
    private LocalDateTime participationTime;
    
    /**
     * 参与状态(0已取消，1已报名)
     */
    private Integer status;
    
    /**
     * 是否为当前正在进行的活动
     */
    private Boolean isOngoing;
} 