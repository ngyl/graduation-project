package com.animesocial.platform.service;

import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.dto.ResourceDTO;

import java.util.List;

/**
 * 收藏服务接口
 * 处理用户收藏资源相关的业务逻辑
 */
public interface FavoriteService {
    
    /**
     * 添加收藏
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 操作结果描述
     * @throws RuntimeException 如果用户或资源不存在，或已收藏
     */
    String addFavorite(Integer userId, Integer resourceId);
    
    /**
     * 取消收藏
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 操作结果描述
     * @throws RuntimeException 如果用户或资源不存在，或未收藏
     */
    String removeFavorite(Integer userId, Integer resourceId);
    
    /**
     * 检查用户是否已收藏资源
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return true表示已收藏，false表示未收藏
     */
    boolean isFavorite(Integer userId, Integer resourceId);
    
    /**
     * 获取用户的收藏列表
     * 
     * @param userId 用户ID
     * @return 资源列表
     */
    List<ResourceDTO> getUserFavorites(Integer userId);
    
    /**
     * 分页获取用户的收藏列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 资源列表
     */
    List<ResourceDTO> getUserFavorites(Integer userId, Integer page, Integer size);
    
    /**
     * 获取用户的收藏数量
     * 
     * @param userId 用户ID
     * @return 收藏数量
     */
    int countUserFavorites(Integer userId);
} 