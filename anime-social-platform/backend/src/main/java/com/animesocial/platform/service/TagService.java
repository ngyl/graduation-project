package com.animesocial.platform.service;

import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.dto.TagDTO;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {
    /**
     * 获取所有标签
     * @return 标签列表
     */
    List<TagDTO> getAllTags();
    
    /**
     * 根据类型获取标签
     * @param type 标签类型(post/resource/user)
     * @return 标签列表
     */
    List<TagDTO> getTagsByType(String type);
    
    /**
     * 根据ID获取标签
     * @param id 标签ID
     * @return 标签信息
     */
    TagDTO getTagById(Integer id);
    
    /**
     * 根据名称获取标签
     * @param name 标签名称
     * @return 标签信息
     */
    TagDTO getTagByName(String name);
    
    /**
     * 创建标签
     * @param tag 标签对象
     * @return 创建的标签
     */
    TagDTO createTag(Tag tag);
    
    /**
     * 创建标签（接收DTO参数）
     * @param tagDTO 标签DTO对象
     * @return 创建的标签
     */
    TagDTO createTag(TagDTO tagDTO);
    
    /**
     * 更新标签
     * @param id 标签ID
     * @param tag 标签对象
     * @return 更新后的标签
     */
    TagDTO updateTag(Integer id, Tag tag);
    
    /**
     * 删除标签
     * @param id 标签ID
     */
    void deleteTag(Integer id);
    
    /**
     * 获取帖子标签
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<TagDTO> getPostTags(Integer postId);
    
    /**
     * 获取资源标签
     * @param resourceId 资源ID
     * @return 标签列表
     */
    List<TagDTO> getResourceTags(Integer resourceId);
    
    /**
     * 更新帖子标签
     * @param postId 帖子ID
     * @param tagIds 标签ID列表
     */
    void updatePostTags(Integer postId, List<Integer> tagIds);
    
    /**
     * 更新资源标签
     * @param resourceId 资源ID
     * @param tagIds 标签ID列表
     */
    void updateResourceTags(Integer resourceId, List<Integer> tagIds);
    
    /**
     * 获取热门帖子标签
     * @param limit 限制数量
     * @return 热门标签列表
     */
    List<TagDTO> getHotPostTags(Integer limit);
    
    /**
     * 获取热门资源标签
     * @param limit 限制数量
     * @return 热门标签列表
     */
    List<TagDTO> getHotResourceTags(Integer limit);
    
    /**
     * 为用户推荐标签（基于用户已有标签）
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐标签列表
     */
    List<TagDTO> getRecommendedTags(Integer userId, Integer limit);
    
    /**
     * 根据标签ID查询与之关联的帖子ID列表
     * @param tagId 标签ID
     * @return 帖子ID列表
     */
    List<Integer> findPostIdsByTagId(Integer tagId);
    
    /**
     * 根据标签ID查询与之关联的资源ID列表
     * @param tagId 标签ID
     * @return 资源ID列表
     */
    List<Integer> findResourceIdsByTagId(Integer tagId);
    
    /**
     * 将Tag实体转换为TagDTO
     * @param tag Tag实体
     * @return TagDTO对象
     */
    TagDTO convertToDTO(Tag tag);
    
    /**
     * 将TagDTO转换为Tag实体
     * @param tagDTO TagDTO对象
     * @return Tag实体
     */
    Tag convertToEntity(TagDTO tagDTO);
    
    /**
     * 获取标签总数
     * @param type 标签类型(可选，post/resource/user，null表示所有)
     * @return 标签总数
     */
    int countTags(String type);
} 