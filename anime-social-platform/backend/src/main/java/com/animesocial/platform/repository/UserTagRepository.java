package com.animesocial.platform.repository;

import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.UserTag;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 用户标签关联仓库
 * 专注于用户与标签之间的关联关系管理
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
    void save(@Param("userId") Integer userId, @Param("tagId") Integer tagId);
    
    /**
     * 批量保存用户标签关联
     * 
     * @param userId 用户ID
     * @param tagIds 标签ID列表
     */
    @Insert({
        "<script>",
        "INSERT INTO user_tags (user_id, tag_id, created_at) VALUES ",
        "<foreach collection='tagIds' item='tagId' separator=','>",
        "(#{userId}, #{tagId}, NOW())",
        "</foreach>",
        "</script>"
    })
    void batchSave(@Param("userId") Integer userId, @Param("tagIds") List<Integer> tagIds);
    
    /**
     * 检查用户是否已关联标签
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 关联数量，大于0表示已关联
     */
    @Select("SELECT COUNT(*) FROM user_tags WHERE user_id = #{userId} AND tag_id = #{tagId}")
    int exists(@Param("userId") Integer userId, @Param("tagId") Integer tagId);
    
    /**
     * 删除用户标签关联
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 删除的关联数量
     */
    @Delete("DELETE FROM user_tags WHERE user_id = #{userId} AND tag_id = #{tagId}")
    int delete(@Param("userId") Integer userId, @Param("tagId") Integer tagId);
    
    /**
     * 删除用户所有标签关联
     * 
     * @param userId 用户ID
     * @return 删除的关联数量
     */
    @Delete("DELETE FROM user_tags WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);
    
    /**
     * 获取用户关联的所有标签
     * 
     * @param userId 用户ID
     * @return 用户关联的标签列表
     */
    @Select("SELECT t.* FROM tags t JOIN user_tags ut ON t.id = ut.tag_id WHERE ut.user_id = #{userId}")
    List<Tag> findTagsByUserId(Integer userId);
    
    /**
     * 获取用户标签关联信息（包括创建时间）
     * 
     * @param userId 用户ID
     * @return 用户标签关联信息列表
     */
    @Select("SELECT ut.id, ut.user_id, ut.tag_id, ut.created_at, t.name, t.category, t.type " +
            "FROM user_tags ut JOIN tags t ON ut.tag_id = t.id " +
            "WHERE ut.user_id = #{userId}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "tagId", column = "tag_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "tagName", column = "name"),
        @Result(property = "tagCategory", column = "category"),
        @Result(property = "tagType", column = "type")
    })
    List<UserTag> findDetailsByUserId(Integer userId);
    
    /**
     * 根据标签ID查找关联的用户ID列表
     * 
     * @param tagId 标签ID
     * @return 关联了该标签的用户ID列表
     */
    @Select("SELECT user_id FROM user_tags WHERE tag_id = #{tagId}")
    List<Integer> findUserIdsByTagId(Integer tagId);
    
    /**
     * 统计使用某标签的用户数量
     * 
     * @param tagId 标签ID
     * @return 使用该标签的用户数量
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM user_tags WHERE tag_id = #{tagId}")
    int countUsersByTagId(Integer tagId);
    
    /**
     * 获取标签使用情况统计
     * 
     * @return 标签ID及其被使用次数的映射
     */
    @Select("SELECT tag_id, COUNT(DISTINCT user_id) as user_count FROM user_tags GROUP BY tag_id")
    @Results({
        @Result(property = "tagId", column = "tag_id"),
        @Result(property = "count", column = "user_count")
    })
    List<Map<String, Object>> getTagUsageStats();
} 