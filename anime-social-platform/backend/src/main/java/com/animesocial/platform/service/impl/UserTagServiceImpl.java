package com.animesocial.platform.service.impl;

import com.animesocial.platform.model.Tag;
import com.animesocial.platform.repository.TagRepository;
import com.animesocial.platform.repository.UserTagRepository;
import com.animesocial.platform.service.UserTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户标签服务实现类
 */
@Service
@Transactional
public class UserTagServiceImpl implements UserTagService {

    private final UserTagRepository userTagRepository;
    private final TagRepository tagRepository;

    public UserTagServiceImpl(UserTagRepository userTagRepository, TagRepository tagRepository) {
        this.userTagRepository = userTagRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public String addTag(Integer userId, Integer tagId) {
        // 检查标签是否存在
        Tag tag = tagRepository.findById(tagId);
        if (tag == null) {
            return "标签不存在";
        }
        
        // 检查用户是否已关联该标签
        if (userTagRepository.checkUserHasTag(userId, tagId) > 0) {
            return "用户已添加该标签";
        }
        
        // 保存用户标签关联
        userTagRepository.saveUserTag(userId, tagId);
        return "标签添加成功";
    }

    @Override
    public String addTags(Integer userId, List<Integer> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return "标签列表为空";
        }
        
        int successCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Integer tagId : tagIds) {
            // 检查标签是否存在
            Tag tag = tagRepository.findById(tagId);
            if (tag == null) {
                errors.add("标签ID" + tagId + "不存在");
                continue;
            }
            
            // 检查用户是否已有该标签
            if (userTagRepository.checkUserHasTag(userId, tagId) > 0) {
                errors.add("标签'" + tag.getName() + "'已添加");
                continue;
            }
            
            // 添加用户标签
            userTagRepository.saveUserTag(userId, tagId);
            successCount++;
        }
        
        if (successCount == 0) {
            return "所有标签添加失败: " + String.join(", ", errors);
        } else if (errors.isEmpty()) {
            return "全部标签添加成功";
        } else {
            return String.format("成功添加%d个标签, %d个标签添加失败: %s", 
                    successCount, errors.size(), String.join(", ", errors));
        }
    }

    @Override
    public String removeTag(Integer userId, Integer tagId) {
        // 检查用户是否有该标签
        if (userTagRepository.checkUserHasTag(userId, tagId) == 0) {
            return "用户未添加该标签";
        }
        
        // 移除用户标签
        userTagRepository.deleteUserTag(userId, tagId);
        return "标签移除成功";
    }

    @Override
    public String removeAllTags(Integer userId) {
        int count = userTagRepository.deleteUserAllTags(userId);
        return String.format("已移除%d个标签", count);
    }

    @Override
    public List<Tag> getUserTags(Integer userId) {
        return userTagRepository.findByUserId(userId);
    }

    @Override
    public boolean hasTag(Integer userId, Integer tagId) {
        return userTagRepository.checkUserHasTag(userId, tagId) > 0;
    }

    @Override
    public List<Tag> getHotUserTags(Integer limit) {
        return userTagRepository.findHotTagsByUserCount(limit);
    }

    @Override
    public List<Integer> getUsersWithTag(Integer tagId) {
        return userTagRepository.findUserIdsByTagId(tagId);
    }
} 