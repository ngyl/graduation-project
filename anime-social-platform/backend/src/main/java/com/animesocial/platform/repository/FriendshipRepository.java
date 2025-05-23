package com.animesocial.platform.repository;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 好友关系数据访问接口
 * 使用MyBatis注解方式定义SQL操作，处理用户之间关注关系相关的数据库操作
 */
@Mapper
public interface FriendshipRepository {
    
    /**
     * 统计用户的关注数量
     * @param userId 用户ID
     * @return 关注数量
     */
    @Select("SELECT COUNT(*) FROM friendships WHERE user_id = #{userId}")
    int countFollowing(Integer userId);
    
    /**
     * 统计用户的粉丝数量
     * @param userId 用户ID
     * @return 粉丝数量
     */
    @Select("SELECT COUNT(*) FROM friendships WHERE friend_id = #{userId}")
    int countFollowers(Integer userId);
    
    /**
     * 检查是否存在关注关系
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 存在返回true，不存在返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM friendships WHERE user_id = #{userId} AND friend_id = #{friendId}")
    boolean exists(@Param("userId") Integer userId, @Param("friendId") Integer friendId);
    
    /**
     * 检查是否为互相关注
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 互相关注返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM friendships WHERE user_id = #{userId} AND friend_id = #{friendId} AND status = 1")
    boolean isMutualFollow(@Param("userId") Integer userId, @Param("friendId") Integer friendId);
    
    /**
     * 添加关注关系
     * @param userId 用户ID
     * @param friendId 好友ID
     */
    @Insert("INSERT INTO friendships (user_id, friend_id, status, created_at) VALUES (#{userId}, #{friendId}, 0, NOW())")
    void insert(@Param("userId") Integer userId, @Param("friendId") Integer friendId);
    
    /**
     * 更新关注关系状态
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param status 关系状态(0单向关注,1互相关注)
     */
    @Update("UPDATE friendships SET status = #{status} WHERE user_id = #{userId} AND friend_id = #{friendId}")
    void updateStatus(@Param("userId") Integer userId, @Param("friendId") Integer friendId, @Param("status") Integer status);
    
    /**
     * 取消关注关系
     * @param userId 用户ID
     * @param friendId 好友ID
     */
    @Delete("DELETE FROM friendships WHERE user_id = #{userId} AND friend_id = #{friendId}")
    void delete(@Param("userId") Integer userId, @Param("friendId") Integer friendId);
    
    /**
     * 获取用户关注的用户ID列表
     * @param userId 用户ID
     * @return 关注的用户ID列表
     */
    @Select("SELECT friend_id FROM friendships WHERE user_id = #{userId}")
    List<Integer> findFollowingIds(Integer userId);
    
    /**
     * 获取关注用户的用户ID列表
     * @param userId 用户ID
     * @return 粉丝用户ID列表
     */
    @Select("SELECT user_id FROM friendships WHERE friend_id = #{userId}")
    List<Integer> findFollowerIds(Integer userId);

    /**
     * 获取用户互相关注的用户ID列表
     * @param userId 用户ID
     * @return 互相关注的用户ID列表
     */
    @Select("SELECT friend_id FROM friendships WHERE user_id = #{userId} AND status = 1")
    List<Integer> findMutualIds(Integer userId);
    
    /**
     * 获取用户的好友关系状态
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 关系状态(0单向关注,1互相关注), 如果不存在关系则返回null
     */
    @Select("SELECT status FROM friendships WHERE user_id = #{userId} AND friend_id = #{friendId}")
    Integer getStatus(@Param("userId") Integer userId, @Param("friendId") Integer friendId);
    
    /**
     * 批量获取用户的关注状态
     * @param userId 用户ID
     * @param friendIds 好友ID列表
     * @return 好友ID与关注状态的映射
     */
    @Select({
        "<script>",
        "SELECT friend_id, status FROM friendships",
        "WHERE user_id = #{userId} AND friend_id IN",
        "<foreach item='item' collection='friendIds' open='(' separator=',' close=')'>",
        "#{item}",
        "</foreach>",
        "</script>"
    })
    @Results({
        @Result(property = "key", column = "friend_id"),
        @Result(property = "value", column = "status")
    })
    List<java.util.Map.Entry<Integer, Integer>> getStatusBatch(@Param("userId") Integer userId, @Param("friendIds") List<Integer> friendIds);
} 