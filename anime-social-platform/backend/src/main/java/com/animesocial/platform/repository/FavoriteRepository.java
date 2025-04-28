package com.animesocial.platform.repository;

import com.animesocial.platform.model.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏数据访问接口
 * 使用MyBatis注解方式定义SQL操作，处理用户收藏相关的数据库操作
 */
@Mapper
public interface FavoriteRepository {
    
    /**
     * 添加收藏
     * @param favorite 收藏对象
     * @return 影响行数
     */
    int insert(Favorite favorite);
    
    /**
     * 删除收藏
     * @param id 收藏ID
     * @return 影响行数
     */
    int deleteById(Integer id);
    
    /**
     * 根据用户ID和资源ID删除收藏
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 影响行数
     */
    int deleteByUserIdAndResourceId(@Param("userId") Integer userId, @Param("resourceId") Integer resourceId);
    
    /**
     * 根据ID查询收藏
     * @param id 收藏ID
     * @return 收藏对象
     */
    Favorite findById(Integer id);
    
    /**
     * 根据用户ID查询收藏列表
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<Favorite> findByUserId(Integer userId);
    
    /**
     * 根据资源ID查询收藏列表
     * @param resourceId 资源ID
     * @return 收藏列表
     */
    List<Favorite> findByResourceId(Integer resourceId);
    
    /**
     * 查询用户是否已收藏资源
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 收藏对象，如果不存在则返回null
     */
    Favorite findByUserIdAndResourceId(@Param("userId") Integer userId, @Param("resourceId") Integer resourceId);
    
    /**
     * 查询用户的收藏的资源id
     * @param userId 用户ID
     * @return 收藏详情列表
     */
    List<Integer> findResourceIdsByUserId(Integer userId);
    
    /**
     * 分页查询用户收藏的资源id
     * @param userId 用户ID
     * @param offset 偏移量
     * @param size 每页数量
     * @return 资源ID列表
     */
    List<Integer> findResourceIdsByUserIdWithPagination(@Param("userId") Integer userId, @Param("offset") Integer offset, @Param("size") Integer size);
    
    /**
     * 统计用户收藏数量
     * @param userId 用户ID
     * @return 收藏数量
     */
    int countByUserId(Integer userId);
    
    /**
     * 根据资源ID删除所有收藏记录
     * @param resourceId 资源ID
     * @return 删除的记录数
     */
    int deleteByResourceId(Integer resourceId);
} 