package com.animesocial.platform.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.LoginRequest;
import com.animesocial.platform.model.dto.RegisterRequest;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.dto.UpdateUserInfoRequest;
import com.animesocial.platform.model.dto.UpdateUserTagsRequest;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.dto.UserDetailResponse;
import com.animesocial.platform.repository.*;
import com.animesocial.platform.service.TagService;
import com.animesocial.platform.service.UserService;
import com.animesocial.platform.service.UserTagService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务实现类
 * 实现UserService接口定义的所有业务方法
 * 使用@Transactional注解确保数据一致性
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private FriendshipRepository friendshipRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private UserTagService userTagService;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户注册实现
     * 1. 验证密码一致性
     * 2. 检查用户名是否已存在
     * 3. 创建新用户并保存
     * 
     * @param registerRequest 注册请求对象
     * @throws BusinessException 如果密码不一致或用户名已存在
     */
    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        // 验证密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        
        // 检查用户名是否已存在
        if (isUsernameExist(registerRequest.getUsername())) {
            throw new BusinessException("用户名已被使用");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setIsAdmin(false);
        user.setStatus(1); // 正常状态
        user.setRegisterTime(LocalDateTime.now());
        
        // 保存用户
        userRepository.insert(user);
        
        // 如果注册请求包含标签，则添加用户标签
        if (registerRequest.getTagIds() != null && !registerRequest.getTagIds().isEmpty()) {
            for (Integer tagId : registerRequest.getTagIds()) {
                try {
                    // 验证标签并添加
                    tagService.addUserTag(user.getId(), tagId);
                } catch (BusinessException e) {
                    // 记录错误但不中断流程
                    log.error("添加用户标签失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 用户登录实现
     * 1. 验证用户名和密码
     * 2. 检查用户状态
     * 3. 更新最后登录时间
     * 4. 将用户信息存入session
     * 
     * @param loginRequest 登录请求对象
     * @return 登录成功的用户对象
     * @throws BusinessException 如果用户名或密码错误，或用户被禁用
     */
    @Override
    public User login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername());
        
        // 验证用户是否存在
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证用户是否被禁用
        if (user.getStatus() != 1) {
            throw new BusinessException("该账号已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.updateLastLoginTime(user);
        
        // 将用户信息存入session
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
        }
        
        // 在返回前隐藏密码
        user.hideSensitiveInfo();
        return user;
    }

    /**
     * 根据ID获取用户信息
     * 
     * @param id 用户ID
     * @return 用户DTO对象，如果用户不存在则返回null
     */
    @Override
    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id);
        if (user == null) {
            return null;
        }
        
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        
        // 获取用户标签
        try {
            List<TagDTO> tags = tagService.getUserTags(id).getTags();
            userDTO.setTags(tags);
        } catch (Exception e) {
            log.error("获取用户标签失败: {}", e.getMessage());
        }
        
        return userDTO;
    }

    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return 用户对象，如果用户不存在则返回null
     */
    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 检查用户名是否已存在
     * 
     * @param username 要检查的用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    @Override
    public boolean isUsernameExist(String username) {
        return userRepository.findByUsername(username) != null;
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return 当前登录用户的信息DTO，如果未登录则返回null
     */
    @Override
    public UserDTO getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return null;
        }
        
        return getUserById(user.getId());
    }

    /**
     * 获取用户详情和统计数据
     * 1. 获取用户基本信息
     * 2. 统计帖子数量
     * 3. 统计收藏数量
     * 4. 统计关注数量
     * 
     * @param id 用户ID
     * @return 用户详情响应对象
     * @throws BusinessException 如果用户不存在
     */
    @Override
    public UserDetailResponse getUserDetail(Integer id) {
        // 验证用户是否存在
        User user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 隐藏敏感信息
        user.hideSensitiveInfo();
        
        // 创建响应对象
        UserDetailResponse response = new UserDetailResponse();
        BeanUtils.copyProperties(user, response);
        
        // 获取用户标签
        try {
            List<TagDTO> tags = tagService.getUserTags(id).getTags();
            response.setTags(tags);
        } catch (Exception e) {
            log.error("获取用户标签失败: {}", e.getMessage());
            response.setTags(new ArrayList<>());
        }
        
        // 获取统计数据
        int postCount = postRepository.countByUserId(id);
        int favoriteCount = favoriteRepository.countByUserId(id);
        int followerCount = friendshipRepository.countFollowers(id);
        int followingCount = friendshipRepository.countFollowing(id);
        
        // 设置统计数据
        response.setPostCount(postCount);
        response.setFavoriteCount(favoriteCount);
        response.setFollowerCount(followerCount);
        response.setFollowingCount(followingCount);
        
        // 检查当前用户是否已关注此用户
        Integer currentUserId = getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(id)) {
            boolean isFollowing = friendshipRepository.exists(currentUserId, id);
            response.setIsFollowing(isFollowing);
        } else {
            response.setIsFollowing(false);
        }
        
        return response;
    }

    /**
     * 获取用户的帖子列表
     * 
     * @param id 用户ID
     * @return 帖子列表，按创建时间降序排序
     */
    @Override
    public List<Post> getUserPosts(Integer userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        return postRepository.findByUserId(userId);
    }

    /**
     * 获取用户的收藏列表
     * 
     * @param id 用户ID
     * @return 资源列表，按收藏时间降序排序
     */
    @Override
    public List<Resource> getUserFavorites(Integer userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        return resourceRepository.findByUserId(userId);
    }

    /**
     * 更新用户信息
     * 1. 验证用户是否存在
     * 2. 更新头像和个人简介
     * 3. 保存更新后的用户信息
     * 
     * @param id 用户ID
     * @param request 更新用户信息请求对象
     * @return 更新后的用户对象
     * @throws BusinessException 如果用户不存在
     */
    @Override
    @Transactional
    public User updateUserInfo(Integer id, UpdateUserInfoRequest request) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新用户信息
        user.setAvatar(request.getAvatar());
        user.setBio(request.getBio());
        
        userRepository.update(user);
        
        // 更新用户标签
        if (request.getTagIds() != null) {
            try {
                // 创建UpdateUserTagsRequest对象
                UpdateUserTagsRequest tagsRequest = new UpdateUserTagsRequest();
                tagsRequest.setTagIds(request.getTagIds());
                tagService.updateUserTags(id, tagsRequest);
            } catch (Exception e) {
                log.error("更新用户标签失败: {}", e.getMessage());
            }
        }
        
        // 在返回前隐藏密码
        user.hideSensitiveInfo();
        return user;
    }
    
    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param status 用户状态(0禁用,1正常)
     * @throws BusinessException 如果用户不存在
     */
    @Override
    public void updateUserStatus(Integer id, Integer status) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证状态值合法性
        if (status != 0 && status != 1) {
            throw new BusinessException("无效的状态值");
        }
        
        // 更新用户状态
        userRepository.updateStatus(id, status);
    }
    
    /**
     * 分页获取所有用户
     * 
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    @Override
    public List<UserDTO> getAllUsers(Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取用户列表
        List<User> users = userRepository.findAll(offset, size);
        
        // 转换为DTO
        return users.stream()
            .map(user -> {
                UserDTO dto = new UserDTO();
                BeanUtils.copyProperties(user, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有管理员用户
     * 
     * @return 管理员用户列表
     */
    @Override
    public List<UserDTO> getAllAdmins() {
        // 获取管理员用户列表
        List<User> admins = userRepository.findAllAdmins();
        
        // 转换为DTO
        return admins.stream()
            .map(user -> {
                UserDTO dto = new UserDTO();
                BeanUtils.copyProperties(user, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 根据兴趣标签推荐用户
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐用户列表
     */
    @Override
    public List<UserDTO> getRecommendedUsers(Integer userId, Integer limit) {
        // 验证用户存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 获取用户的标签
        List<Integer> userTagIds = userTagService.getUserTags(userId).stream()
                .map(tag -> tag.getId())
                .collect(Collectors.toList());
        
        if (userTagIds.isEmpty()) {
            // 如果用户没有标签，返回随机用户
            List<User> randomUsers = userRepository.findRandomUsers(limit);
            return convertUsersToDTO(randomUsers);
        }
        
        // 查找与这些标签相关的其他用户
        List<Integer> similarUserIds = new ArrayList<>();
        for (Integer tagId : userTagIds) {
            List<Integer> userIds = userTagService.getUsersWithTag(tagId);
            similarUserIds.addAll(userIds);
        }
        
        // 过滤掉当前用户
        similarUserIds.removeIf(id -> id.equals(userId));
        
        // 如果没有找到相似用户，返回随机用户
        if (similarUserIds.isEmpty()) {
            List<User> randomUsers = userRepository.findRandomUsers(limit);
            return convertUsersToDTO(randomUsers);
        }
        
        // 统计相同标签数量
        Map<Integer, Integer> userTagCounts = new HashMap<>();
        for (Integer similarUserId : similarUserIds) {
            userTagCounts.put(similarUserId, userTagCounts.getOrDefault(similarUserId, 0) + 1);
        }
        
        // 按标签匹配度排序
        List<Integer> sortedUserIds = userTagCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 获取用户详情
        List<User> recommendedUsers = new ArrayList<>();
        for (Integer recommendedUserId : sortedUserIds) {
            User recommendedUser = userRepository.findById(recommendedUserId);
            if (recommendedUser != null) {
                recommendedUsers.add(recommendedUser);
            }
        }
        
        return convertUsersToDTO(recommendedUsers);
    }
    
    /**
     * 搜索用户
     * 
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    @Override
    public List<UserDTO> searchUsers(String keyword, Integer page, Integer size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers(page, size);
        }
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 在用户名和个人简介中搜索
        String searchPattern = "%" + keyword.trim() + "%";
        List<User> users = userRepository.findByUsernameOrBioLike(searchPattern, offset, size);
        
        return convertUsersToDTO(users);
    }
    
    /**
     * 将用户列表转换为DTO列表
     */
    private List<UserDTO> convertUsersToDTO(List<User> users) {
        return users.stream()
            .map(user -> {
                UserDTO dto = new UserDTO();
                BeanUtils.copyProperties(user, dto);
                
                // 获取用户标签
                try {
                    List<TagDTO> tags = tagService.getUserTags(user.getId()).getTags();
                    dto.setTags(tags);
                } catch (Exception e) {
                    log.error("获取用户标签失败: {}", e.getMessage());
                }
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID，如果未登录则返回null
     */
    private Integer getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    return user.getId();
                }
            }
        }
        return null;
    }

    /**
     * 检查用户名是否已存在（提供给控制器调用的方法）
     * @param username 用户名
     * @return 如果用户名已存在返回true，否则返回false
     */
    @Override
    public boolean checkUsernameExists(String username) {
        return isUsernameExist(username);
    }

    /**
     * 关注用户
     * @param userId 当前用户ID
     * @param followId 被关注用户ID
     * @return 操作结果描述
     */
    @Override
    @Transactional
    public String followUser(Integer userId, Integer followId) {
        // 验证当前用户
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证被关注用户
        User followUser = userRepository.findById(followId);
        if (followUser == null) {
            throw new BusinessException("要关注的用户不存在");
        }
        
        // 检查是否自己关注自己
        if (userId.equals(followId)) {
            throw new BusinessException("不能关注自己");
        }
        
        // 检查是否已经关注
        boolean isFollowing = friendshipRepository.exists(userId, followId);
        if (isFollowing) {
            throw new BusinessException("已经关注过该用户");
        }
        
        // 创建关注关系
        friendshipRepository.insert(userId, followId);
        
        // 检查对方是否已关注自己，如果是则互相关注
        boolean isFollowedBack = friendshipRepository.exists(followId, userId);
        if (isFollowedBack) {
            // 更新为互相关注状态
            friendshipRepository.updateStatus(userId, followId, 1);
            friendshipRepository.updateStatus(followId, userId, 1);
            return "互相关注成功";
        }
        
        return "关注成功";
    }

    /**
     * 取消关注
     * @param userId 当前用户ID
     * @param followId 被取消关注用户ID
     * @return 操作结果描述
     */
    @Override
    @Transactional
    public String unfollowUser(Integer userId, Integer followId) {
        // 验证关注关系是否存在
        boolean isFollowing = friendshipRepository.exists(userId, followId);
        if (!isFollowing) {
            throw new BusinessException("未关注该用户");
        }
        
        // 检查是否互相关注
        boolean isFollowedBack = friendshipRepository.exists(followId, userId);
        if (isFollowedBack) {
            // 如果对方关注了自己，将对方的关注状态改为单向关注
            friendshipRepository.updateStatus(followId, userId, 0);
        }
        
        // 删除关注关系
        friendshipRepository.delete(userId, followId);
        
        return "已取消关注";
    }

    /**
     * 获取用户的关注列表
     * @param userId 用户ID
     * @return 关注的用户列表
     */
    @Override
    public List<User> getUserFollowing(Integer userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 获取关注的用户ID列表
        List<Integer> followingIds = friendshipRepository.findFollowingIds(userId);
        
        // 获取用户信息
        List<User> followingUsers = new ArrayList<>();
        for (Integer followId : followingIds) {
            User followUser = userRepository.findById(followId);
            if (followUser != null) {
                // 隐藏敏感信息
                followUser.hideSensitiveInfo();
                followingUsers.add(followUser);
            }
        }
        
        return followingUsers;
    }

    /**
     * 获取用户的粉丝列表
     * @param userId 用户ID
     * @return 粉丝用户列表
     */
    @Override
    public List<User> getUserFollowers(Integer userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 获取粉丝用户ID列表
        List<Integer> followerIds = friendshipRepository.findFollowerIds(userId);
        
        // 获取用户信息
        List<User> followers = new ArrayList<>();
        for (Integer followerId : followerIds) {
            User follower = userRepository.findById(followerId);
            if (follower != null) {
                // 隐藏敏感信息
                follower.hideSensitiveInfo();
                followers.add(follower);
            }
        }
        
        return followers;
    }

    /**
     * 检查是否已关注用户
     * @param userId 当前用户ID
     * @param followId 被关注用户ID
     * @return true表示已关注，false表示未关注
     */
    @Override
    public boolean isFollowing(Integer userId, Integer followId) {
        return friendshipRepository.exists(userId, followId);
    }
} 