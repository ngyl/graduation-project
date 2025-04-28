package com.animesocial.platform.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

import java.util.ArrayList;
import java.util.List;

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
        try {
            userService.register(registerRequest);
            return ApiResponse.success("注册成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 用户登录接口
     * 
     * @param loginRequest 登录请求，包含用户名和密码
     * @param session HTTP会话
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            User user = userService.login(loginRequest);
            
            // 如果用户被禁用，拒绝登录
            if (user.getStatus() == 0) {
                return ApiResponse.failed("账号已被禁用，请联系管理员");
            }
            
            // 生成简单的会话令牌
            String token = String.format("%s_%d", user.getUsername(), System.currentTimeMillis());
            
            // 在会话中存储用户信息
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("isAdmin", user.getIsAdmin());
            
            // 设置Spring Security上下文
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (user.getIsAdmin()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, authorities);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(auth);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            
            // 转换为DTO
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            
            // 创建登录响应
            LoginResponse loginResponse = new LoginResponse(token, userDTO);
            
            // 输出会话ID，用于调试
            System.out.println("登录成功，会话ID: " + session.getId());
            
            return ApiResponse.success("登录成功", loginResponse);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息接口
     * 
     * @param session HTTP会话
     * @return 当前登录用户的信息，如果未登录则返回未授权错误
     */
    @GetMapping("/info")
    public ApiResponse<UserDTO> info(HttpSession session) {
        // 输出会话ID，用于调试
        System.out.println("获取用户信息，会话ID: " + session.getId());
        
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            System.out.println("会话中没有userId属性");
            return ApiResponse.unauthorized();
        }
        
        try {
            UserDTO userDTO = userService.getUserDTOById(userId);
            if (userDTO == null) {
                return ApiResponse.unauthorized();
            }
            return ApiResponse.success(userDTO);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 检查结果，true表示已存在，false表示不存在
     */
    @GetMapping("/check-username")
    public ApiResponse<Boolean> checkUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ApiResponse.validateFailed("用户名不能为空");
        }
        
        try {
            boolean exists = userService.checkUsernameExists(username);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
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
            SecurityContextHolder.clearContext();
            session.invalidate();
        }
        return ApiResponse.success("登出成功", null);
    }
} 