package com.animesocial.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应DTO
 * 
 * 用于返回用户登录成功后的数据，包含：
 * 1. token：用于后续请求的身份验证
 * 2. user：用户的基本信息
 * 
 * 注意：实际项目中token应该使用JWT等更安全的方式生成
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    /** 认证令牌 */
    private String token;
    
    /** 用户信息 */
    private UserDTO user;
} 