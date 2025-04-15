package com.animesocial.platform.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.LoginRequest;
import com.animesocial.platform.model.dto.LoginResponse;
import com.animesocial.platform.model.dto.RegisterRequest;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * 认证控制器
 * 
 * 处理用户认证相关的请求，包括：
 * 1. 用户注册
 * 2. 用户登录
 * 3. 获取当前用户信息
 * 4. 用户登出
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    /**
     * 用户注册接口
     * 
     * @param registerRequest 注册请求，包含用户名、密码等信息
     * @return 注册结果，成功返回成功消息，失败返回错误信息
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.register(registerRequest);
        return ApiResponse.success("注册成功", null);
    }
    
    /**
     * 用户登录接口
     * 
     * @param loginRequest 登录请求，包含用户名和密码
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest);
        
        // 生成简单的token，实际项目中应使用JWT等方式
        String token = String.format("%s_%d", user.getUsername(), System.currentTimeMillis());
        
        // 转换为DTO
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        
        // 创建登录响应
        LoginResponse loginResponse = new LoginResponse(token, userDTO);
        
        return ApiResponse.success("登录成功", loginResponse);
    }
    
    /**
     * 获取当前用户信息接口
     * 
     * @return 当前登录用户的信息，如果未登录则返回未授权错误
     */
    @GetMapping("/info")
    public ApiResponse<UserDTO> info() {
        UserDTO userDTO = userService.getCurrentUser();
        if (userDTO == null) {
            return ApiResponse.unauthorized();
        }
        return ApiResponse.success(userDTO);
    }
    
    /**
     * 用户登出接口
     * 
     * @param request HTTP请求对象
     * @return 登出结果，成功返回成功消息
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.success("登出成功", null);
    }
} 