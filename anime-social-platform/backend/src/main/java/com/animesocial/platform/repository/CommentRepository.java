package com.animesocial.platform.repository;

import java.util.List;

import org.apache.ibatis.annotations.*;

import com.animesocial.platform.model.Comment;

/**
 * 评论数据访问接口
 * 使用MyBatis注解方式定义SQL操作，处理评论相关的数据库操作
 */
@Mapper
public interface CommentRepository {
    
    /**
     * 根据ID查询评论
     * @param id 评论ID
     * @return 评论对象，如果不存在则返回null
     */
    @Select("SELECT * FROM comments WHERE id = #{id}")
    Comment findById(Integer id);
    
    /**
     * 根据帖子ID查询评论
     * @param postId 帖子ID
     * @param parentId 父评论ID，如果为null则查询一级评论
     * @return 评论列表，按创建时间升序排序
     */
    @Select({
        "<script>",
        "SELECT * FROM comments WHERE post_id = #{postId}",
        "<if test='parentId != null'>",
        "  AND parent_id = #{parentId}",
        "</if>",
        "<if test='parentId == null'>",
        "  AND parent_id IS NULL",
        "</if>",
        "ORDER BY created_at ASC",
        "</script>"
    })
    List<Comment> findByPostId(@Param("postId") Integer postId, @Param("parentId") Integer parentId);
    
    /**
     * 根据用户ID查询评论
     * @param userId 用户ID
     * @return 评论列表，按创建时间降序排序
     */
    @Select("SELECT * FROM comments WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Comment> findByUserId(Integer userId);
    
    /**
     * 根据父评论ID查询回复
     * @param parentId 父评论ID
     * @return 回复列表，按创建时间升序排序
     */
    @Select("SELECT * FROM comments WHERE parent_id = #{parentId} ORDER BY created_at ASC")
    List<Comment> findByParentId(Integer parentId);
    
    /**
     * 插入评论
     * @param comment 评论对象
     * @return 插入成功返回1，失败返回0
     */
    @Insert("INSERT INTO comments (post_id, user_id, content, created_at, parent_id) " +
            "VALUES (#{postId}, #{userId}, #{content}, #{createdAt}, #{parentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Comment comment);
    
    /**
     * 删除评论
     * @param id 评论ID
     * @return 删除成功返回1，失败返回0
     */
    @Delete("DELETE FROM comments WHERE id = #{id}")
    void delete(Integer id);
    
    /**
     * 统计帖子评论数量
     * @param postId 帖子ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM comments WHERE post_id = #{postId}")
    int countByPostId(Integer postId);
    
    /**
     * 删除帖子的所有评论
     * @param postId 帖子ID
     * @return 删除的评论数量
     */
    @Delete("DELETE FROM comments WHERE post_id = #{postId}")
    int deleteByPostId(Integer postId);
} 