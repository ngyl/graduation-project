package com.animesocial.platform.service;

import com.animesocial.platform.model.Tag;
import java.util.List;

/**
 * 用户标签服务接口
 */
public interface UserTagService {
    
    /**
     * 用户添加标签
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 操作结果信息
     */
    String addTag(Integer userId, Integer tagId);
    
    /**
     * 用户批量添加标签
     * @param userId 用户ID
     * @param tagIds 标签ID列表
     * @return 操作结果信息
     */
    String addTags(Integer userId, List<Integer> tagIds);
    
    /**
     * 用户移除标签
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 操作结果信息
     */
    String removeTag(Integer userId, Integer tagId);
    
    /**
     * 移除用户的所有标签
     * @param userId 用户ID
     * @return 操作结果信息
     */
    String removeAllTags(Integer userId);
    
    /**
     * 获取用户的所有标签
     * @param userId 用户ID
     * @return 标签列表
     */
    List<Tag> getUserTags(Integer userId);
    
    /**
     * 检查用户是否已有标签
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 是否已有标签
     */
    boolean hasTag(Integer userId, Integer tagId);
    
    /**
     * 获取热门用户标签
     * @param limit 限制数量
     * @return 热门标签列表
     */
    List<Tag> getHotUserTags(Integer limit);
    
    /**
     * 获取拥有相同标签的用户ID列表
     * @param tagId 标签ID
     * @return 用户ID列表
     */
    List<Integer> getUsersWithTag(Integer tagId);
} 