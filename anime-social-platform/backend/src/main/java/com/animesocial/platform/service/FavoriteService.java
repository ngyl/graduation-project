package com.animesocial.platform.service;

import com.animesocial.platform.model.Favorite;
import com.animesocial.platform.model.dto.ResourceListResponse;

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
     * @return 收藏对象
     */
    Favorite addFavorite(Integer userId, Integer resourceId);
    
    /**
     * 取消收藏
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否成功
     */
    boolean removeFavorite(Integer userId, Integer resourceId);
    
    /**
     * 判断用户是否已收藏资源
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否已收藏
     */
    boolean isFavorited(Integer userId, Integer resourceId);
    
    /**
     * 获取用户收藏列表
     * 
     * @param userId 用户ID
     * @return 收藏DTO列表
     */
    ResourceListResponse getUserFavorites(Integer userId);
    
    /**
     * 分页获取用户收藏列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 包含收藏列表和总数的Map
     */
    ResourceListResponse getUserFavoritesWithPagination(Integer userId, Integer page, Integer size);
    
    /**
     * 获取资源被收藏次数
     * 
     * @param resourceId 资源ID
     * @return 收藏次数
     */
    int getResourceFavoriteCount(Integer resourceId);
    
    /**
     * 获取用户收藏数量
     * 
     * @param userId 用户ID
     * @return 收藏数量
     */
    int getUserFavoriteCount(Integer userId);
} 