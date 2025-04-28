package com.animesocial.platform.service.impl;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.UserTag;
import com.animesocial.platform.repository.TagRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.repository.UserTagRepository;
import com.animesocial.platform.service.UserTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户标签服务实现类
 */
@Service
@Slf4j
public class UserTagServiceImpl implements UserTagService {

    @Autowired
    private UserTagRepository userTagRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public boolean addUserTag(Integer userId, Integer tagId) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查标签是否存在
        Tag tag = tagRepository.findById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 检查是否已存在关联
        if (userTagRepository.exists(userId, tagId) > 0) {
            return true; // 已存在关联，视为成功
        }
        
        // 添加用户标签关联
        userTagRepository.save(userId, tagId);
        return true;
    }

    @Override
    @Transactional
    public List<Tag> updateUserTags(Integer userId, List<Integer> tagIds) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果标签ID列表为空，则移除所有标签
        if (tagIds == null || tagIds.isEmpty()) {
            userTagRepository.deleteByUserId(userId);
            return new ArrayList<>();
        }
        
        // 验证所有标签是否存在且为用户标签
        List<Tag> validTags = new ArrayList<>();
        for (Integer tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId);
            if (tag != null) {
                validTags.add(tag);
            }
        }
        
        // 获取有效的标签ID列表
        List<Integer> validTagIds = validTags.stream()
                .map(Tag::getId)
                .toList();
        
        // 清除用户现有的所有标签
        userTagRepository.deleteByUserId(userId);
        
        // 添加新的标签关联
        if (!validTagIds.isEmpty()) {
            userTagRepository.batchSave(userId, validTagIds);
        }
        
        // 返回更新后的标签列表
        return validTags;
    }

    @Override
    @Transactional
    public boolean removeUserTag(Integer userId, Integer tagId) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查标签是否存在
        Tag tag = tagRepository.findById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 检查是否存在关联
        if (userTagRepository.exists(userId, tagId) == 0) {
            return false; // 不存在关联，视为失败
        }
        
        // 移除用户标签关联
        int result = userTagRepository.delete(userId, tagId);
        return result > 0;
    }

    @Override
    public List<Tag> getUserTags(Integer userId) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        return userTagRepository.findTagsByUserId(userId);
    }

    @Override
    public List<UserTag> getUserTagDetails(Integer userId) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        return userTagRepository.findDetailsByUserId(userId);
    }

    @Override
    public boolean hasTag(Integer userId, Integer tagId) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        return userTagRepository.exists(userId, tagId) > 0;
    }

    @Override
    public List<Map<String, Object>> getHotUserTags(Integer limit) {
        // 获取标签使用情况统计
        List<Map<String, Object>> tagStats = userTagRepository.getTagUsageStats();
        
        // 如果限制数量无效，使用默认值10
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        
        // 按使用数量排序并限制结果数量
        tagStats.sort((a, b) -> {
            Integer countA = (Integer) a.get("count");
            Integer countB = (Integer) b.get("count");
            return countB.compareTo(countA); // 降序排序
        });
        
        // 截取指定数量的结果
        if (tagStats.size() > limit) {
            tagStats = tagStats.subList(0, limit);
        }
        
        // 添加标签详情
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> stat : tagStats) {
            Integer tagId = (Integer) stat.get("tagId");
            Tag tag = tagRepository.findById(tagId);
            if (tag != null) {
                Map<String, Object> tagInfo = new HashMap<>();
                tagInfo.put("id", tag.getId());
                tagInfo.put("name", tag.getName());
                tagInfo.put("category", tag.getCategory());
                tagInfo.put("type", tag.getType());
                tagInfo.put("userCount", stat.get("count"));
                result.add(tagInfo);
            }
        }
        
        return result;
    }

    @Override
    public List<Integer> getSimilarUsers(Integer userId, Integer limit) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 获取用户的标签
        List<Tag> userTags = getUserTags(userId);
        if (userTags.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取标签ID
        List<Integer> userTagIds = userTags.stream()
                .map(Tag::getId)
                .toList();
        
        // 查找具有相似标签的用户
        Map<Integer, Integer> userScores = new HashMap<>();
        
        // 为每个标签查找关联的用户
        for (Integer tagId : userTagIds) {
            List<Integer> usersWithTag = userTagRepository.findUserIdsByTagId(tagId);
            for (Integer otherUserId : usersWithTag) {
                // 排除自己
                if (!otherUserId.equals(userId)) {
                    // 增加用户的相似度分数
                    userScores.put(otherUserId, userScores.getOrDefault(otherUserId, 0) + 1);
                }
            }
        }
        
        // 按相似度分数排序
        List<Map.Entry<Integer, Integer>> sortedUsers = new ArrayList<>(userScores.entrySet());
        sortedUsers.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // 提取用户ID并限制结果数量
        return sortedUsers.stream()
                .map(Map.Entry::getKey)
                .limit(limit != null && limit > 0 ? limit : 10)
                .toList();
    }

    @Override
    public List<Integer> getRecommendedContent(Integer userId, String contentType, Integer limit) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证内容类型
        if (!"post".equals(contentType) && !"resource".equals(contentType)) {
            throw new BusinessException("无效的内容类型");
        }
        
        // 获取用户的标签
        List<Tag> userTags = getUserTags(userId);
        if (userTags.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取标签ID
        List<Integer> userTagIds = userTags.stream()
                .map(Tag::getId)
                .toList();
        
        // 根据内容类型和用户标签查找推荐内容
        // 这里简单实现，实际可能需要更复杂的推荐算法
        Map<Integer, Integer> contentScores = new HashMap<>();
        
        // 为每个标签查找关联的内容
        for (Integer tagId : userTagIds) {
            List<Integer> contentIds;
            if ("post".equals(contentType)) {
                // 查找与标签关联的帖子
                contentIds = tagRepository.findPostIdsByTagId(tagId);
            } else {
                // 查找与标签关联的资源
                contentIds = tagRepository.findResourceIdsByTagId(tagId);
            }
            
            for (Integer contentId : contentIds) {
                // 增加内容的推荐分数
                contentScores.put(contentId, contentScores.getOrDefault(contentId, 0) + 1);
            }
        }
        
        // 按推荐分数排序
        List<Map.Entry<Integer, Integer>> sortedContent = new ArrayList<>(contentScores.entrySet());
        sortedContent.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // 提取内容ID并限制结果数量
        return sortedContent.stream()
                .map(Map.Entry::getKey)
                .limit(limit != null && limit > 0 ? limit : 10)
                .toList();
    }
} 