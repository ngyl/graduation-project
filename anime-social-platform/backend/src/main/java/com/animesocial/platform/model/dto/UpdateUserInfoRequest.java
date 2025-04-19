package com.animesocial.platform.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 更新用户信息请求数据传输对象
 */
@Data
public class UpdateUserInfoRequest {
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 用户选择的兴趣标签ID列表
     */
    private List<Integer> tagIds;
} 