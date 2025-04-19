package com.animesocial.platform.repository;

import com.animesocial.platform.model.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户标签关联仓库
 */
@Mapper
public interface UserTagRepository {
    
    /**
     * 保存用户标签关联
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     */
    @Insert("INSERT INTO user_tags(user_id, tag_id, created_at) VALUES(#{userId}, #{tagId}, NOW())")
    void saveUserTag(@Param("userId") Integer userId, @Param("tagId") Integer tagId);
    
    /**
     * 检查用户是否已关联标签
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 关联数量，大于0表示已关联
     */
    @Select("SELECT COUNT(*) FROM user_tags WHERE user_id = #{userId} AND tag_id = #{tagId}")
    int checkUserHasTag(@Param("userId") Integer userId, @Param("tagId") Integer tagId);
    
    /**
     * 删除用户标签关联
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 删除的关联数量
     */
    @Delete("DELETE FROM user_tags WHERE user_id = #{userId} AND tag_id = #{tagId}")
    int deleteUserTag(@Param("userId") Integer userId, @Param("tagId") Integer tagId);
    
    /**
     * 删除用户所有标签关联
     * 
     * @param userId 用户ID
     * @return 删除的关联数量
     */
    @Delete("DELETE FROM user_tags WHERE user_id = #{userId}")
    int deleteUserAllTags(@Param("userId") Integer userId);
    
    /**
     * 根据用户ID查找关联的标签
     * 
     * @param userId 用户ID
     * @return 用户关联的标签列表
     */
    @Select("SELECT t.* FROM tags t JOIN user_tags ut ON t.id = ut.tag_id WHERE ut.user_id = #{userId}")
    List<Tag> findByUserId(Integer userId);
    
    /**
     * 根据标签ID查找关联的用户ID列表
     * 
     * @param tagId 标签ID
     * @return 关联了该标签的用户ID列表
     */
    @Select("SELECT user_id FROM user_tags WHERE tag_id = #{tagId}")
    List<Integer> findUserIdsByTagId(Integer tagId);
    
    /**
     * 查找热门标签（按使用该标签的用户数量排序）
     * 
     * @param limit 返回的标签数量上限
     * @return 热门标签列表
     */
    @Select("SELECT t.*, COUNT(ut.user_id) as user_count FROM tags t " +
            "JOIN user_tags ut ON t.id = ut.tag_id " +
            "GROUP BY t.id " +
            "ORDER BY user_count DESC " +
            "LIMIT #{limit}")
    List<Tag> findHotTagsByUserCount(Integer limit);
} 