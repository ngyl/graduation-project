package com.animesocial.platform.model.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户标签数据传输对象
 * 用于向前端传输用户标签信息
 */
@Data
@NoArgsConstructor
public class UserTagsDTO {
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 标签列表
     */
    private List<TagDTO> tags;
    
    /**
     * 标签数量
     */
    private Integer tagCount;
} 