package com.animesocial.platform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.LoginRequest;
import com.animesocial.platform.model.dto.RegisterRequest;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    // 使用接口类型声明变量
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        userService = userServiceImpl;

        // 初始化测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setStatus(1);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("测试用户注册 - 成功场景")
    void testRegisterSuccess() {
        // 配置mock行为
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doNothing().when(userRepository).insert(any(User.class));

        // 执行测试
        assertDoesNotThrow(() -> userService.register(registerRequest));

        // 验证mock调用
        verify(userRepository).findByUsername(registerRequest.getUsername());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).insert(any(User.class));
    }

    @Test
    @DisplayName("测试用户注册 - 用户名已存在")
    void testRegisterUsernameExists() {
        // 配置mock行为
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> userService.register(registerRequest));

        assertTrue(exception.getMessage().contains("用户名已存在"));
    }

    @Test
    @DisplayName("测试用户登录 - 成功场景")
    void testLoginSuccess() {
        // 配置mock行为
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        doNothing().when(userRepository).updateLastLoginTime(any(User.class));

        // 执行测试
        User result = userService.login(loginRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    @DisplayName("测试用户登录 - 密码错误")
    void testLoginWrongPassword() {
        // 配置mock行为
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> userService.login(loginRequest));

        assertTrue(exception.getMessage().contains("密码错误"));
    }

    @Test
    @DisplayName("测试获取用户信息")
    void testGetUserDTOById() {
        // 配置mock行为
        when(userRepository.findById(anyInt())).thenReturn(testUser);

        // 执行测试
        UserDTO result = userService.getUserDTOById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    @DisplayName("测试检查用户名是否存在")
    void testIsUsernameExist() {
        // 配置mock行为
        when(userRepository.findByUsername("existingUser")).thenReturn(testUser);
        when(userRepository.findByUsername("newUser")).thenReturn(null);

        // 执行测试
        boolean existingResult = userService.isUsernameExist("existingUser");
        boolean newResult = userService.isUsernameExist("newUser");

        // 验证结果
        assertTrue(existingResult);
        assertFalse(newResult);
    }
} 