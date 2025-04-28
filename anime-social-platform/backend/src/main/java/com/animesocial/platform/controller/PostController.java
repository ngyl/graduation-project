package com.animesocial.platform.controller;

import com.animesocial.platform.model.dto.PostDTO;
import com.animesocial.platform.model.dto.PostListResponse;
import com.animesocial.platform.model.dto.CreatePostRequest;
import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 帖子控制器
 * 处理与帖子相关的所有HTTP请求，包括：
 * 1. 帖子的发布和管理
 * 2. 帖子的查询和筛选
 * 3. 帖子的点赞操作
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    @Autowired
    private PostService postService;

    /**
     * 获取指定ID的帖子详情
     * @param id 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/{id}")
    public ApiResponse<PostDTO> getPost(@PathVariable Integer id) {
        try {
            return ApiResponse.success(postService.getPostById(id));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取指定用户的所有帖子
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 帖子列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<PostDTO>> getUserPosts(@PathVariable Integer userId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        try {
            return ApiResponse.success(postService.getPostsByUserId(userId, page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 分页获取所有帖子
     * @param page 页码(默认1)
     * @param size 每页数量(默认10)
     * @param tagId 标签ID（可选）
     * @param sort 排序方式（可选，支持：latest/views/likes）
     * @return 帖子列表和总数
     */
    @GetMapping
    public ApiResponse<PostListResponse> getAllPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer tagId,
            @RequestParam(defaultValue = "latest") String sort) {
        try {
            return ApiResponse.success(postService.getAllPosts(page, size, tagId, sort));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 创建新帖子
     * @param request 创建帖子请求对象
     * @param session HTTP会话
     * @return 新创建的帖子对象
     */
    @PostMapping
    public ApiResponse<PostDTO> createPost(@Valid @RequestBody CreatePostRequest request, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            return ApiResponse.success("帖子发布成功", postService.createPost(userId, request));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 更新指定ID的帖子
     * @param id 帖子ID
     * @param request 更新帖子请求对象
     * @param session HTTP会话
     * @return 更新后的帖子对象
     */
    @PutMapping("/{id}")
    public ApiResponse<PostDTO> updatePost(@PathVariable Integer id, @Valid @RequestBody CreatePostRequest request, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            // 验证当前用户是否是帖子的作者或管理员
            PostDTO post = postService.getPostById(id);
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (!post.getUserDTO().getId().equals(userId) && (isAdmin == null || !isAdmin)) {
                return ApiResponse.forbidden();
            }
            
            return ApiResponse.success("帖子更新成功", postService.updatePost(id, request));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 删除指定ID的帖子
     * @param id 帖子ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Integer id, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            // 验证当前用户是否是帖子的作者或管理员
            PostDTO post = postService.getPostById(id);
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (!post.getUserDTO().getId().equals(userId) && (isAdmin == null || !isAdmin)) {
                return ApiResponse.forbidden();
            }
            
            postService.deletePost(id);
            return ApiResponse.success("帖子已删除", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 点赞帖子
     * @param id 帖子ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @PostMapping("/{id}/like")
    public ApiResponse<Void> likePost(@PathVariable Integer id, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            postService.likePost(id, userId);
            return ApiResponse.success("已点赞", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 取消点赞
     * @param id 帖子ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/{id}/like")
    public ApiResponse<Void> unlikePost(@PathVariable Integer id, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            postService.unlikePost(id, userId);
            return ApiResponse.success("已取消点赞", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员置顶/取消置顶帖子
     * @param id 帖子ID
     * @param isTop 是否置顶(0不置顶,1置顶)
     * @param session HTTP会话
     * @return 操作结果
     */
    @PutMapping("/{id}/top")
    public ApiResponse<Void> setPostTop(
            @PathVariable Integer id,
            @RequestParam Integer isTop,
            HttpSession session) {
        // 检查管理员权限
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ApiResponse.forbidden();
        }
        
        if (isTop != 0 && isTop != 1) {
            return ApiResponse.validateFailed("置顶值必须是0(不置顶)或1(置顶)");
        }
        
        try {
            postService.setPostTop(id, isTop);
            return ApiResponse.success(isTop == 1 ? "帖子已置顶" : "帖子已取消置顶", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
} 