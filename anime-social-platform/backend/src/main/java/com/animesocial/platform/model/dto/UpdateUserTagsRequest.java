package com.animesocial.platform.model.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户标签请求数据传输对象
 * 用于接收前端传来的用户标签更新请求
 */
@Data
@NoArgsConstructor
public class UpdateUserTagsRequest {
    /**
     * 标签ID列表
     */
    private List<Integer> tagIds;
} 