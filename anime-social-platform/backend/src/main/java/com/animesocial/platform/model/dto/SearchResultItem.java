package com.animesocial.platform.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果项DTO
 * 统一表示不同类型的搜索结果（帖子、资源、用户）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultItem {
    
    /**
     * ID
     */
    private Integer id;
    
    /**
     * 结果类型 (post/resource/user)
     */
    private String type;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容摘要
     */
    private String content;
    
    /**
     * 详情页URL
     */
    private String url;
    
    /**
     * 作者/用户名
     */
    private String author;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 创建/上传时间
     */
    private LocalDateTime createdAt;
} 