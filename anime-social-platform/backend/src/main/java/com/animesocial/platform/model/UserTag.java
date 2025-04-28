package com.animesocial.platform.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户标签关联实体类
 * 用于表示用户与标签的关联关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTag {
    
    /**
     * 关联ID
     */
    private Integer id;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 标签ID
     */
    private Integer tagId;
    
    /**
     * 关联创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 标签名称 (非数据库字段，来自关联查询)
     */
    private String tagName;
    
    /**
     * 标签分类 (非数据库字段，来自关联查询)
     */
    private String tagCategory;
    
    /**
     * 标签类型 (非数据库字段，来自关联查询)
     */
    private String tagType;
} 