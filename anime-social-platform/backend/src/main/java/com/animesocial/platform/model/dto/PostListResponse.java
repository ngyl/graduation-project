package com.animesocial.platform.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 帖子列表响应对象
 */
@Data
public class PostListResponse {
    private List<PostDTO> items;
    private Integer total;
    
    public PostListResponse(List<PostDTO> items, Integer total) {
        this.items = items;
        this.total = total;
    }
} 