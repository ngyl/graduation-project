package com.animesocial.platform.repository;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 资源点赞数据访问接口
 */
@Mapper
public interface ResourceLikeRepository {
    
    /**
     * 保存点赞记录
     * @param resourceId 资源ID
     * @param userId 用户ID
     */
    @Insert("INSERT INTO resource_likes (resource_id, user_id, created_at) VALUES (#{resourceId}, #{userId}, NOW())")
    void save(@Param("resourceId") Integer resourceId, @Param("userId") Integer userId);
    
    /**
     * 删除点赞记录
     * @param resourceId 资源ID
     * @param userId 用户ID
     */
    @Delete("DELETE FROM resource_likes WHERE resource_id = #{resourceId} AND user_id = #{userId}")
    void delete(@Param("resourceId") Integer resourceId, @Param("userId") Integer userId);
    
    /**
     * 删除资源的所有点赞记录
     * @param resourceId 资源ID
     */
    @Delete("DELETE FROM resource_likes WHERE resource_id = #{resourceId}")
    void deleteByResourceId(Integer resourceId);
    
    /**
     * 检查点赞记录是否存在
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 存在返回true，否则返回false
     */
    @Select("SELECT COUNT(*) FROM resource_likes WHERE resource_id = #{resourceId} AND user_id = #{userId}")
    boolean exists(@Param("resourceId") Integer resourceId, @Param("userId") Integer userId);
    
    /**
     * 获取资源的所有点赞用户ID
     * @param resourceId 资源ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM resource_likes WHERE resource_id = #{resourceId}")
    List<Integer> findUserIdsByResourceId(Integer resourceId);
    
    /**
     * 统计资源的点赞数量
     * @param resourceId 资源ID
     * @return 点赞数量
     */
    @Select("SELECT COUNT(*) FROM resource_likes WHERE resource_id = #{resourceId}")
    int countByResourceId(Integer resourceId);
    
    /**
     * 获取用户的所有点赞资源ID
     * @param userId 用户ID
     * @return 资源ID列表
     */
    @Select("SELECT resource_id FROM resource_likes WHERE user_id = #{userId}")
    List<Integer> findResourceIdsByUserId(Integer userId);
    
    /**
     * 统计用户的点赞数量
     * @param userId 用户ID
     * @return 点赞数量
     */
    @Select("SELECT COUNT(*) FROM resource_likes WHERE user_id = #{userId}")
    int countByUserId(Integer userId);
} 