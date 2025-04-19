package com.animesocial.platform.service.impl;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Comment;
import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.CommentDTO;
import com.animesocial.platform.model.dto.CreateCommentRequest;
import com.animesocial.platform.repository.CommentRepository;
import com.animesocial.platform.repository.PostRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.CommentService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 根据ID获取评论
     */
    @Override
    public CommentDTO getCommentById(Integer id) {
        Comment comment = commentRepository.findById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        return convertToDTO(comment);
    }

    /**
     * 根据帖子ID获取评论列表
     */
    @Override
    public List<CommentDTO> getCommentsByPostId(Integer postId) {
        // 验证帖子是否存在
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        // 获取一级评论（不包括回复）
        List<Comment> comments = commentRepository.findByPostId(postId, null);
        List<CommentDTO> dtoList = comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 获取每个评论的回复
        for (CommentDTO dto : dtoList) {
            List<Comment> replies = commentRepository.findByPostId(postId, dto.getId());
            List<CommentDTO> replyDtos = replies.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            dto.setReplies(replyDtos);
        }
        
        return dtoList;
    }

    /**
     * 获取用户的评论列表
     */
    @Override
    public List<CommentDTO> getCommentsByUserId(Integer userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        List<Comment> comments = commentRepository.findByUserId(userId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建评论
     */
    @Override
    @Transactional
    public CommentDTO createComment(Integer userId, CreateCommentRequest request) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证帖子是否存在
        Post post = postRepository.findById(request.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        // 验证父评论（如果存在）
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId());
            if (parent == null) {
                throw new BusinessException("回复的评论不存在");
            }
            
            // 确保父评论属于同一个帖子
            if (!parent.getPostId().equals(request.getPostId())) {
                throw new BusinessException("回复的评论不属于该帖子");
            }
        }
        
        // 创建评论
        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setParentId(request.getParentId());
        
        // 保存评论
        commentRepository.insert(comment);
        
        // 转换为DTO并返回
        CommentDTO dto = convertToDTO(comment);
        
        // 如果是回复评论，设置空的回复列表
        if (request.getParentId() != null) {
            dto.setReplies(new ArrayList<>());
        }
        
        return dto;
    }

    /**
     * 删除评论
     */
    @Override
    @Transactional
    public void deleteComment(Integer id) {
        // 验证评论是否存在
        Comment comment = commentRepository.findById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 如果是父评论，同时删除其所有回复
        if (comment.getParentId() == null) {
            List<Comment> replies = commentRepository.findByParentId(id);
            for (Comment reply : replies) {
                commentRepository.delete(reply.getId());
            }
        }
        
        // 删除评论
        commentRepository.delete(id);
    }

    /**
     * 获取帖子的评论数量
     */
    @Override
    public Integer getCommentCountByPostId(Integer postId) {
        return commentRepository.countByPostId(postId);
    }

    /**
     * 检查是否是回复评论
     */
    @Override
    public boolean isReplyComment(Integer id) {
        Comment comment = commentRepository.findById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        return comment.getParentId() != null;
    }

    /**
     * 获取评论的回复列表
     */
    @Override
    public List<CommentDTO> getRepliesByCommentId(Integer commentId) {
        // 验证评论是否存在
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 只有父评论才能有回复
        if (comment.getParentId() != null) {
            throw new BusinessException("只能获取父评论的回复");
        }
        
        List<Comment> replies = commentRepository.findByParentId(commentId);
        return replies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将Comment实体转换为CommentDTO
     */
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);
        
        // 加载用户信息
        User user = userRepository.findById(comment.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }
        
        return dto;
    }
} 