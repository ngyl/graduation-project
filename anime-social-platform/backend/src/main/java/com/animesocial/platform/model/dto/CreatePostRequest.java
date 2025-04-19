package com.animesocial.platform.model.dto;

import java.util.List;
import lombok.Data;

/**
 * 创建帖子请求数据传输对象
 * 用于接收前端创建帖子时的请求数据
 */
@Data
public class CreatePostRequest {
    /**
     * 帖子标题
     */
    private String title;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 标签ID列表
     */
    private List<Integer> tagIds;
} 