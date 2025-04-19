package com.animesocial.platform.repository;

import org.apache.ibatis.annotations.*;

/**
 * 收藏数据访问接口
 * 使用MyBatis注解方式定义SQL操作，处理用户收藏相关的数据库操作
 */
@Mapper
public interface FavoriteRepository {
    
    /**
     * 检查是否存在收藏关系
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 存在返回true，不存在返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM favorites WHERE user_id = #{userId} AND resource_id = #{resourceId}")
    boolean exists(@Param("userId") Integer userId, @Param("resourceId") Integer resourceId);
    
    /**
     * 统计用户收藏数量
     * @param userId 用户ID
     * @return 收藏数量
     */
    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId}")
    int countByUserId(Integer userId);
    
    /**
     * 统计资源被收藏次数
     * @param resourceId 资源ID
     * @return 收藏次数
     */
    @Select("SELECT COUNT(*) FROM favorites WHERE resource_id = #{resourceId}")
    int countByResourceId(Integer resourceId);
    
    /**
     * 保存收藏关系
     * @param userId 用户ID
     * @param resourceId 资源ID
     */
    @Insert("INSERT INTO favorites (user_id, resource_id, created_at) VALUES (#{userId}, #{resourceId}, NOW())")
    void insert(@Param("userId") Integer userId, @Param("resourceId") Integer resourceId);
    
    /**
     * 删除收藏关系
     * @param userId 用户ID
     * @param resourceId 资源ID
     */
    @Delete("DELETE FROM favorites WHERE user_id = #{userId} AND resource_id = #{resourceId}")
    void delete(@Param("userId") Integer userId, @Param("resourceId") Integer resourceId);
    
    /**
     * 删除用户的所有收藏
     * @param userId 用户ID
     */
    @Delete("DELETE FROM favorites WHERE user_id = #{userId}")
    void deleteByUserId(Integer userId);
    
    /**
     * 删除资源的所有收藏
     * @param resourceId 资源ID
     */
    @Delete("DELETE FROM favorites WHERE resource_id = #{resourceId}")
    void deleteByResourceId(Integer resourceId);
} 