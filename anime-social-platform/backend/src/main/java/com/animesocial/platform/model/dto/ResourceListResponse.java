package com.animesocial.platform.model.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 资源列表响应对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceListResponse {
    /**
     * 资源列表
     */
    private List<ResourceDTO> items;
    
    /**
     * 总数
     */
    private Integer total;
} 