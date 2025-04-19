package com.animesocial.platform.controller;

import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.UpdateUserInfoRequest;
import com.animesocial.platform.model.dto.UserDetailResponse;
import com.animesocial.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * 用户控制器
 * 处理与用户相关的所有HTTP请求，包括：
 * 1. 用户信息管理
 * 2. 用户内容查询
 * 3. 用户关系管理
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;

    /**
     * 获取用户详情和统计数据
     * @param id 用户ID
     * @return 用户详情响应对象
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> getUserDetail(@PathVariable Integer id) {
        try {
            return ApiResponse.success(userService.getUserDetail(id));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取用户的帖子列表
     * @param id 用户ID
     * @return 帖子列表
     */
    @GetMapping("/{id}/posts")
    public ApiResponse<List<Post>> getUserPosts(@PathVariable Integer id) {
        try {
            return ApiResponse.success(userService.getUserPosts(id));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取用户的收藏列表
     * @param id 用户ID
     * @return 资源列表
     */
    @GetMapping("/{id}/favorites")
    public ApiResponse<List<Resource>> getUserFavorites(@PathVariable Integer id) {
        try {
            return ApiResponse.success(userService.getUserFavorites(id));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取用户的关注列表
     * @param id 用户ID
     * @return 用户列表
     */
    @GetMapping("/{id}/following")
    public ApiResponse<List<User>> getUserFollowing(@PathVariable Integer id) {
        try {
            return ApiResponse.success(userService.getUserFollowing(id));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取用户的粉丝列表
     * @param id 用户ID
     * @return 用户列表
     */
    @GetMapping("/{id}/followers")
    public ApiResponse<List<User>> getUserFollowers(@PathVariable Integer id) {
        try {
            return ApiResponse.success(userService.getUserFollowers(id));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param request 更新用户信息请求对象
     * @param session HTTP会话
     * @return 更新后的用户对象
     */
    @PutMapping("/{id}")
    public ApiResponse<User> updateUserInfo(@PathVariable Integer id, 
                             @RequestBody UpdateUserInfoRequest request,
                             HttpSession session) {
        // 检查用户是否登录
        Integer currentUserId = (Integer) session.getAttribute("userId");
        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }
        
        // 检查权限
        if (!currentUserId.equals(id)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success("信息更新成功", userService.updateUserInfo(id, request));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 关注/取消关注用户
     * @param id 被关注用户ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @PostMapping("/{id}/follow")
    public ApiResponse<String> followUser(@PathVariable Integer id, HttpSession session) {
        // 检查用户是否登录
        Integer currentUserId = (Integer) session.getAttribute("userId");
        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }
        
        // 不能关注自己
        if (currentUserId.equals(id)) {
            return ApiResponse.validateFailed("不能关注自己");
        }
        
        try {
            String result = userService.followUser(currentUserId, id);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员禁用/启用用户
     * @param id 用户ID
     * @param status 状态(0禁用,1正常)
     * @param session HTTP会话
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            HttpSession session) {
        // 检查管理员权限
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ApiResponse.forbidden();
        }
        
        if (status != 0 && status != 1) {
            return ApiResponse.validateFailed("状态值必须是0(禁用)或1(正常)");
        }
        
        try {
            userService.updateUserStatus(id, status);
            return ApiResponse.success(status == 1 ? "用户已启用" : "用户已禁用", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
} 