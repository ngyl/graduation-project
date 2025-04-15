package com.animesocial.platform.model;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户实体类
 * 
 * 表示系统中的用户信息，包含：
 * 1. 基本信息：ID、用户名、密码等
 * 2. 个人资料：头像、简介等
 * 3. 状态信息：管理员标识、账号状态等
 * 4. 时间信息：注册时间、最后登录时间等
 */
@Data
public class User {
    /** 用户ID */
    private Integer id;
    
    /** 用户名 */
    private String username;
    
    /** 密码（加密存储） */
    private String password;
    
    /** 头像URL */
    private String avatar;
    
    /** 个人简介 */
    private String bio;
    
    /** 是否为管理员 */
    private Boolean isAdmin;
    
    /** 账号状态：0-正常，1-禁用 */
    private Integer status;
    
    /** 注册时间 */
    private LocalDateTime registerTime;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    /**
     * 隐藏敏感信息
     * 在返回给前端时调用，确保密码等敏感信息不会被泄露
     */
    public void hideSensitiveInfo() {
        this.password = null;
    }
} 