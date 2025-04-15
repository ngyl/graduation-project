package com.animesocial.platform.model.dto;

import lombok.Data;

/**
 * 用户数据传输对象（DTO）
 * 
 * 用于在前后端之间传输用户信息，包含：
 * 1. 基本信息：ID、用户名
 * 2. 个人资料：头像、简介
 * 3. 权限信息：管理员标识
 * 
 * 注意：不包含敏感信息（如密码）
 */
@Data
public class UserDTO {
    /** 用户ID */
    private Integer id;
    
    /** 用户名 */
    private String username;
    
    /** 头像URL */
    private String avatar;
    
    /** 个人简介 */
    private String bio;
    
    /** 是否为管理员 */
    private Boolean isAdmin;
} 