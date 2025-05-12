package com.animesocial.platform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.service.TagService;
import com.animesocial.platform.service.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * 推荐控制器
 * 提供用户推荐功能，包括：
 * 1. 根据用户标签推荐相似用户
 * 2. 推荐热门标签
 * 3. 为用户推荐个性化标签
 */
@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    /**
     * 获取推荐用户列表（基于用户标签相似度）
     * 
     * @param session HTTP会话
     * @param limit 限制数量
     * @return 推荐用户列表
     */
    @GetMapping("/users")
    public ApiResponse<List<UserDTO>> getRecommendedUsers(
            HttpSession session,
            @RequestParam(defaultValue = "10") Integer limit) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            return ApiResponse.success(userService.getRecommendedUsers(userId, limit));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 为指定用户获取推荐用户列表
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐用户列表
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<List<UserDTO>> getRecommendedUsersForUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            return ApiResponse.success(userService.getRecommendedUsers(userId, limit));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取热门帖子标签
     * 
     * @param limit 限制数量
     * @return 热门标签列表
     */
    @GetMapping("/tags/posts")
    public ApiResponse<List<TagDTO>> getHotPostTags(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            return ApiResponse.success(tagService.getHotPostTags(limit));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取热门资源标签
     * 
     * @param limit 限制数量
     * @return 热门标签列表
     */
    @GetMapping("/tags/resources")
    public ApiResponse<List<TagDTO>> getHotResourceTags(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            return ApiResponse.success(tagService.getHotResourceTags(limit));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 为用户推荐标签（基于用户已有标签）
     * 
     * @param session HTTP会话
     * @param limit 限制数量
     * @return 推荐标签列表
     */
    @GetMapping("/tags/user")
    public ApiResponse<List<TagDTO>> getRecommendedTags(
            HttpSession session,
            @RequestParam(defaultValue = "10") Integer limit) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            return ApiResponse.success(tagService.getRecommendedTags(userId, limit));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 为指定用户推荐标签
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐标签列表
     */
    @GetMapping("/tags/user/{userId}")
    public ApiResponse<List<TagDTO>> getRecommendedTagsForUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            return ApiResponse.success(tagService.getRecommendedTags(userId, limit));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
}
