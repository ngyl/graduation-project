package com.animesocial.platform.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.animesocial.platform.listener.ResourceEntityListener;

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
@Entity
@Table(name = "resources")
@EntityListeners(ResourceEntityListener.class)
public class Resource {
    /**
     * 资源ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    /**
     * 上传用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    /**
     * 资源标题
     */
    @Column(nullable = false)
    private String title;
    
    /**
     * 资源描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 文件URL
     */
    @Column(name = "file_url", nullable = false)
    private String fileUrl;
    
    /**
     * 封面图片URL（可选）
     */
    @Column(name = "cover_url")
    private String coverUrl;
    
    /**
     * 文件类型
     */
    @Column(name = "file_type", nullable = false)
    private String fileType;
    
    /**
     * 文件大小（KB）
     */
    @Column(name = "file_size")
    private Integer fileSize;
    
    /**
     * 上传时间
     */
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
    
    /**
     * 下载次数
     */
    @Column(name = "download_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer downloadCount;
    
    /**
     * 收藏次数
     */
    @Column(name = "like_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer likeCount;
} 