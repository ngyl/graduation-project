package com.animesocial.platform.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

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
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.LoginRequest;
import com.animesocial.platform.model.dto.RegisterRequest;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.dto.UpdateUserInfoRequest;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.dto.UserDetailResponse;
import com.animesocial.platform.repository.*;
import com.animesocial.platform.service.TagService;
import com.animesocial.platform.service.UserService;
import com.animesocial.platform.service.UserTagService;
import com.animesocial.platform.service.impl.UserTagServiceImpl;

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
    private UserTagRepository userTagRepository;
    
    @Autowired
    private UserTagService userTagService;
    
    @Autowired
    private TagService tagService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
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
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(null, loginRequest.getUsername())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        User user = userRepository.findByUsername(loginRequest.getUsername());
        
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
            HttpSession session = request.getSession(true); // 确保创建新会话
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("isAdmin", user.getIsAdmin());
            
            // 输出调试信息
            log.info("用户登录成功: {}, 会话ID: {}", user.getUsername(), session.getId());
            log.info("会话中的属性: user={}, userId={}, username={}, isAdmin={}", 
                     session.getAttribute("user") != null, 
                     session.getAttribute("userId"), 
                     session.getAttribute("username"), 
                     session.getAttribute("isAdmin"));
        } else {
            log.warn("无法获取请求上下文，无法创建会话");
        }
        
        // 在返回前隐藏密码
        user.hideSensitiveInfo();
        return user;
    }

    /**
     * 将User实体转换为UserDTO
     * @param user 用户实体
     * @return 用户DTO
     */
    private UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        
        // 获取用户标签
        try {
            List<Tag> tagEntities = userTagRepository.findTagsByUserId(user.getId());
            List<TagDTO> tags = tagEntities.stream()
                .map(tagService::convertToDTO)
                .toList();
            dto.setTags(tags);
        } catch (Exception e) {
            log.error("获取用户标签失败: {}", e.getMessage());
        }
        
        // 获取统计数据
        dto.setPostCount(postRepository.countByUserId(user.getId()));
        dto.setFollowingCount(friendshipRepository.countFollowing(user.getId()));
        dto.setFollowerCount(friendshipRepository.countFollowers(user.getId()));
        dto.setFavoriteCount(favoriteRepository.countByUserId(user.getId()));
        
        return dto;
    }

    @Override
    public UserDTO getUserDTOById(Integer id) {
        User user = userRepository.findById(id);
        return convertToDTO(user);
    }

    @Override
    public UserDTO getByUserDTOname(String username) {
        User user = userRepository.findByUsername(username);
        return convertToDTO(user);
    }

    /**
     * 检查用户名是否已存在
     * 
     * @param username 要检查的用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    @Override
    public boolean isUsernameExist(String username) {
        return userRepository.existsByIdOrUsername(null, username);
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return 当前登录用户的信息DTO，如果未登录则返回null
     */
    @Override
    public UserDTO getCurrentUser() {
        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return null;
        }
        return getUserDTOById(currentUserId);
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
        UserDTO user = convertToDTO(userRepository.findById(id));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 创建响应对象
        UserDetailResponse response = new UserDetailResponse();
        BeanUtils.copyProperties(user, response);
        
        // 获取用户标签
        try {
            List<Tag> tagEntities = userTagRepository.findTagsByUserId(id);
            List<TagDTO> tags = tagEntities.stream()
                .map(tagService::convertToDTO)
                .toList();
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
     * 分页获取用户的帖子列表
     * 
     * @param id 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 帖子列表，按创建时间降序排序
     */
    @Override
    public List<Post> getUserPosts(Integer userId, Integer page, Integer size) {
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        int offset = (page - 1) * size;
        return postRepository.findByUserId(userId, offset, size);
    }

    /**
     *  分页获取用户所上传的资源
     *  @param userId 用户ID
     *  @param page 页码
     *  @param size 每页数量
     *  @return 资源列表，按创建时间降序排序
     */
    @Override
    public List<Resource> getUserResources(Integer userId, Integer page, Integer size) {
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        int offset = (page - 1) * size;
        return resourceRepository.findByUserId(userId, offset, size);
    }

    /**
     * 获取用户的收藏列表
     * 
     * @param id 用户ID
     * @return 资源列表，按收藏时间降序排序
     */
    @Override
    public List<Resource> getUserFavorites(Integer userId, Integer page, Integer size) {
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        int offset = (page - 1) * size;
        return resourceRepository.findFavoritesByUserId(userId, offset, size);
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
    public UserDTO updateUserInfo(Integer id, UpdateUserInfoRequest request) {
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(id, null)) {
            throw new BusinessException("用户不存在");
        }
        
        User user = userRepository.findById(id);
        user.hideSensitiveInfo();
        // 更新用户信息
        user.setAvatar(request.getAvatar());
        user.setBio(request.getBio());
        userRepository.update(user);
        
        // 返回更新后的用户信息
        return getUserDTOById(id);
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
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(id, null)) {
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
        int offset = (page - 1) * size;
        List<User> users = userRepository.findAll(offset, size);
        return users.stream()
            .map(this::convertToDTO)
            .toList();
    }
    
    /**
     * 获取所有管理员用户
     * 
     * @return 管理员用户列表
     */
    @Override
    public List<UserDTO> getAllAdmins() {
        List<User> admins = userRepository.findAllAdmins();
        return admins.stream()
            .map(this::convertToDTO)
            .toList();
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
        UserDTO user = convertToDTO(userRepository.findById(userId));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 获取用户的标签
        List<Integer> userTagIds = userTagService.getUserTags(userId).stream()
                .map(Tag::getId)
                .toList();
        
        if (userTagIds.isEmpty()) {
            // 如果用户没有标签，返回随机用户（排除已关注的用户）
            List<User> randomUsers = userRepository.findRandomUsers(limit * 2); // 获取更多随机用户，以防过滤后数量不足
            return filterFollowingUsers(userId, randomUsers, limit);
        }
        
        // 获取与当前用户相似的用户
        UserTagServiceImpl userTagServiceImpl = (UserTagServiceImpl) userTagService;
        List<Integer> similarUserIds = userTagServiceImpl.getSimilarUsers(userId, limit * 2); // 获取更多相似用户，以防过滤后数量不足
        
        // 如果没有找到相似用户，返回随机用户（排除已关注的用户）
        if (similarUserIds.isEmpty()) {
            List<User> randomUsers = userRepository.findRandomUsers(limit * 2);
            return filterFollowingUsers(userId, randomUsers, limit);
        }
        
        // 获取用户详情，并排除已关注的用户
        List<User> recommendedUsers = new ArrayList<>();
        for (Integer recommendedUserId : similarUserIds) {
            User recommendedUser = userRepository.findById(recommendedUserId);
            if (recommendedUser != null) {
                recommendedUsers.add(recommendedUser);
            }
        }
        
        return filterFollowingUsers(userId, recommendedUsers, limit);
    }
    
    /**
     * 过滤已关注的用户
     * 
     * @param userId 当前用户ID
     * @param users 用户列表
     * @param limit 限制数量
     * @return 过滤后的用户DTO列表
     */
    private List<UserDTO> filterFollowingUsers(Integer userId, List<User> users, Integer limit) {
        // 获取当前用户已关注的所有用户ID
        List<Integer> followingIds = friendshipRepository.findFollowingIds(userId);
        
        // 过滤掉已关注的用户，并限制返回数量
        return users.stream()
                .filter(user -> !followingIds.contains(user.getId())) // 排除已关注的用户
                .filter(user -> !user.getId().equals(userId)) // 排除自己
                .limit(limit != null && limit > 0 ? limit : 10)
                .map(this::convertToDTO)
                .toList();
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
        return userRepository.findByUsernameOrBioLike(searchPattern, offset, size).stream()
                .map(this::convertToDTO)
                .toList();   
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
     * 关注或取消关注用户
     * 根据当前关注状态自动判断：
     * - 如果未关注，则创建关注关系
     * - 如果已关注，则取消关注
     * 
     * @param userId 当前用户ID
     * @param targetId 目标用户ID
     * @return 操作结果描述，例如"关注成功"或"已取消关注"
     */
    @Override
    @Transactional
    public String toggleFollow(Integer userId, Integer targetId) {
        // 验证当前用户
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证目标用户
        if (!userRepository.existsByIdOrUsername(targetId, null)) {
            throw new BusinessException("目标用户不存在");
        }
        
        // 检查是否自己关注自己
        if (userId.equals(targetId)) {
            throw new BusinessException("不能关注自己");
        }
        
        // 检查当前关注状态
        boolean isFollowing = friendshipRepository.exists(userId, targetId);
        
        if (isFollowing) {
            // 已关注，执行取消关注操作
            boolean isFollowedBack = friendshipRepository.exists(targetId, userId);
            if (isFollowedBack) {
                // 如果对方关注了自己，将对方的关注状态改为单向关注
                friendshipRepository.updateStatus(targetId, userId, 0);
            }
            
            // 删除关注关系
            friendshipRepository.delete(userId, targetId);
            return "已取消关注";
        } else {
            // 未关注，执行关注操作
            friendshipRepository.insert(userId, targetId);
            
            // 检查对方是否已关注自己，如果是则互相关注
            boolean isFollowedBack = friendshipRepository.exists(targetId, userId);
            if (isFollowedBack) {
                // 更新为互相关注状态
                friendshipRepository.updateStatus(userId, targetId, 1);
                friendshipRepository.updateStatus(targetId, userId, 1);
                return "互相关注成功";
            }
            
            return "关注成功";
        }
    }

    /**
     * 分页获取用户的关注列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 关注的用户列表
     */
    @Override
    public List<UserDTO> getUserFollowing(Integer userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<User> users = userRepository.findFollowingByUserId(userId, offset, size);
        return users.stream()
            .map(this::convertToDTO)
            .toList();
    }

    /**
     * 分页获取用户的粉丝列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 粉丝用户列表
     */
    @Override
    public List<UserDTO> getUserFollowers(Integer userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<User> users = userRepository.findFollowersByUserId(userId, offset, size);
        return users.stream()
            .map(this::convertToDTO)
            .toList();
    }

    /**
     * 获取用户相互关注列表
     * @param userId 用户ID
     * @return 相互关注用户列表
     */
    @Override
    public List<UserDTO> getMutualFriends(Integer userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<User> users = userRepository.findMutualFriendsByUserId(userId, offset, size);
        return users.stream()
            .map(this::convertToDTO)
            .toList();
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

    /**
     * 获取用户总数
     * @return 用户总数
     */
    @Override
    public int countUsers() {
        return userRepository.count();
    }
    
    /**
     * 根据ID列表批量查询用户
     * @param ids 用户ID列表
     * @return 用户DTO列表
     */
    @Override
    public List<UserDTO> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<UserDTO> users = new ArrayList<>();
        for (Integer id : ids) {
            User user = userRepository.findById(id);
            if (user != null) {
                users.add(convertToDTO(user));
            }
        }
        
        return users;
    }

    
} 