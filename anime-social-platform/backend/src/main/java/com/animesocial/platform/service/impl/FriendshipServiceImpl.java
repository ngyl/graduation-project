package com.animesocial.platform.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.repository.FriendshipRepository;
import com.animesocial.platform.service.FriendshipService;
import com.animesocial.platform.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * 好友关系服务实现类
 * 实现FriendshipService接口定义的所有业务方法
 */
@Service
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * 关注用户
     * 
     * @param userId 用户ID
     * @param targetId 被关注用户ID
     * @return 操作结果描述
     * @throws BusinessException 如果用户不存在或不能关注自己
     */
    @Override
    @Transactional
    public String follow(Integer userId, Integer targetId) {
        if (userId.equals(targetId)) {
            throw new BusinessException("不能关注自己");
        }
        
        UserDTO targetUser = userService.getUserDTOById(targetId);
        if (targetUser == null) {
            throw new BusinessException("目标用户不存在");
        }
        
        boolean isFollowing = friendshipRepository.exists(userId, targetId);
        if (isFollowing) {
            throw new BusinessException("已经关注该用户");
        }
        
        friendshipRepository.insert(userId, targetId);
        
        // 检查是否为互相关注
        boolean isFollowedBy = friendshipRepository.exists(targetId, userId);
        if (isFollowedBy) {
            // 更新双方的关注状态为互相关注
            friendshipRepository.updateStatus(userId, targetId, 1);
            friendshipRepository.updateStatus(targetId, userId, 1);
        }
        
        return "关注成功";
    }
    
    /**
     * 取消关注用户
     * 
     * @param userId 用户ID
     * @param targetId 被关注用户ID
     * @return 操作结果描述
     * @throws BusinessException 如果用户不存在或关注关系不存在
     */
    @Override
    @Transactional
    public String unfollow(Integer userId, Integer targetId) {
        UserDTO targetUser = userService.getUserDTOById(targetId);
        if (targetUser == null) {
            throw new BusinessException("目标用户不存在");
        }
        
        boolean isFollowing = friendshipRepository.exists(userId, targetId);
        if (!isFollowing) {
            throw new BusinessException("未关注该用户");
        }
        
        // 检查之前是否为互相关注
        boolean isMutual = friendshipRepository.isMutualFollow(userId, targetId);
        if (isMutual) {
            // 更新另一方的关系状态为单向关注
            friendshipRepository.updateStatus(targetId, userId, 0);
        }
        
        // 删除关注关系
        friendshipRepository.delete(userId, targetId);
        
        return "取消关注成功";
    }
    
    /**
     * 获取用户的关注列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 关注的用户列表
     */
    @Override
    public List<UserDTO> getFollowing(Integer userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<UserDTO> users = userService.getUserFollowing(userId, offset, size);
        
        return users.stream()
            .map(user -> {
                UserDTO dto = new UserDTO();
                BeanUtils.copyProperties(user, dto);
                return dto;
            })
            .toList();
    }
    
    /**
     * 分页获取用户的粉丝列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 粉丝用户列表
     */
    @Override
    public List<UserDTO> getFollowers(Integer userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<UserDTO> users = userService.getUserFollowers(userId, offset, size);
        
        return users.stream()
            .map(user -> {
                UserDTO dto = new UserDTO();
                BeanUtils.copyProperties(user, dto);
                return dto;
            })
            .toList();
    }
    
    /**
     * 检查是否已关注
     * 
     * @param userId 用户ID
     * @param targetId 目标用户ID
     * @return true表示已关注，false表示未关注
     */
    @Override
    public boolean isFollowing(Integer userId, Integer targetId) {
        return friendshipRepository.exists(userId, targetId);
    }
    
    /**
     * 检查是否互相关注
     * 
     * @param userId 用户ID
     * @param targetId 目标用户ID
     * @return true表示互相关注，false表示非互相关注
     */
    @Override
    public boolean isMutualFollow(Integer userId, Integer targetId) {
        return friendshipRepository.isMutualFollow(userId, targetId);
    }
    
    /**
     * 获取用户的关注数量
     * 
     * @param userId 用户ID
     * @return 关注数量
     */
    @Override
    public int countFollowing(Integer userId) {
        return friendshipRepository.countFollowing(userId);
    }
    
    /**
     * 获取用户的粉丝数量
     * 
     * @param userId 用户ID
     * @return 粉丝数量
     */
    @Override
    public int countFollowers(Integer userId) {
        return friendshipRepository.countFollowers(userId);
    }
} 