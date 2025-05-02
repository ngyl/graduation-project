package com.animesocial.platform.service;

import java.util.List;
import java.util.Map;

import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.UserTag;

/**
 * 用户标签服务接口
 * 处理用户与标签关联相关的业务逻辑
 */
public interface UserTagService {
    
    /**
     * 添加用户标签
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 是否添加成功
     */
    boolean addUserTag(Integer userId, Integer tagId);
    
    /**
     * 批量更新用户标签
     * 
     * @param userId 用户ID
     * @param tagIds 标签ID列表
     * @return 更新后的用户标签列表
     */
    List<Tag> updateUserTags(Integer userId, List<Integer> tagIds);
    
    /**
     * 按标签类型批量更新用户标签
     * 
     * @param userId 用户ID
     * @param tagIds 标签ID列表
     * @param tagType 标签类型(post/resource/user)
     * @return 更新后的用户标签列表
     */
    List<Tag> updateUserTagsByType(Integer userId, List<Integer> tagIds, String tagType);
    
    /**
     * 移除用户标签
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 是否移除成功
     */
    boolean removeUserTag(Integer userId, Integer tagId);
    
    /**
     * 获取用户标签列表
     * 
     * @param userId 用户ID
     * @return 用户标签列表
     */
    List<Tag> getUserTags(Integer userId);
    
    /**
     * 获取用户标签详情列表（包含关联信息）
     * 
     * @param userId 用户ID
     * @return 用户标签详情列表
     */
    List<UserTag> getUserTagDetails(Integer userId);
    
    /**
     * 检查用户是否拥有某标签
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 是否拥有该标签
     */
    boolean hasTag(Integer userId, Integer tagId);
    
    /**
     * 获取热门用户标签
     * 
     * @param limit 数量限制
     * @return 热门标签列表及其使用数量
     */
    List<Map<String, Object>> getHotUserTags(Integer limit);
    
    /**
     * 获取标签相似度用户推荐
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 推荐的用户ID列表
     */
    List<Integer> getSimilarUsers(Integer userId, Integer limit);
    
    /**
     * 根据用户标签推荐内容
     * 
     * @param userId 用户ID
     * @param contentType 内容类型(post/resource)
     * @param limit 数量限制
     * @return 推荐的内容ID列表
     */
    List<Integer> getRecommendedContent(Integer userId, String contentType, Integer limit);
} 