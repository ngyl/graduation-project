package com.animesocial.platform.model.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建活动的请求DTO
 */
@Data
public class CreateEventRequest {
    /**
     * 活动标题
     */
    @NotBlank(message = "活动标题不能为空")
    @Size(max = 100, message = "活动标题不能超过100个字符")
    private String title;
    
    /**
     * 活动描述
     */
    @Size(max = 1000, message = "活动描述不能超过1000个字符")
    private String description;
    
    /**
     * 活动开始时间
     */
    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;
} 