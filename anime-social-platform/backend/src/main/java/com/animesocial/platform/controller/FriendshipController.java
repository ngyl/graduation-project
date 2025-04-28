package com.animesocial.platform.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.service.FriendshipService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 好友关系控制器
 * 处理用户关注、粉丝等社交关系的接口
 */
@RestController
@RequestMapping("/api/friendships")
@Slf4j
public class FriendshipController {
    
    @Autowired
    private FriendshipService friendshipService;
    
    /**
     * 关注用户
     * 
     * @param friendId 要关注的用户ID
     * @param session HTTP会话，用于获取当前登录用户
     * @return 操作结果
     */
    @PostMapping("/follow/{friendId}")
    public ApiResponse<String> followUser(
            @PathVariable Integer friendId,
            HttpSession session) {
        
        Integer currentUserId = (Integer) session.getAttribute("userId");
        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            String msg = friendshipService.follow(currentUserId, friendId);
            return ApiResponse.success(msg);
        } catch (BusinessException e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 取消关注用户
     * 
     * @param friendId 要取消关注的用户ID
     * @param session HTTP会话，用于获取当前登录用户
     * @return 操作结果
     */
    @DeleteMapping("/unfollow/{friendId}")
    public ApiResponse<String> unfollowUser(
            @PathVariable Integer friendId,
            HttpSession session) {
        
        Integer currentUserId = (Integer) session.getAttribute("userId");
        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            String msg = friendshipService.unfollow(currentUserId, friendId);
            return ApiResponse.success(msg);
        } catch (BusinessException e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取用户关注的人列表
     * 
     * @param userId 用户ID
     * @param page 页码（可选）
     * @param size 每页数量（可选）
     * @return 关注的用户列表
     */
    @GetMapping("/following/{userId}")
    public ApiResponse<List<UserDTO>> getFollowings(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        List<UserDTO> followings;
        try {
            followings = friendshipService.getFollowing(userId, page, size);
            return ApiResponse.success(followings);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取用户的粉丝列表
     * 
     * @param userId 用户ID
     * @param page 页码（可选）
     * @param size 每页数量（可选）
     * @return 粉丝用户列表
     */
    @GetMapping("/followers/{userId}")
    public ApiResponse<List<UserDTO>> getFollowers(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        List<UserDTO> followers;
        try {
            followers = friendshipService.getFollowers(userId, page, size);
            return ApiResponse.success(followers);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 检查与指定用户的关注状态
     * 
     * @param friendId 要检查关系的用户ID
     * @param session HTTP会话，用于获取当前登录用户
     * @return 关注状态信息
     */
    @GetMapping("/status/{friendId}")
    public ApiResponse<Map<String, Boolean>> checkFriendshipStatus(
            @PathVariable Integer friendId,
            HttpSession session) {
        
        Integer currentUserId = (Integer) session.getAttribute("userId");
        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            Map<String, Boolean> status = new HashMap<>();
            
            // 当前用户是否关注目标用户
            status.put("isFollowing", friendshipService.isFollowing(currentUserId, friendId));
            
            // 目标用户是否关注当前用户
            status.put("isFollower", friendshipService.isFollowing(friendId, currentUserId));
            
            // 是否互相关注
            status.put("isMutual", friendshipService.isMutualFollow(currentUserId, friendId));
            
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取用户的关注数量和粉丝数量
     * 
     * @param userId 用户ID
     * @return 关注数和粉丝数
     */
    @GetMapping("/counts/{userId}")
    public ApiResponse<Map<String, Integer>> getFriendshipCounts(
            @PathVariable Integer userId) {
        
        try {
            Map<String, Integer> counts = new HashMap<>();
            counts.put("following", friendshipService.countFollowing(userId));
            counts.put("followers", friendshipService.countFollowers(userId));
            
            return ApiResponse.success(counts);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
} 