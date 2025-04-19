package com.animesocial.platform.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.dto.UpdateUserTagsRequest;
import com.animesocial.platform.model.dto.UserTagsDTO;
import com.animesocial.platform.repository.TagRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.TagService;

import lombok.extern.slf4j.Slf4j;

/**
 * 标签服务实现类
 */
@Service
@Slf4j
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public List<TagDTO> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return convertToDTOList(tags);
    }
    
    @Override
    public List<TagDTO> getTagsByType(String type) {
        List<Tag> tags = tagRepository.findByType(type);
        return convertToDTOList(tags);
    }
    
    @Override
    public TagDTO getTagById(Integer id) {
        Tag tag = tagRepository.findById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        return convertToDTO(tag);
    }
    
    @Override
    public TagDTO getTagByName(String name) {
        Tag tag = tagRepository.findByName(name);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        return convertToDTO(tag);
    }
    
    @Override
    @Transactional
    public TagDTO createTag(Tag tag) {
        // 检查标签名是否已存在
        Tag existingTag = tagRepository.findByName(tag.getName());
        if (existingTag != null) {
            throw new BusinessException("标签名已存在");
        }
        
        // 保存标签
        tagRepository.save(tag);
        return convertToDTO(tag);
    }
    
    @Override
    @Transactional
    public TagDTO updateTag(Integer id, Tag tag) {
        // 检查标签是否存在
        Tag existingTag = tagRepository.findById(id);
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 如果标签名发生变化，检查新名称是否已存在
        if (!existingTag.getName().equals(tag.getName())) {
            Tag tagWithSameName = tagRepository.findByName(tag.getName());
            if (tagWithSameName != null) {
                throw new BusinessException("标签名已存在");
            }
        }
        
        // 更新标签
        tag.setId(id);
        tagRepository.update(tag);
        return convertToDTO(tag);
    }
    
    @Override
    @Transactional
    public void deleteTag(Integer id) {
        // 检查标签是否存在
        Tag tag = tagRepository.findById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 删除标签
        tagRepository.deleteById(id);
    }
    
    @Override
    public UserTagsDTO getUserTags(Integer userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 获取用户标签
        List<Tag> tags = tagRepository.findByUserId(userId);
        
        // 构建结果
        UserTagsDTO dto = new UserTagsDTO();
        dto.setUserId(userId);
        dto.setUsername(user.getUsername());
        dto.setTags(convertToDTOList(tags));
        dto.setTagCount(tags.size());
        
        return dto;
    }
    
    @Override
    @Transactional
    public UserTagsDTO updateUserTags(Integer userId, UpdateUserTagsRequest request) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 删除用户的所有标签
        tagRepository.deleteAllUserTags(userId);
        
        // 如果有新标签，则添加
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            // 验证标签是否存在
            for (Integer tagId : request.getTagIds()) {
                Tag tag = tagRepository.findById(tagId);
                if (tag == null) {
                    throw new BusinessException("标签不存在: " + tagId);
                }
            }
            
            // 保存用户标签
            tagRepository.saveUserTags(userId, request.getTagIds());
        }
        
        // 返回更新后的用户标签
        return getUserTags(userId);
    }
    
    @Override
    @Transactional
    public String addUserTag(Integer userId, Integer tagId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证标签是否存在
        Tag tag = tagRepository.findById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 检查是否已经存在该标签
        if (tagRepository.existsUserTag(userId, tagId)) {
            throw new BusinessException("已添加该标签");
        }
        
        // 保存用户标签
        tagRepository.saveUserTag(userId, tagId);
        
        return "添加标签成功";
    }
    
    @Override
    @Transactional
    public String removeUserTag(Integer userId, Integer tagId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证标签是否存在
        Tag tag = tagRepository.findById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 检查是否已经存在该标签
        if (!tagRepository.existsUserTag(userId, tagId)) {
            throw new BusinessException("未添加该标签");
        }
        
        // 删除用户标签
        tagRepository.deleteUserTag(userId, tagId);
        
        return "删除标签成功";
    }
    
    @Override
    public boolean hasUserTag(Integer userId, Integer tagId) {
        return tagRepository.existsUserTag(userId, tagId);
    }
    
    @Override
    public List<TagDTO> getPostTags(Integer postId) {
        List<Tag> tags = tagRepository.findByPostId(postId);
        return convertToDTOList(tags);
    }
    
    @Override
    public List<TagDTO> getResourceTags(Integer resourceId) {
        List<Tag> tags = tagRepository.findByResourceId(resourceId);
        return convertToDTOList(tags);
    }
    
    @Override
    @Transactional
    public void updatePostTags(Integer postId, List<Integer> tagIds) {
        // 删除帖子的所有标签
        tagRepository.deletePostTags(postId);
        
        // 如果有新标签，则添加
        if (tagIds != null && !tagIds.isEmpty()) {
            // 验证标签是否存在
            for (Integer tagId : tagIds) {
                Tag tag = tagRepository.findById(tagId);
                if (tag == null) {
                    throw new BusinessException("标签不存在: " + tagId);
                }
                
                // 检查标签类型
                if (!"post".equals(tag.getType())) {
                    throw new BusinessException("标签类型错误，不能用于帖子: " + tag.getName());
                }
            }
            
            // 保存帖子标签
            tagRepository.savePostTags(postId, tagIds);
        }
    }
    
    @Override
    @Transactional
    public void updateResourceTags(Integer resourceId, List<Integer> tagIds) {
        // 删除资源的所有标签
        tagRepository.deleteResourceTags(resourceId);
        
        // 如果有新标签，则添加
        if (tagIds != null && !tagIds.isEmpty()) {
            // 验证标签是否存在
            for (Integer tagId : tagIds) {
                Tag tag = tagRepository.findById(tagId);
                if (tag == null) {
                    throw new BusinessException("标签不存在: " + tagId);
                }
                
                // 检查标签类型
                if (!"resource".equals(tag.getType())) {
                    throw new BusinessException("标签类型错误，不能用于资源: " + tag.getName());
                }
            }
            
            // 保存资源标签
            tagRepository.saveResourceTags(resourceId, tagIds);
        }
    }
    
    @Override
    public List<TagDTO> getHotUserTags(Integer limit) {
        List<Tag> tags = tagRepository.findHotTagsByUserCount(limit);
        return convertToDTOList(tags);
    }
    
    @Override
    public List<TagDTO> getHotPostTags(Integer limit) {
        List<Tag> tags = tagRepository.findHotTagsByContentCount("post", limit);
        return convertToDTOList(tags);
    }
    
    @Override
    public List<TagDTO> getHotResourceTags(Integer limit) {
        List<Tag> tags = tagRepository.findHotTagsByContentCount("resource", limit);
        return convertToDTOList(tags);
    }
    
    @Override
    public List<TagDTO> getRecommendedTags(Integer userId, Integer limit) {
        // 获取用户已有标签
        List<Tag> userTags = tagRepository.findByUserId(userId);
        
        // 如果用户没有标签，则返回热门标签
        if (userTags.isEmpty()) {
            return getHotUserTags(limit);
        }
        
        // 基于用户已有标签推荐
        // 这里可以实现更复杂的推荐算法，例如基于相似用户、基于内容等
        // 本实现简单返回热门标签中用户没有的标签
        List<Tag> hotTags = tagRepository.findHotTagsByUserCount(limit * 2);
        List<Integer> userTagIds = userTags.stream().map(Tag::getId).collect(Collectors.toList());
        
        // 过滤掉用户已有的标签
        List<Tag> recommendedTags = hotTags.stream()
            .filter(tag -> !userTagIds.contains(tag.getId()))
            .limit(limit)
            .collect(Collectors.toList());
        
        return convertToDTOList(recommendedTags);
    }
    
    /**
     * 将Tag实体转换为DTO
     */
    @Override
    public TagDTO convertToDTO(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        TagDTO dto = new TagDTO();
        BeanUtils.copyProperties(tag, dto);
        return dto;
    }
    
    /**
     * 将TagDTO转换为Tag实体
     */
    @Override
    public Tag convertToEntity(TagDTO tagDTO) {
        if (tagDTO == null) {
            return null;
        }
        
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagDTO, tag);
        return tag;
    }
    
    /**
     * 创建标签（接收DTO参数）
     */
    @Override
    @Transactional
    public TagDTO createTag(TagDTO tagDTO) {
        Tag tag = convertToEntity(tagDTO);
        return createTag(tag);
    }
    
    /**
     * 将Tag实体列表转换为DTO列表
     */
    private List<TagDTO> convertToDTOList(List<Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        
        return tags.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
} 