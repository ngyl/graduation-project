package com.animesocial.platform.controller;

import com.animesocial.platform.model.Favorite;
import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.ResourceListResponse;
import com.animesocial.platform.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    /**
     * 添加收藏
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 结果
     */
    @PostMapping
    public ApiResponse<Favorite> addFavorite(@RequestParam Integer resourceId, @RequestParam Integer userId) {
        Favorite favorite = favoriteService.addFavorite(userId, resourceId);
        return ApiResponse.success("收藏成功", favorite);
    }
    
    /**
     * 取消收藏
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 结果
     */
    @DeleteMapping
    public ApiResponse<Boolean> removeFavorite(@RequestParam Integer resourceId, @RequestParam Integer userId) {
        boolean success = favoriteService.removeFavorite(userId, resourceId);
        if (success) {
            return ApiResponse.success("取消收藏成功", true);
        } else {
            return ApiResponse.failed("取消收藏失败");
        }
    }
    
    /**
     * 检查是否已收藏
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> checkFavorite(@RequestParam Integer resourceId, @RequestParam Integer userId) {
        boolean isFavorited = favoriteService.isFavorited(userId, resourceId);
        return ApiResponse.success(isFavorited);
    }
    
    /**
     * 获取用户收藏列表
     * @param userId 用户ID
     * @return 收藏列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<ResourceListResponse> getUserFavorites(@PathVariable Integer userId) {
        ResourceListResponse favorites = favoriteService.getUserFavorites(userId);
        
        return ApiResponse.success(favorites);
    }
    
    /**
     * 分页获取用户收藏列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 收藏列表和总数
     */
    @GetMapping("/user/{userId}/page")
    public ApiResponse<ResourceListResponse> getUserFavoritesWithPagination(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        ResourceListResponse favorites = favoriteService.getUserFavoritesWithPagination(userId, page, size);
        return ApiResponse.success(favorites);
    }
    
    /**
     * 获取资源收藏次数
     * @param resourceId 资源ID
     * @return 收藏次数
     */
    @GetMapping("/resource/{resourceId}/count")
    public ApiResponse<Integer> getResourceFavoriteCount(@PathVariable Integer resourceId) {
        int count = favoriteService.getResourceFavoriteCount(resourceId);
        return ApiResponse.success(count);
    }
} 