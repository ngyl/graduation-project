package com.animesocial.platform.service;

import java.util.List;

import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.LoginRequest;
import com.animesocial.platform.model.dto.RegisterRequest;
import com.animesocial.platform.model.dto.UpdateUserInfoRequest;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.dto.UserDetailResponse;

/**
 * 用户服务接口
 * 定义用户相关的业务操作，包括用户注册、登录、信息查询、统计等功能
 */
public interface UserService {
    /**
     * 用户注册
     * 验证用户名是否已存在，密码是否一致，创建新用户
     * 
     * @param registerRequest 注册请求，包含用户名、密码等信息
     * @throws RuntimeException 如果用户名已存在或注册失败
     */
    void register(RegisterRequest registerRequest);
    
    /**
     * 用户登录
     * 验证用户名和密码，更新最后登录时间
     * 
     * @param loginRequest 登录请求，包含用户名和密码
     * @return 登录成功的用户信息
     * @throws RuntimeException 如果用户名或密码错误
     */
    User login(LoginRequest loginRequest);
    
    /**
     * 根据ID获取用户信息
     * 
     * @param id 用户ID
     * @return 用户信息DTO，如果用户不存在则返回null
     */
    UserDTO getUserDTOById(Integer id);
    
    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return 用户实体对象，如果用户不存在则返回null
     */
    UserDTO getByUserDTOname(String username);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 要检查的用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    boolean isUsernameExist(String username);
    
    /**
     * 检查用户名是否已存在（提供给控制器调用的方法）
     * 
     * @param username 要检查的用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    boolean checkUsernameExists(String username);
    
    /**
     * 获取当前登录用户信息
     * 
     * @return 当前登录用户的信息DTO，如果未登录则返回null
     */
    UserDTO getCurrentUser();
    
    /**
     * 获取用户详情和统计数据
     * 包括用户基本信息、帖子数量、收藏数量、关注数量等
     * 
     * @param id 用户ID
     * @return 用户详情和统计信息
     * @throws RuntimeException 如果用户不存在
     */
    UserDetailResponse getUserDetail(Integer id);
    
    /**
     * 分页获取用户的帖子列表
     * 
     * @param id 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 帖子列表，按创建时间降序排序
     */
    List<Post> getUserPosts(Integer id, Integer page, Integer size);

    /**
     * 分页获取用户所上传的资源
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 资源列表，按创建时间降序排序
     */
    List<Resource> getUserResources(Integer userId, Integer page, Integer size);
    
    /**
     * 分页获取用户的收藏列表
     * 
     * @param id 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 资源列表，按收藏时间降序排序
     */
    List<Resource> getUserFavorites(Integer id, Integer page, Integer size);
    
    /**
     * 更新用户信息
     * 可以更新头像和个人简介
     * 
     * @param id 用户ID
     * @param request 更新用户信息请求对象
     * @return 更新后的用户对象
     * @throws RuntimeException 如果用户不存在
     */
    UserDTO updateUserInfo(Integer id, UpdateUserInfoRequest request);
    
    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param status 用户状态(0禁用,1正常)
     * @throws RuntimeException 如果用户不存在
     */
    void updateUserStatus(Integer id, Integer status);
    
    /**
     * 分页获取所有用户
     * 
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    List<UserDTO> getAllUsers(Integer page, Integer size);
    
    /**
     * 获取所有管理员用户
     * 
     * @return 管理员用户列表
     */
    List<UserDTO> getAllAdmins();
    
    /**
     * 根据兴趣标签推荐用户
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐用户列表
     */
    List<UserDTO> getRecommendedUsers(Integer userId, Integer limit);
    
    /**
     * 搜索用户
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @return 匹配的用户列表
     */
    List<UserDTO> searchUsers(String keyword, Integer page, Integer size);
    
    /**
     * 关注或取消关注用户
     * 根据当前关注状态自动判断：
     * - 如果未关注，则创建关注关系
     * - 如果已关注，则取消关注
     * 
     * @param userId 当前用户ID
     * @param targetId 目标用户ID
     * @return 操作结果描述，例如"关注成功"或"已取消关注"
     * @throws RuntimeException 如果用户不存在或不能关注自己
     */
    String toggleFollow(Integer userId, Integer targetId);
    
    /**
     * 获取用户的关注列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 关注的用户列表
     */
    List<UserDTO> getUserFollowing(Integer userId, Integer page, Integer size);
    
    /**
     * 获取用户的粉丝列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 粉丝用户列表
     */
    List<UserDTO> getUserFollowers(Integer userId, Integer page, Integer size);
    
    /**
     * 获取用户相互关注列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 相互关注用户列表
     */
    List<UserDTO> getMutualFriends(Integer userId, Integer page, Integer size);
    
    /**
     * 检查是否已关注用户
     * 
     * @param userId 当前用户ID
     * @param followId 被关注用户ID
     * @return true表示已关注，false表示未关注
     */
    boolean isFollowing(Integer userId, Integer followId);
    
    /**
     * 获取用户总数
     * @return 用户总数
     */
    int countUsers();
    
    /**
     * 根据ID列表批量查询用户
     * @param ids 用户ID列表
     * @return 用户DTO列表
     */
    List<UserDTO> findByIds(List<Integer> ids);
} 