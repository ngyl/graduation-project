package com.animesocial.platform.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签数据传输对象
 * 用于向前端传输标签信息
 */
@Data
@NoArgsConstructor
public class TagDTO {
    /**
     * 标签ID
     */
    private Integer id;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 标签分类
     */
    private String category;
    
    /**
     * 标签类型(post/resource/user)
     */
    private String type;
    
    /**
     * 使用该标签的用户数量
     */
    private Integer userCount;
    
    /**
     * 使用该标签的内容数量
     */
    private Integer contentCount;
    
    /**
     * 是否为用户已选标签
     */
    private Boolean isSelected;
} 