package com.animesocial.platform.service;

import com.animesocial.platform.model.dto.CommentDTO;
import com.animesocial.platform.model.dto.CreateCommentRequest;

import java.util.List;

/**
 * 评论服务接口
 * 定义评论相关的业务逻辑方法
 */
public interface CommentService {
    
    /**
     * 根据ID获取评论
     * @param id 评论ID
     * @return 评论详情
     */
    CommentDTO getCommentById(Integer id);
    
    /**
     * 根据帖子ID获取评论列表
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<CommentDTO> getCommentsByPostId(Integer postId);
    
    /**
     * 获取用户的评论列表
     * @param userId 用户ID
     * @return 评论列表
     */
    List<CommentDTO> getCommentsByUserId(Integer userId);
    
    /**
     * 创建评论
     * @param userId 用户ID
     * @param request 创建评论请求
     * @return 创建的评论
     */
    CommentDTO createComment(Integer userId, CreateCommentRequest request);
    
    /**
     * 删除评论
     * @param id 评论ID
     */
    void deleteComment(Integer id);
    
    /**
     * 获取帖子的评论数量
     * @param postId 帖子ID
     * @return 评论数量
     */
    Integer getCommentCountByPostId(Integer postId);
    
    /**
     * 检查是否是回复评论
     * @param id 评论ID
     * @return true表示是回复，false表示不是回复
     */
    boolean isReplyComment(Integer id);
    
    /**
     * 获取评论的回复列表
     * @param commentId 评论ID
     * @return 回复列表
     */
    List<CommentDTO> getRepliesByCommentId(Integer commentId);
} 