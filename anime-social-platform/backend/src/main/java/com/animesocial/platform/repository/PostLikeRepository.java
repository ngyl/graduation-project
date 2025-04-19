package com.animesocial.platform.repository;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 帖子点赞数据访问接口
 */
@Mapper
public interface PostLikeRepository {
    
    /**
     * 保存点赞记录
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    @Insert("INSERT INTO post_likes (post_id, user_id, created_at) VALUES (#{postId}, #{userId}, NOW())")
    void save(@Param("postId") Integer postId, @Param("userId") Integer userId);
    
    /**
     * 删除点赞记录
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    @Delete("DELETE FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    void delete(@Param("postId") Integer postId, @Param("userId") Integer userId);
    
    /**
     * 删除帖子的所有点赞记录
     * @param postId 帖子ID
     */
    @Delete("DELETE FROM post_likes WHERE post_id = #{postId}")
    void deleteByPostId(Integer postId);
    
    /**
     * 检查点赞记录是否存在
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 存在返回true，否则返回false
     */
    @Select("SELECT COUNT(*) FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean exists(@Param("postId") Integer postId, @Param("userId") Integer userId);
    
    /**
     * 获取帖子的所有点赞用户ID
     * @param postId 帖子ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM post_likes WHERE post_id = #{postId}")
    List<Integer> findUserIdsByPostId(Integer postId);
    
    /**
     * 统计帖子的点赞数量
     * @param postId 帖子ID
     * @return 点赞数量
     */
    @Select("SELECT COUNT(*) FROM post_likes WHERE post_id = #{postId}")
    int countByPostId(Integer postId);
    
    /**
     * 获取用户的所有点赞帖子ID
     * @param userId 用户ID
     * @return 帖子ID列表
     */
    @Select("SELECT post_id FROM post_likes WHERE user_id = #{userId}")
    List<Integer> findPostIdsByUserId(Integer userId);
    
    /**
     * 统计用户的点赞数量
     * @param userId 用户ID
     * @return 点赞数量
     */
    @Select("SELECT COUNT(*) FROM post_likes WHERE user_id = #{userId}")
    int countByUserId(Integer userId);
} 