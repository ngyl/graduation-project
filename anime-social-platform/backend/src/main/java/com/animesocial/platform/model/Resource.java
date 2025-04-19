package com.animesocial.platform.model;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 资源实体类
 * 
 * 表示系统中的资源信息，包含：
 * 1. 基本信息：ID、标题、描述等
 * 2. 文件信息：文件URL、类型、大小等
 * 3. 统计信息：下载次数、收藏次数等
 * 4. 上传信息：上传用户、上传时间等
 */
@Data
public class Resource {
    /**
     * 资源ID
     */
    private Integer id;
    
    /**
     * 上传用户ID
     */
    private Integer userId;
    
    /**
     * 资源标题
     */
    private String title;
    
    /**
     * 资源描述
     */
    private String description;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件大小（KB）
     */
    private Integer fileSize;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 收藏次数
     */
    private Integer likeCount;
} 