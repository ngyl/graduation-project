package com.animesocial.platform.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.animesocial.platform.model.Resource;

/**
 * 资源数据访问接口
 * 使用MyBatis注解方式定义SQL操作，处理资源相关的数据库操作
 */
@Mapper
public interface ResourceRepository {
    
    /**
     * 根据ID查询资源
     * @param id 资源ID
     * @return 资源对象，如果不存在则返回null
     */
    @Select("SELECT * FROM resources WHERE id = #{id}")
    Resource findById(Integer id);
    
    /**
     * 根据用户ID分页查询上传的资源
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 资源列表，按上传时间降序排序
     */
    @Select("SELECT * FROM resources WHERE user_id = #{userId} ORDER BY upload_time DESC LIMIT #{offset}, #{limit}")
    List<Resource> findByUserId(Integer userId, int offset, int limit);
    
    /**
     * 根据用户ID分页查询收藏的资源
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 资源列表，按收藏时间降序排序
     */
    @Select("SELECT r.* FROM resources r " +
            "JOIN favorites f ON r.id = f.resource_id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Resource> findFavoritesByUserId(Integer userId, int offset, int limit);
    
    /**
     * 分页查询所有资源
     * @param orderBy 排序方式
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 资源列表
     */
    @Select("SELECT * FROM resources ORDER BY ${orderBy} LIMIT #{offset}, #{limit}")
    List<Resource> findAll(@Param("orderBy") String orderBy, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 根据标签ID查询资源
     * @param tagId 标签ID
     * @param orderBy 排序方式
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 资源列表
     */
    @Select("SELECT r.* FROM resources r " +
            "JOIN content_tags ct ON r.id = ct.content_id " +
            "WHERE ct.tag_id = #{tagId} AND ct.content_type = 'resource' " +
            "ORDER BY ${orderBy} LIMIT #{offset}, #{limit}")
    List<Resource> findByTagId(@Param("tagId") Integer tagId, @Param("orderBy") String orderBy, @Param("offset") int offset, @Param("limit") Integer limit);
    
    /**
     * 统计标签下的资源数量
     * @param tagId 标签ID
     * @return 资源数量
     */
    @Select("SELECT COUNT(*) FROM resources r " +
            "JOIN content_tags ct ON r.id = ct.content_id " +
            "WHERE ct.tag_id = #{tagId} AND ct.content_type = 'resource'")
    int countByTagId(Integer tagId);
    
    /**
     * 统计资源总数
     * @return 资源总数
     */
    @Select("SELECT COUNT(*) FROM resources")
    int count();
    
    /**
     * 插入资源
     * @param resource 资源对象
     * @return 插入成功返回1，失败返回0
     */
    @Insert("INSERT INTO resources (user_id, title, description, file_url, cover_url, file_type, file_size, upload_time, download_count, like_count) " +
            "VALUES (#{userId}, #{title}, #{description}, #{fileUrl}, #{coverUrl}, #{fileType}, #{fileSize}, #{uploadTime}, #{downloadCount}, #{likeCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Resource resource);
    
    /**
     * 更新资源
     * @param resource 资源对象
     * @return 更新成功返回1，失败返回0
     */
    @Update("UPDATE resources SET title = #{title}, description = #{description}, cover_url = #{coverUrl} WHERE id = #{id}")
    void update(Resource resource);
    
    /**
     * 删除资源
     * @param id 资源ID
     * @return 删除成功返回1，失败返回0
     */
    @Delete("DELETE FROM resources WHERE id = #{id}")
    void delete(Integer id);
    
    /**
     * 增加下载次数
     * @param id 资源ID
     * @return 更新成功返回1，失败返回0
     */
    @Update("UPDATE resources SET download_count = download_count + 1 WHERE id = #{id}")
    void incrementDownloadCount(Integer id);
    
    /**
     * 增加收藏次数
     * @param id 资源ID
     * @return 更新成功返回1，失败返回0
     */
    @Update("UPDATE resources SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Integer id);
    
    /**
     * 减少收藏次数
     * @param id 资源ID
     * @return 更新成功返回1，失败返回0
     */
    @Update("UPDATE resources SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    void decrementLikeCount(Integer id);
    
    /**
     * 搜索资源
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 资源列表
     */
    @Select("SELECT * FROM resources WHERE title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY upload_time DESC LIMIT #{offset}, #{limit}")
    List<Resource> search(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 根据ID列表查询资源
     * @param ids 资源ID列表
     * @return 资源列表
     */
    @Select("<script>SELECT * FROM resources WHERE id IN <foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Resource> findByIds(@Param("ids") List<Integer> ids);
    
    /**
     * 统计搜索结果数量
     * @param keyword 关键词
     * @return 结果数量
     */
    @Select("SELECT COUNT(*) FROM resources WHERE title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')")
    int countSearch(String keyword);
    
    /**
     * 获取热门资源（按照点赞数除以下载数和收藏数除以下载数的总和排序）
     * @param limit 获取数量
     * @return 热门资源列表
     */
    @Select({
        "<script>",
        "SELECT r.*, ",
        "COALESCE(r.like_count / NULLIF(r.download_count, 0), 0) AS like_ratio, ",
        "(SELECT COUNT(*) FROM favorites f WHERE f.resource_id = r.id) AS favorite_count ",
        "FROM resources r ",
        "WHERE r.download_count > 0 ",
        "ORDER BY (COALESCE(r.like_count / NULLIF(r.download_count, 0), 0) + ",
        "COALESCE((SELECT COUNT(*) FROM favorites f WHERE f.resource_id = r.id) / NULLIF(r.download_count, 0), 0)) DESC, ",
        "r.like_count DESC, r.download_count DESC ",
        "LIMIT #{limit}",
        "</script>"
    })
    List<Resource> findHotResources(@Param("limit") Integer limit);
} 