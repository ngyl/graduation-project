package com.animesocial.platform.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.animesocial.platform.model.User;

@Mapper
public interface UserRepository {
    
    /**
     * 通过ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Integer id);
    
    /**
     * 通过用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    /**
     * 插入新用户
     * @param user 用户信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO users(username, password, is_admin, status, register_time) " +
           "VALUES(#{username}, #{password}, #{isAdmin}, #{status}, #{registerTime})")
    int insert(User user);
    
    /**
     * 更新用户最后登录时间
     * @param user 用户信息
     * @return 影响的行数
     */
    @Update("UPDATE users SET last_login_time = #{lastLoginTime} WHERE id = #{id}")
    int updateLastLoginTime(User user);
} 