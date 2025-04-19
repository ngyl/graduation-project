package com.animesocial.platform.repository;

import com.animesocial.platform.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户数据访问接口
 * 使用MyBatis注解方式定义SQL操作，处理用户相关的数据库操作
 */
@Mapper
public interface UserRepository {
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Integer id);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    /**
     * 保存新用户
     * @param user 用户对象，包含用户名、密码等信息
     * @return 插入成功返回1，失败返回0
     */
    @Insert("INSERT INTO users (username, password, avatar, bio, is_admin, status, register_time) " +
            "VALUES (#{username}, #{password}, #{avatar}, #{bio}, #{isAdmin}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
    
    /**
     * 更新用户信息
     * @param user 用户对象，包含需要更新的字段
     * @return 更新成功返回1，失败返回0
     */
    @Update("UPDATE users SET avatar = #{avatar}, bio = #{bio} WHERE id = #{id}")
    void update(User user);
    
    /**
     * 更新用户最后登录时间
     * @param user 用户对象，包含用户ID和最后登录时间
     * @return 更新成功返回1，失败返回0
     */
    @Update("UPDATE users SET last_login_time = #{lastLoginTime} WHERE id = #{id}")
    void updateLastLoginTime(User user);
    
    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 用户状态(0禁用,1正常)
     */
    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    
    /**
     * 查询用户关注的用户列表(该用户的关注)
     * @param userId 用户ID
     * @return 用户列表，按关注时间降序排序
     */
    @Select("SELECT u.* FROM users u " +
            "JOIN friendships f ON u.id = f.friend_id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<User> findFollowingByUserId(Integer userId);
    
    /**
     * 查询关注该用户的用户列表(该用户的粉丝)
     * @param userId 用户ID
     * @return 用户列表，按关注时间降序排序
     */
    @Select("SELECT u.* FROM users u " +
            "JOIN friendships f ON u.id = f.user_id " +
            "WHERE f.friend_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<User> findFollowersByUserId(Integer userId);
    
    /**
     * 查询所有管理员用户
     * @return 管理员用户列表
     */
    @Select("SELECT * FROM users WHERE is_admin = 1")
    List<User> findAllAdmins();
    
    /**
     * 分页查询所有用户
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 用户列表
     */
    @Select("SELECT * FROM users ORDER BY register_time DESC LIMIT #{offset}, #{limit}")
    List<User> findAll(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 统计用户总数
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM users")
    int count();
    
    /**
     * 根据用户名或个人简介模糊查询用户
     * 
     * @param pattern 搜索模式(已包含%通配符)
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户列表
     */
    @Select("SELECT * FROM users WHERE username LIKE #{pattern} OR bio LIKE #{pattern} LIMIT #{offset}, #{limit}")
    List<User> findByUsernameOrBioLike(@Param("pattern") String pattern, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 随机获取指定数量的用户
     * 
     * @param limit 限制数量
     * @return 用户列表
     */
    @Select("SELECT * FROM users ORDER BY RAND() LIMIT #{limit}")
    List<User> findRandomUsers(@Param("limit") int limit);
    
    /**
     * 获取所有用户ID
     * 
     * @return 用户ID列表
     */
    @Select("SELECT id FROM users")
    List<Integer> findAllIds();
} 