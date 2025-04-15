package com.animesocial.platform.service;

import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.LoginRequest;
import com.animesocial.platform.model.dto.RegisterRequest;
import com.animesocial.platform.model.dto.UserDTO;

/**
 * 用户服务接口
 * 
 * 定义用户相关的业务操作，包括：
 * 1. 用户注册
 * 2. 用户登录
 * 3. 用户信息查询
 * 4. 用户名检查
 */
public interface UserService {
    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求，包含用户名、密码等信息
     * @throws RuntimeException 如果用户名已存在或注册失败
     */
    void register(RegisterRequest registerRequest);
    
    /**
     * 用户登录
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
    UserDTO getUserById(Integer id);
    
    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return 用户实体对象，如果用户不存在则返回null
     */
    User getByUsername(String username);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 要检查的用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    boolean isUsernameExist(String username);
    
    /**
     * 获取当前登录用户信息
     * 
     * @return 当前登录用户的信息DTO，如果未登录则返回null
     */
    UserDTO getCurrentUser();
} 