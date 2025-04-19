package com.animesocial.platform.controller;

import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.CommentDTO;
import com.animesocial.platform.model.dto.CreateCommentRequest;
import com.animesocial.platform.service.CommentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 评论控制器
 * 处理与评论相关的所有HTTP请求，包括：
 * 1. 添加评论
 * 2. 获取评论列表
 * 3. 删除评论
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 根据帖子ID获取评论列表
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/post/{postId}")
    public ApiResponse<List<CommentDTO>> getCommentsByPost(@PathVariable Integer postId) {
        try {
            return ApiResponse.success(commentService.getCommentsByPostId(postId));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 添加评论
     * @param request 评论请求对象
     * @param session HTTP会话
     * @return 新创建的评论对象
     */
    @PostMapping
    public ApiResponse<CommentDTO> addComment(@Valid @RequestBody CreateCommentRequest request, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            CommentDTO comment = commentService.createComment(userId, request);
            return ApiResponse.success("评论发布成功", comment);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 删除评论
     * @param id 评论ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Integer id, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            // 验证当前用户是否是评论的作者或管理员
            CommentDTO comment = commentService.getCommentById(id);
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (!comment.getUserId().equals(userId) && (isAdmin == null || !isAdmin)) {
                return ApiResponse.forbidden();
            }
            
            commentService.deleteComment(id);
            return ApiResponse.success("评论已删除", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取用户的所有评论
     * @param userId 用户ID
     * @return 评论列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<CommentDTO>> getUserComments(@PathVariable Integer userId) {
        try {
            return ApiResponse.success(commentService.getCommentsByUserId(userId));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
} 