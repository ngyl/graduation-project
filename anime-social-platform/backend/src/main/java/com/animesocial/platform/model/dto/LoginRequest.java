package com.animesocial.platform.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求DTO
 * 
 * 用于接收用户登录请求的数据，包含：
 * 1. 用户名
 * 2. 密码
 * 
 * 使用验证注解确保数据的合法性：
 * 1. 用户名和密码不能为空
 * 2. 用户名长度限制为3-20个字符
 * 3. 密码长度限制为6-20个字符
 */
@Data
public class LoginRequest {
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度应为3-20个字符")
    private String username;
    
    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应为6-20个字符")
    private String password;
} 