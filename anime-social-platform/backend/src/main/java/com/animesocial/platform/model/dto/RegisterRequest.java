package com.animesocial.platform.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求DTO
 * 
 * 用于接收用户注册请求的数据，包含：
 * 1. 用户名
 * 2. 密码
 * 3. 确认密码
 * 
 * 使用验证注解确保数据的合法性：
 * 1. 用户名和密码不能为空
 * 2. 用户名长度限制为3-20个字符
 * 3. 密码长度限制为6-20个字符
 * 4. 确认密码不能为空（实际业务中需要与密码一致）
 */
@Data
public class RegisterRequest {
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度应为3-20个字符")
    private String username;
    
    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应为6-20个字符")
    private String password;
    
    /** 确认密码 */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
} 