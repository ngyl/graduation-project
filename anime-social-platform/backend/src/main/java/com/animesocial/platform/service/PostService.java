package com.animesocial.platform.service;

import java.util.List;
import java.util.Map;

import com.animesocial.platform.model.dto.CreatePostRequest;
import com.animesocial.platform.model.dto.PostDTO;
import com.animesocial.platform.model.dto.PostListResponse;

/**
 * 帖子服务接口
 * 定义帖子相关的业务逻辑方法
 */
public interface PostService {
    
    /**
     * 根据ID获取帖子
     * @param id 帖子ID
     * @return 帖子详情
     */
    PostDTO getPostById(Integer id);
    
    /**
     * 分页获取用户的帖子列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 帖子列表
     */
    List<PostDTO> getPostsByUserId(Integer userId, Integer page, Integer size);
    
    /**
     * 分页获取帖子列表
     * @param page 页码
     * @param size 每页数量
     * @param tagId 标签ID（可选）
     * @param sort 排序方式（latest/views/likes）
     * @return 帖子列表和总数
     */
    PostListResponse getAllPosts(Integer page, Integer size, Integer tagId, String sort);
    
    /**
     * 创建帖子
     * @param userId 用户ID
     * @param request 创建帖子请求
     * @return 创建的帖子
     */
    PostDTO createPost(Integer userId, CreatePostRequest request);
    
    /**
     * 更新帖子
     * @param id 帖子ID
     * @param request 更新帖子请求
     * @return 更新后的帖子
     */
    PostDTO updatePost(Integer id, CreatePostRequest request);
    
    /**
     * 删除帖子
     * @param id 帖子ID
     */
    void deletePost(Integer id);
    
    /**
     * 点赞帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 操作结果描述
     * @throws RuntimeException 如果帖子不存在或已点赞
     */
    String likePost(Integer postId, Integer userId);
    
    /**
     * 取消点赞
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 操作结果描述
     * @throws RuntimeException 如果帖子不存在或未点赞
     */
    String unlikePost(Integer postId, Integer userId);
    
    /**
     * 检查用户是否已点赞帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return true表示已点赞，false表示未点赞
     */
    boolean isPostLiked(Integer postId, Integer userId);
    
    /**
     * 获取用户点赞的帖子列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 帖子列表和总数
     */
    PostListResponse getLikedPosts(Integer userId, Integer page, Integer size);
    
    /**
     * 增加帖子浏览量
     * @param postId 帖子ID
     * @throws RuntimeException 如果帖子不存在
     */
    void increaseViewCount(Integer postId);
    
    /**
     * 设置帖子置顶状态
     * @param postId 帖子ID
     * @param isTop 是否置顶(0不置顶，1置顶)
     * @throws RuntimeException 如果帖子不存在
     */
    void setPostTop(Integer postId, Integer isTop);
    
    /**
     * 获取置顶帖子列表
     * @return 置顶帖子列表
     */
    List<PostDTO> getTopPosts();
    
    /**
     * 搜索帖子
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 匹配的帖子列表和总数
     */
    PostListResponse searchPosts(String keyword, Integer page, Integer size);
    
    /**
     * 获取帖子总数
     * @return 帖子总数
     */
    int countPosts();
    
    /**
     * 根据ID列表批量查询帖子
     * @param ids 帖子ID列表
     * @return 帖子列表
     */
    List<PostDTO> findByIds(List<Integer> ids);
} 