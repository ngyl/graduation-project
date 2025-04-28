package com.animesocial.platform.model;

import lombok.Data;

/**
 * 标签实体类
 */
@Data
public class Tag {
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
     * 标签类型(post/resource)
     */
    private String type;
} 