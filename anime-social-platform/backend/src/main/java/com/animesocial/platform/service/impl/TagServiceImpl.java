package com.animesocial.platform.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.repository.TagRepository;
import com.animesocial.platform.service.TagService;

import lombok.extern.slf4j.Slf4j;

/**
 * 标签服务实现类
 * 负责处理与标签相关的业务逻辑，包括标签的增删改查、
 * 标签与内容的关联等功能
 */
@Service
@Slf4j
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;
    
    /**
     * 获取所有标签
     * @return 标签列表
     */
    @Override
    public List<TagDTO> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return convertToDTOList(tags);
    }
    
    /**
     * 根据类型获取标签
     * @param type 标签类型(post/resource)
     * @return 标签列表
     */
    @Override
    public List<TagDTO> getTagsByType(String type) {
        if (!StringUtils.hasText(type)) {
            return getAllTags();
        }
        List<Tag> tags = tagRepository.findByType(type);
        return convertToDTOList(tags);
    }
    
    /**
     * 根据ID获取标签
     * @param id 标签ID
     * @return 标签信息，如果不存在则返回null
     */
    @Override
    public TagDTO getTagById(Integer id) {
        if (id == null) {
            return null;
        }
        Tag tag = tagRepository.findById(id);
        return tag != null ? convertToDTO(tag) : null;
    }
    
    /**
     * 根据名称获取标签
     * @param name 标签名称
     * @return 标签信息，如果不存在则返回null
     */
    @Override
    public TagDTO getTagByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        Tag tag = tagRepository.findByName(name);
        return tag != null ? convertToDTO(tag) : null;
    }
    
    /**
     * 创建标签
     * @param tag 标签对象
     * @return 创建的标签
     * @throws BusinessException 如果标签名已存在
     */
    @Override
    @Transactional
    public TagDTO createTag(Tag tag) {
        // 参数校验
        if (tag == null || !StringUtils.hasText(tag.getName()) || !StringUtils.hasText(tag.getType())) {
            throw new BusinessException("标签信息不完整");
        }
        
        // 检查标签名是否已存在
        Tag existingTag = tagRepository.findByName(tag.getName());
        if (existingTag != null) {
            throw new BusinessException("标签名已存在");
        }
        
        // 保存标签
        tagRepository.save(tag);
        return convertToDTO(tag);
    }
    
    /**
     * 更新标签
     * @param id 标签ID
     * @param tag 标签对象
     * @return 更新后的标签
     * @throws BusinessException 如果标签不存在或标签名已被使用
     */
    @Override
    @Transactional
    public TagDTO updateTag(Integer id, Tag tag) {
        // 参数校验
        if (id == null || tag == null || !StringUtils.hasText(tag.getName()) || !StringUtils.hasText(tag.getType())) {
            throw new BusinessException("参数不完整");
        }
        
        // 检查标签是否存在
        Tag existingTag = tagRepository.findById(id);
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 如果标签名发生变化，检查新名称是否已存在
        String existingName = existingTag.getName();
        String newName = tag.getName();
        if (!Objects.equals(existingName, newName)) {
            Tag tagWithSameName = tagRepository.findByName(newName);
            if (tagWithSameName != null && !Objects.equals(tagWithSameName.getId(), id)) {
                throw new BusinessException("标签名已存在");
            }
        }
        
        // 更新标签
        tag.setId(id);
        tagRepository.update(tag);
        return convertToDTO(tag);
    }
    
    /**
     * 删除标签
     * @param id 标签ID
     * @throws BusinessException 如果标签不存在或标签正在被使用
     */
    @Override
    @Transactional
    public void deleteTag(Integer id) {
        if (id == null) {
            throw new BusinessException("标签ID不能为空");
        }
        
        // 检查标签是否存在
        Tag tag = tagRepository.findById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 检查标签是否被内容使用
        int contentCount = tagRepository.countContentByTagId(id);
        if (contentCount > 0) {
            throw new BusinessException("标签正在被内容使用，无法删除");
        }
        
        // 删除标签
        tagRepository.deleteById(id);
    }
    
    /**
     * 更新帖子标签
     * @param postId 帖子ID
     * @param tagIds 标签ID列表
     * @throws BusinessException 如果标签不存在或标签类型错误
     */
    @Override
    @Transactional
    public void updatePostTags(Integer postId, List<Integer> tagIds) {
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        
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
                String tagType = tag.getType();
                if (!"post".equals(tagType)) {
                    throw new BusinessException("标签类型错误，不能用于帖子: " + tag.getName());
                }
            }
            
            // 保存帖子标签
            tagRepository.savePostTags(postId, tagIds);
        }
    }
    
    /**
     * 更新资源标签
     * @param resourceId 资源ID
     * @param tagIds 标签ID列表
     * @throws BusinessException 如果标签不存在或标签类型错误
     */
    @Override
    @Transactional
    public void updateResourceTags(Integer resourceId, List<Integer> tagIds) {
        if (resourceId == null) {
            throw new BusinessException("资源ID不能为空");
        }
        
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
                String tagType = tag.getType();
                if (!"resource".equals(tagType)) {
                    throw new BusinessException("标签类型错误，不能用于资源: " + tag.getName());
                }
            }
            
            // 保存资源标签
            tagRepository.saveResourceTags(resourceId, tagIds);
        }
    }
    
    /**
     * 获取热门帖子标签
     * @param limit 限制数量
     * @return 热门标签列表
     */
    @Override
    public List<TagDTO> getHotPostTags(Integer limit) {
        int actualLimit = limit != null && limit > 0 ? limit : 10;
        List<Tag> tags = tagRepository.findHotTagsByContentCount("post", actualLimit);
        return convertToDTOList(tags);
    }
    
    /**
     * 获取热门资源标签
     * @param limit 限制数量
     * @return 热门标签列表
     */
    @Override
    public List<TagDTO> getHotResourceTags(Integer limit) {
        int actualLimit = limit != null && limit > 0 ? limit : 10;
        List<Tag> tags = tagRepository.findHotTagsByContentCount("resource", actualLimit);
        return convertToDTOList(tags);
    }
    
    /**
     * 获取帖子标签
     * @param postId 帖子ID
     * @return 标签列表
     */
    @Override
    public List<TagDTO> getPostTags(Integer postId) {
        if (postId == null) {
            return new ArrayList<>();
        }
        List<Tag> tags = tagRepository.findByPostId(postId);
        return convertToDTOList(tags);
    }
    
    /**
     * 获取资源标签
     * @param resourceId 资源ID
     * @return 标签列表
     */
    @Override
    public List<TagDTO> getResourceTags(Integer resourceId) {
        if (resourceId == null) {
            return new ArrayList<>();
        }
        List<Tag> tags = tagRepository.findByResourceId(resourceId);
        return convertToDTOList(tags);
    }
    
    /**
     * 为用户推荐标签（基于用户已有标签）
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐标签列表
     */
    @Override
    public List<TagDTO> getRecommendedTags(Integer userId, Integer limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        int actualLimit = limit != null && limit > 0 ? limit : 10;
        
        // 获取用户已有标签
        List<Tag> userTags = tagRepository.findByUserId(userId);
        
        // 如果用户没有标签，则返回热门内容标签
        if (userTags == null || userTags.isEmpty()) {
            return getHotPostTags(actualLimit);
        }
        
        // 获取热门内容标签
        List<Tag> hotTags = tagRepository.findHotTagsByContentCount("post", actualLimit * 2);
        if (hotTags == null || hotTags.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 过滤掉用户已有的标签
        List<Integer> userTagIds = userTags.stream()
            .map(Tag::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        List<Tag> recommendedTags = hotTags.stream()
            .filter(tag -> tag != null && tag.getId() != null && !userTagIds.contains(tag.getId()))
            .limit(actualLimit)
            .collect(Collectors.toList());
        
        return convertToDTOList(recommendedTags);
    }
    
    /**
     * 将Tag实体转换为DTO
     * @param tag Tag实体
     * @return TagDTO对象
     */
    @Override
    public TagDTO convertToDTO(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        TagDTO dto = new TagDTO();
        BeanUtils.copyProperties(tag, dto);
        
        // 获取标签使用次数（只统计内容标签）
        Integer tagId = tag.getId();
        if (tagId != null) {
            Integer contentCount = tagRepository.countContentByTagId(tagId);
            dto.setContentCount(contentCount);
        } else {
            dto.setContentCount(0);
        }
        
        // 默认为非选中状态
        dto.setIsSelected(false);
        
        return dto;
    }
    
    /**
     * 将TagDTO转换为Tag实体
     * @param tagDTO TagDTO对象
     * @return Tag实体
     */
    @Override
    public Tag convertToEntity(TagDTO tagDTO) {
        if (tagDTO == null) {
            return null;
        }
        
        Tag tag = new Tag();
        // 只复制基本属性，不包括统计信息
        tag.setId(tagDTO.getId());
        tag.setName(tagDTO.getName());
        tag.setCategory(tagDTO.getCategory());
        tag.setType(tagDTO.getType());
        
        return tag;
    }
    
    /**
     * 创建标签（接收DTO参数）
     * @param tagDTO 标签DTO对象
     * @return 创建的标签
     */
    @Override
    @Transactional
    public TagDTO createTag(TagDTO tagDTO) {
        if (tagDTO == null) {
            throw new BusinessException("标签信息不能为空");
        }
        
        Tag tag = convertToEntity(tagDTO);
        return createTag(tag);
    }
    
    /**
     * 将Tag实体列表转换为DTO列表
     * @param tags Tag实体列表
     * @return TagDTO对象列表
     */
    private List<TagDTO> convertToDTOList(List<Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        
        return tags.stream()
            .filter(Objects::nonNull)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 根据标签ID查询与之关联的帖子ID列表
     * @param tagId 标签ID
     * @return 帖子ID列表
     * @throws BusinessException 如果标签不存在
     */
    @Override
    public List<Integer> findPostIdsByTagId(Integer tagId) {
        if (tagId == null) {
            return new ArrayList<>();
        }
        
        // 检查标签是否存在
        Tag tag = tagRepository.findById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        return tagRepository.findPostIdsByTagId(tagId);
    }
    
    /**
     * 根据标签ID查询与之关联的资源ID列表
     * @param tagId 标签ID
     * @return 资源ID列表
     * @throws BusinessException 如果标签不存在
     */
    @Override
    public List<Integer> findResourceIdsByTagId(Integer tagId) {
        if (tagId == null) {
            return new ArrayList<>();
        }
        
        // 检查标签是否存在
        Tag tag = tagRepository.findById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        return tagRepository.findResourceIdsByTagId(tagId);
    }
    
    /**
     * 获取标签总数
     * @param type 标签类型(可选，post/resource，null表示所有)
     * @return 标签总数
     */
    @Override
    public int countTags(String type) {
        if (type == null) {
            return tagRepository.count();
        } else {
            return tagRepository.countByType(type);
        }
    }
} 