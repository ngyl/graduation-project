package com.animesocial.platform.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.animesocial.platform.model.Post;

/**
 * 帖子数据访问接口
 * 使用MyBatis注解方式定义SQL操作
 */
@Mapper
public interface PostRepository {
    
    /**
     * 根据ID查询帖子
     * @param id 帖子ID
     * @return 帖子对象，如果不存在则返回null
     */
    @Select("SELECT * FROM posts WHERE id = #{id}")
    Post findById(Integer id);

    /**
     * 分页查询指定用户的所有帖子
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 帖子列表，按创建时间降序排序
     */
    @Select("SELECT * FROM posts WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<Post> findByUserId(Integer userId, Integer offset, Integer limit);

    /**
     * 统计用户的帖子数量
     * @param userId 用户ID
     * @return 帖子数量
     */
    @Select("SELECT COUNT(*) FROM posts WHERE user_id = #{userId}")
    int countByUserId(Integer userId);

    /**
     * 分页查询所有帖子
     * @param offset 偏移量，表示从第几条记录开始
     * @param limit 每页数量
     * @return 帖子列表，按创建时间降序排序
     */
    @Select("SELECT * FROM posts ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<Post> findAllPosts(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据排序方式分页查询所有帖子
     * @param offset 偏移量
     * @param limit 每页数量
     * @param sort 排序方式 (latest/views/likes)
     * @return 帖子列表
     */
    @Select({
        "<script>",
        "SELECT * FROM posts",
        "ORDER BY",
        "<choose>",
        "  <when test=\"sort == 'views'\">view_count DESC</when>",
        "  <when test=\"sort == 'likes'\">like_count DESC</when>",
        "  <otherwise>created_at DESC</otherwise>",
        "</choose>",
        "LIMIT #{offset}, #{limit}",
        "</script>"
    })
    List<Post> findAllWithSort(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("sort") String sort);

    /**
     * 根据标签ID分页查询帖子
     * @param tagId 标签ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @param sort 排序方式 (latest/views/likes)
     * @return 帖子列表
     */
    @Select({
        "<script>",
        "SELECT p.* FROM posts p",
        "JOIN content_tags ct ON p.id = ct.content_id",
        "WHERE ct.tag_id = #{tagId} AND ct.content_type = 'post'",
        "ORDER BY",
        "<choose>",
        "  <when test=\"sort == 'views'\">p.view_count DESC</when>",
        "  <when test=\"sort == 'likes'\">p.like_count DESC</when>",
        "  <otherwise>p.created_at DESC</otherwise>",
        "</choose>",
        "LIMIT #{offset}, #{limit}",
        "</script>"
    })
    List<Post> findByTagId(@Param("tagId") Integer tagId, @Param("offset") Integer offset, @Param("limit") Integer limit, @Param("sort") String sort);

    /**
     * 统计所有帖子数量
     * @return 帖子总数
     */
    @Select("SELECT COUNT(*) FROM posts")
    int count();

    /**
     * 统计指定标签的帖子数量
     * @param tagId 标签ID
     * @return 帖子数量
     */
    @Select("SELECT COUNT(*) FROM content_tags WHERE tag_id = #{tagId} AND content_type = 'post'")
    int countByTagId(Integer tagId);

    /**
     * 保存新帖子
     * @param post 帖子对象，包含标题、内容等信息
     */
    @Insert("INSERT INTO posts (user_id, title, content, created_at, updated_at, view_count, like_count, is_top) " +
            "VALUES (#{userId}, #{title}, #{content}, NOW(), NOW(), 0, 0, false)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Post post);

    /**
     * 更新帖子
     * @param post 帖子对象，包含需要更新的字段
     */
    @Update("UPDATE posts SET title = #{title}, content = #{content}, updated_at = NOW() WHERE id = #{id}")
    void update(Post post);

    /**
     * 增加帖子点赞数
     * @param id 帖子ID
     */
    @Update("UPDATE posts SET like_count = like_count + 1 WHERE id = #{id}")
    void increaseLikeCount(Integer id);

    /**
     * 减少帖子点赞数
     * @param id 帖子ID
     */
    @Update("UPDATE posts SET like_count = like_count - 1 WHERE id = #{id}")
    void decreaseLikeCount(Integer id);

    /**
     * 增加帖子浏览量
     * @param id 帖子ID
     */
    @Update("UPDATE posts SET view_count = view_count + 1 WHERE id = #{id}")
    void increaseViewCount(Integer id);

    /**
     * 删除帖子
     * @param id 帖子ID
     */
    @Delete("DELETE FROM posts WHERE id = #{id}")
    void deleteById(Integer id);
    
    /**
     * 查询用户点赞的所有帖子列表（不分页）
     * @param userId 用户ID
     * @return 帖子列表
     */
    @Select("SELECT p.* FROM posts p " +
            "JOIN post_likes pl ON p.id = pl.post_id " +
            "WHERE pl.user_id = #{userId} " +
            "ORDER BY pl.created_at DESC")
    List<Post> findAllLikedPostsByUserId(Integer userId);
    
    /**
     * 查询用户点赞的帖子ID列表
     * @param userId 用户ID
     * @return 帖子ID列表
     */
    @Select("SELECT post_id FROM post_likes WHERE user_id = #{userId}")
    List<Integer> findLikedPostIdsByUserId(Integer userId);
    
    /**
     * 分页查询用户点赞的帖子
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 帖子列表
     */
    @Select("SELECT p.* FROM posts p " +
            "JOIN post_likes pl ON p.id = pl.post_id " +
            "WHERE pl.user_id = #{userId} " +
            "ORDER BY pl.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Post> findLikedPostsByUserIdPaged(@Param("userId") Integer userId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 统计用户点赞的帖子数量
     * @param userId 用户ID
     * @return 点赞数量
     */
    @Select("SELECT COUNT(*) FROM post_likes WHERE user_id = #{userId}")
    int countLikedPostsByUserId(Integer userId);

    /**
     * 更新帖子置顶状态
     * @param id 帖子ID
     * @param isTop 是否置顶(0不置顶,1置顶)
     */
    @Update("UPDATE posts SET is_top = #{isTop} WHERE id = #{id}")
    void updateTopStatus(@Param("id") Integer id, @Param("isTop") Integer isTop);

    /**
     * 获取置顶帖子列表
     * @return 置顶帖子列表
     */
    @Select("SELECT * FROM posts WHERE is_top = 1 ORDER BY created_at DESC")
    List<Post> findTopPosts();

    /**
     * 按关键词搜索帖子
     * @param keyword 搜索关键词
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 匹配的帖子列表
     */
    @Select("SELECT * FROM posts WHERE title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<Post> searchPosts(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计搜索结果总数
     * @param keyword 搜索关键词
     * @return 匹配的帖子数量
     */
    @Select("SELECT COUNT(*) FROM posts WHERE title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')")
    int countSearchPosts(@Param("keyword") String keyword);
    
    /**
     * 获取热门帖子（按照点赞数除以浏览数的比例排序）
     * @param limit 获取数量
     * @return 热门帖子列表
     */
    @Select("SELECT * FROM posts WHERE view_count > 0 ORDER BY (like_count / view_count) DESC, like_count DESC LIMIT #{limit}")
    List<Post> findHotPosts(@Param("limit") Integer limit);
} 