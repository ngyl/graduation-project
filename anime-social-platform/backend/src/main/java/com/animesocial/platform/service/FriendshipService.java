package com.animesocial.platform.service;

import com.animesocial.platform.model.dto.UserDTO;

import java.util.List;

/**
 * 好友关系服务接口
 * 处理用户间关注、粉丝等社交关系
 */
public interface FriendshipService {
    
    /**
     * 关注用户
     * 
     * @param userId 用户ID
     * @param targetId 被关注用户ID
     * @return 操作结果描述
     * @throws RuntimeException 如果用户不存在或者不能关注自己
     */
    String follow(Integer userId, Integer targetId);
    
    /**
     * 取消关注用户
     * 
     * @param userId 用户ID
     * @param targetId 被关注用户ID
     * @return 操作结果描述
     * @throws RuntimeException 如果用户不存在或关注关系不存在
     */
    String unfollow(Integer userId, Integer targetId);
    
    /**
     * 分页获取用户的关注列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 关注的用户列表
     */
    List<UserDTO> getFollowing(Integer userId, Integer page, Integer size);
    
    /**
     * 分页获取用户的粉丝列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 粉丝用户列表
     */
    List<UserDTO> getFollowers(Integer userId, Integer page, Integer size);
    
    /**
     * 检查是否已关注
     * 
     * @param userId 用户ID
     * @param targetId 目标用户ID
     * @return true表示已关注，false表示未关注
     */
    boolean isFollowing(Integer userId, Integer targetId);
    
    /**
     * 检查是否互相关注
     * 
     * @param userId 用户ID
     * @param targetId 目标用户ID
     * @return true表示互相关注，false表示非互相关注
     */
    boolean isMutualFollow(Integer userId, Integer targetId);
    
    /**
     * 获取用户的关注数量
     * 
     * @param userId 用户ID
     * @return 关注数量
     */
    int countFollowing(Integer userId);
    
    /**
     * 获取用户的粉丝数量
     * 
     * @param userId 用户ID
     * @return 粉丝数量
     */
    int countFollowers(Integer userId);
} 