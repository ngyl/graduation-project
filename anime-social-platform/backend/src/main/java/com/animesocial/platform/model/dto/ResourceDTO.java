package com.animesocial.platform.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源数据传输对象
 * 用于向前端传输资源信息
 */
@Data
@NoArgsConstructor
public class ResourceDTO {
    /**
     * 资源ID
     */
    private Integer id;
    
    /**
     * 上传用户ID
     */
    private Integer userId;
    
    /**
     * 上传用户名
     */
    private String username;
    
    /**
     * 上传用户头像
     */
    private String userAvatar;
    
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
     * 文件大小(KB)
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
    
    /**
     * 资源标签
     */
    private List<TagDTO> tags;
    
    /**
     * 当前用户是否已收藏该资源
     */
    private Boolean isFavorited;
} 