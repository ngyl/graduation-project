package com.animesocial.platform.service.impl;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.PostDTO;
import com.animesocial.platform.model.dto.CreatePostRequest;
import com.animesocial.platform.model.dto.PostListResponse;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.repository.PostRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.repository.TagRepository;
import com.animesocial.platform.repository.PostLikeRepository;
import com.animesocial.platform.service.PostService;
import com.animesocial.platform.service.TagService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 * 实现帖子相关的业务逻辑
 */
@Service
public class PostServiceImpl implements PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private PostLikeRepository postLikeRepository;
    
    @Autowired
    private TagService tagService;

    /**
     * 根据ID获取帖子
     * @param id 帖子ID
     * @return 帖子对象
     * @throws BusinessException 帖子不存在时抛出异常
     */
    @Override
    public PostDTO getPostById(Integer id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        return convertToDTO(post);
    }

    /**
     * 获取指定用户的所有帖子
     * @param userId 用户ID
     * @return 帖子列表
     */
    @Override
    public List<PostDTO> getPostsByUserId(Integer userId) {
        return postRepository.findByUserId(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * 分页获取所有帖子
     * @param page 页码
     * @param size 每页数量
     * @param tagId 标签ID
     * @param sort 排序方式
     * @return 帖子列表和总数
     */
    @Override
    public PostListResponse getAllPosts(Integer page, Integer size, Integer tagId, String sort) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取帖子列表
        List<Post> posts;
        if (tagId != null) {
            posts = postRepository.findByTagId(tagId, offset, size, sort);
        } else {
            posts = postRepository.findAllWithSort(offset, size, sort);
        }
        
        // 获取总数
        int total = tagId != null ? 
            postRepository.countByTagId(tagId) : 
            postRepository.count();
        
        // 获取当前用户ID，用于检查是否点赞
        Integer currentUserId = getCurrentUserId();
        
        // 转换为DTO
        List<PostDTO> postDTOs = posts.stream()
            .map(post -> convertToDTO(post, currentUserId))
            .collect(Collectors.toList());
            
        return new PostListResponse(postDTOs, total);
    }

    /**
     * 创建新帖子
     * @param userId 用户ID
     * @param request 创建帖子请求对象
     * @return 新创建的帖子对象
     */
    @Override
    @Transactional
    public PostDTO createPost(Integer userId, CreatePostRequest request) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setIsTop(false);
        
        // 保存帖子
        postRepository.save(post);
        
        // 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            // 验证标签是否合法
            for (Integer tagId : request.getTagIds()) {
                TagDTO tag = tagService.getTagById(tagId);
                if (!"post".equals(tag.getType())) {
                    throw new BusinessException("标签类型错误，不能用于帖子: " + tag.getName());
                }
            }
            tagRepository.savePostTags(post.getId(), request.getTagIds());
        }
        
        return convertToDTO(post, userId);
    }

    /**
     * 更新帖子
     * @param id 帖子ID
     * @param request 更新帖子请求对象
     * @return 更新后的帖子对象
     * @throws BusinessException 帖子不存在时抛出异常
     */
    @Override
    @Transactional
    public PostDTO updatePost(Integer id, CreatePostRequest request) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
            
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdatedAt(LocalDateTime.now());
        
        // 更新帖子
        postRepository.update(post);
        
        // 更新标签关联
        if (request.getTagIds() != null) {
            tagRepository.deletePostTags(id);
            if (!request.getTagIds().isEmpty()) {
                // 验证标签是否合法
                for (Integer tagId : request.getTagIds()) {
                    TagDTO tag = tagService.getTagById(tagId);
                    if (!"post".equals(tag.getType())) {
                        throw new BusinessException("标签类型错误，不能用于帖子: " + tag.getName());
                    }
                }
                tagRepository.savePostTags(id, request.getTagIds());
            }
        }
        
        return convertToDTO(post, getCurrentUserId());
    }

    /**
     * 删除帖子
     * @param id 帖子ID
     * @throws BusinessException 帖子不存在时抛出异常
     */
    @Override
    @Transactional
    public void deletePost(Integer id) {
        // 检查帖子是否存在
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        // 删除标签关联
        tagRepository.deletePostTags(id);
        // 删除点赞记录
        postLikeRepository.deleteByPostId(id);
        // 删除帖子
        postRepository.deleteById(id);
    }

    /**
     * 点赞帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 操作结果描述
     */
    @Override
    @Transactional
    public String likePost(Integer postId, Integer userId) {
        // 检查帖子是否存在
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
            
        // 检查是否已点赞
        if (postLikeRepository.exists(postId, userId)) {
            throw new BusinessException("已经点赞过了");
        }
        
        // 保存点赞记录
        postLikeRepository.save(postId, userId);
        
        // 更新点赞数
        postRepository.increaseLikeCount(postId);
        
        return "点赞成功";
    }
    
    /**
     * 取消点赞帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 操作结果描述
     */
    @Override
    @Transactional
    public String unlikePost(Integer postId, Integer userId) {
        // 检查帖子是否存在
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
            
        // 检查是否已点赞
        if (!postLikeRepository.exists(postId, userId)) {
            throw new BusinessException("未点赞该帖子");
        }
        
        // 删除点赞记录
        postLikeRepository.delete(postId, userId);
        
        // 更新点赞数
        postRepository.decreaseLikeCount(postId);
        
        return "取消点赞成功";
    }
    
    /**
     * 检查用户是否已点赞帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    @Override
    public boolean isPostLiked(Integer postId, Integer userId) {
        return postLikeRepository.exists(postId, userId);
    }
    
    /**
     * 获取用户点赞的帖子列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 帖子列表和总数
     */
    @Override
    public PostListResponse getLikedPosts(Integer userId, Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取点赞的帖子
        List<Post> posts = postRepository.findLikedPostsByUserIdPaged(userId, offset, size);
        
        // 获取总数
        int total = postRepository.countLikedPostsByUserId(userId);
        
        // 转换为DTO
        List<PostDTO> postDTOs = posts.stream()
            .map(post -> convertToDTO(post, userId))
            .collect(Collectors.toList());
            
        return new PostListResponse(postDTOs, total);
    }
    
    /**
     * 增加帖子浏览量
     * @param postId 帖子ID
     */
    @Override
    public void increaseViewCount(Integer postId) {
        // 检查帖子是否存在
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        // 增加浏览量
        postRepository.increaseViewCount(postId);
    }

    /**
     * 将Post实体转换为PostDTO
     * @param post 帖子实体
     * @return 帖子DTO
     */
    private PostDTO convertToDTO(Post post) {
        return convertToDTO(post, getCurrentUserId());
    }
    
    /**
     * 将Post实体转换为PostDTO，并设置是否点赞
     * @param post 帖子实体
     * @param currentUserId 当前用户ID
     * @return 帖子DTO
     */
    private PostDTO convertToDTO(Post post, Integer currentUserId) {
        if (post == null) {
            return null;
        }
        
        PostDTO dto = new PostDTO();
        BeanUtils.copyProperties(post, dto);
        
        // 设置用户信息
        User user = userRepository.findById(post.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        
        // 隐藏敏感信息
        // 注意：不直接调用setPassword方法，而是将敏感字段设置为null
        BeanUtils.copyProperties(new UserDTO(), userDTO, "id", "username", "avatar", "bio", "isAdmin", "status", "registerTime", "lastLoginTime", "postCount", "followingCount", "followerCount", "favoriteCount", "tags", "recentPosts", "isFollowing");
        
        dto.setUser(userDTO);
            
        // 设置标签信息
        List<TagDTO> tags = tagService.getPostTags(post.getId());
        dto.setTags(tags != null ? tags : new ArrayList<>());
        
        // 设置是否点赞
        if (currentUserId != null) {
            dto.setIsLiked(postLikeRepository.exists(post.getId(), currentUserId));
        } else {
            dto.setIsLiked(false);
        }
        
        return dto;
    }
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID，如果未登录则返回null
     */
    private Integer getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    return user.getId();
                }
            }
        }
        return null;
    }

    /**
     * 置顶帖子
     */
    @Override
    @Transactional
    public void setPostTop(Integer postId, Integer isTop) {
        // 验证帖子是否存在
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        // 验证置顶状态值
        if (isTop != 0 && isTop != 1) {
            throw new BusinessException("置顶状态值必须是0(不置顶)或1(置顶)");
        }
        
        // 更新置顶状态
        postRepository.updateTopStatus(postId, isTop);
    }

    /**
     * 获取置顶帖子列表
     */
    @Override
    public List<PostDTO> getTopPosts() {
        List<Post> posts = postRepository.findTopPosts();
        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
} 