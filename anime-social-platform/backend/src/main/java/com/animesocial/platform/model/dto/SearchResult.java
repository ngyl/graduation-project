package com.animesocial.platform.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果DTO
 * 包含不同类型的搜索结果和统计信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    
    /**
     * 搜索结果总数
     */
    private Integer totalResults;
    
    /**
     * 帖子搜索结果
     */
    private List<SearchResultItem> posts;
    
    /**
     * 帖子结果总数
     */
    private Integer totalPosts;
    
    /**
     * 资源搜索结果
     */
    private List<SearchResultItem> resources;
    
    /**
     * 资源结果总数
     */
    private Integer totalResources;
    
    /**
     * 用户搜索结果
     */
    private List<SearchResultItem> users;
    
    /**
     * 用户结果总数
     */
    private Integer totalUsers;
} 