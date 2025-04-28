package com.animesocial.platform.repository;

import com.animesocial.platform.model.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 标签数据访问接口
 */
@Mapper
public interface TagRepository {
    /**
     * 查询所有标签
     * @return 标签列表
     */
    @Select("SELECT * FROM tags")
    List<Tag> findAll();
    
    /**
     * 根据类型查询标签
     * @param type 标签类型(post/resource)
     * @return 标签列表
     */
    @Select("SELECT * FROM tags WHERE type = #{type}")
    List<Tag> findByType(String type);
    
    /**
     * 根据ID查询标签
     * @param id 标签ID
     * @return 标签对象
     */
    @Select("SELECT * FROM tags WHERE id = #{id}")
    Tag findById(Integer id);
    
    /**
     * 根据名称查询标签
     * @param name 标签名称
     * @return 标签对象
     */
    @Select("SELECT * FROM tags WHERE name = #{name}")
    Tag findByName(String name);
    
    /**
     * 根据用户ID查询标签（用于推荐功能）
     * @param userId 用户ID
     * @return 标签列表
     */
    @Select("SELECT t.* FROM tags t " +
            "JOIN user_tags ut ON t.id = ut.tag_id " +
            "WHERE ut.user_id = #{userId}")
    List<Tag> findByUserId(Integer userId);
    
    /**
     * 根据帖子ID查询标签
     * @param postId 帖子ID
     * @return 标签列表
     */
    @Select("SELECT t.* FROM tags t " +
            "JOIN content_tags ct ON t.id = ct.tag_id " +
            "WHERE ct.content_id = #{postId} AND ct.content_type = 'post'")
    List<Tag> findByPostId(Integer postId);
    
    /**
     * 根据资源ID查询标签
     * @param resourceId 资源ID
     * @return 标签列表
     */
    @Select("SELECT t.* FROM tags t " +
            "JOIN content_tags ct ON t.id = ct.tag_id " +
            "WHERE ct.content_id = #{resourceId} AND ct.content_type = 'resource'")
    List<Tag> findByResourceId(Integer resourceId);
    
    /**
     * 创建标签
     * @param tag 标签对象
     */
    @Insert("INSERT INTO tags (name, category, type) VALUES (#{name}, #{category}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Tag tag);
    
    /**
     * 更新标签
     * @param tag 标签对象
     */
    @Update("UPDATE tags SET name = #{name}, category = #{category}, type = #{type} WHERE id = #{id}")
    void update(Tag tag);
    
    /**
     * 删除标签
     * @param id 标签ID
     */
    @Delete("DELETE FROM tags WHERE id = #{id}")
    void deleteById(Integer id);
    
    /**
     * 保存内容标签
     * @param contentType 内容类型(post/resource)
     * @param contentId 内容ID
     * @param tagId 标签ID
     */
    @Insert("INSERT INTO content_tags (content_type, content_id, tag_id) VALUES (#{contentType}, #{contentId}, #{tagId})")
    void saveContentTag(@Param("contentType") String contentType, @Param("contentId") Integer contentId, @Param("tagId") Integer tagId);
    
    /**
     * 批量保存帖子标签
     * @param postId 帖子ID
     * @param tagIds 标签ID列表
     */
    @Insert({
        "<script>",
        "INSERT INTO content_tags (content_type, content_id, tag_id) VALUES ",
        "<foreach collection='tagIds' item='tagId' separator=','>",
        "('post', #{postId}, #{tagId})",
        "</foreach>",
        "</script>"
    })
    void savePostTags(@Param("postId") Integer postId, @Param("tagIds") List<Integer> tagIds);
    
    /**
     * 批量保存资源标签
     * @param resourceId 资源ID
     * @param tagIds 标签ID列表
     */
    @Insert({
        "<script>",
        "INSERT INTO content_tags (content_type, content_id, tag_id) VALUES ",
        "<foreach collection='tagIds' item='tagId' separator=','>",
        "('resource', #{resourceId}, #{tagId})",
        "</foreach>",
        "</script>"
    })
    void saveResourceTags(@Param("resourceId") Integer resourceId, @Param("tagIds") List<Integer> tagIds);
    
    /**
     * 删除内容标签
     * @param contentType 内容类型(post/resource)
     * @param contentId 内容ID
     * @param tagId 标签ID
     */
    @Delete("DELETE FROM content_tags WHERE content_type = #{contentType} AND content_id = #{contentId} AND tag_id = #{tagId}")
    void deleteContentTag(@Param("contentType") String contentType, @Param("contentId") Integer contentId, @Param("tagId") Integer tagId);
    
    /**
     * 删除内容的所有标签
     * @param contentType 内容类型(post/resource)
     * @param contentId 内容ID
     */
    @Delete("DELETE FROM content_tags WHERE content_type = #{contentType} AND content_id = #{contentId}")
    void deleteAllContentTags(@Param("contentType") String contentType, @Param("contentId") Integer contentId);
    
    /**
     * 删除帖子的所有标签
     * @param postId 帖子ID
     */
    @Delete("DELETE FROM content_tags WHERE content_type = 'post' AND content_id = #{postId}")
    void deletePostTags(Integer postId);
    
    /**
     * 删除资源的所有标签
     * @param resourceId 资源ID
     */
    @Delete("DELETE FROM content_tags WHERE content_type = 'resource' AND content_id = #{resourceId}")
    void deleteResourceTags(Integer resourceId);
    
    /**
     * 统计使用标签的内容数量（帖子和资源）
     * @param tagId 标签ID
     * @return 内容数量
     */
    @Select("SELECT COUNT(*) FROM content_tags WHERE tag_id = #{tagId}")
    int countContentByTagId(Integer tagId);
    
    /**
     * 获取热门标签(根据内容使用数量)
     * @param contentType 内容类型(post/resource)
     * @param limit 数量限制
     * @return 标签列表
     */
    @Select("SELECT t.*, COUNT(ct.id) AS content_count FROM tags t " +
            "JOIN content_tags ct ON t.id = ct.tag_id " +
            "WHERE ct.content_type = #{contentType} " +
            "GROUP BY t.id " +
            "ORDER BY content_count DESC " +
            "LIMIT #{limit}")
    List<Tag> findHotTagsByContentCount(@Param("contentType") String contentType, @Param("limit") Integer limit);
    
    /**
     * 根据标签ID查询与之关联的帖子ID列表
     * @param tagId 标签ID
     * @return 帖子ID列表
     */
    @Select("SELECT content_id FROM content_tags WHERE content_type = 'post' AND tag_id = #{tagId}")
    List<Integer> findPostIdsByTagId(Integer tagId);
    
    /**
     * 根据标签ID查询与之关联的资源ID列表
     * @param tagId 标签ID
     * @return 资源ID列表
     */
    @Select("SELECT content_id FROM content_tags WHERE content_type = 'resource' AND tag_id = #{tagId}")
    List<Integer> findResourceIdsByTagId(Integer tagId);
    
    /**
     * 统计标签总数
     * @return 标签总数
     */
    @Select("SELECT COUNT(*) FROM tags")
    int count();
    
    /**
     * 根据类型统计标签数量
     * @param type 标签类型(post/resource)
     * @return 标签数量
     */
    @Select("SELECT COUNT(*) FROM tags WHERE type = #{type}")
    int countByType(String type);
} 