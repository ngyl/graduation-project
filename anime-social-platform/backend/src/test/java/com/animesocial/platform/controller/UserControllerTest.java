package com.animesocial.platform.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.LoginRequest;
import com.animesocial.platform.model.dto.RegisterRequest;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.dto.UpdateUserInfoRequest;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.dto.UserDetailResponse;
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.service.UserService;
import com.animesocial.platform.service.TagService;
import com.animesocial.platform.service.UserTagService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private TagService tagService;

    @MockBean
    private UserTagService userTagService;

    private User testUser;
    private UserDTO testUserDTO;
    private TagDTO testTagDTO;
    private Tag testTag;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UpdateUserInfoRequest updateUserInfoRequest;
    private UserDetailResponse userDetailResponse;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setStatus(1);
        testUser.setRegisterTime(LocalDateTime.now());
        testUser.setLastLoginTime(LocalDateTime.now());

        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setUsername("testuser");
        testUserDTO.setAvatar("http://example.com/avatar.jpg");
        testUserDTO.setBio("测试简介");
        testUserDTO.setIsAdmin(false);
        testUserDTO.setStatus(1);
        testUserDTO.setRegisterTime(LocalDateTime.now());
        testUserDTO.setLastLoginTime(LocalDateTime.now());

        testTag = new Tag();
        testTag.setId(1);
        testTag.setName("测试标签");
        testTag.setCategory("测试分类");
        testTag.setType("post");

        testTagDTO = new TagDTO();
        testTagDTO.setId(1);
        testTagDTO.setName("测试标签");
        testTagDTO.setCategory("测试分类");
        testTagDTO.setType("post");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        updateUserInfoRequest = new UpdateUserInfoRequest();
        updateUserInfoRequest.setBio("更新后的简介");
        updateUserInfoRequest.setAvatar("http://example.com/new-avatar.jpg");

        userDetailResponse = new UserDetailResponse();
        userDetailResponse.setId(1);
        userDetailResponse.setUsername("testuser");
        userDetailResponse.setAvatar("http://example.com/avatar.jpg");
        userDetailResponse.setBio("测试简介");
        userDetailResponse.setIsAdmin(false);
        userDetailResponse.setStatus(1);
        userDetailResponse.setRegisterTime(LocalDateTime.now());
        userDetailResponse.setLastLoginTime(LocalDateTime.now());
        userDetailResponse.setPostCount(10);
        userDetailResponse.setFavoriteCount(5);
        userDetailResponse.setFollowerCount(20);
        userDetailResponse.setFollowingCount(15);
        userDetailResponse.setTags(Arrays.asList(testTagDTO));
    }

    @Test
    @DisplayName("测试用户注册")
    void testRegister() throws Exception {
        doNothing().when(userService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("测试用户登录")
    void testLogin() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("测试获取用户信息")
    @WithMockUser
    void testGetUser() throws Exception {
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("测试获取用户详情")
    @WithMockUser
    void testGetUserDetail() throws Exception {
        when(userService.getUserDetail(anyInt())).thenReturn(userDetailResponse);

        mockMvc.perform(get("/api/users/1/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.postCount").value(10))
                .andExpect(jsonPath("$.favoriteCount").value(5));
    }

    @Test
    @DisplayName("测试更新用户信息")
    @WithMockUser
    void testUpdateUser() throws Exception {
        when(userService.updateUserInfo(anyInt(), any(UpdateUserInfoRequest.class)))
                .thenReturn(testUserDTO);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserInfoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("测试上传头像")
    @WithMockUser
    void testUploadAvatar() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes());

        when(userService.updateUserInfo(anyInt(), any(UpdateUserInfoRequest.class)))
                .thenReturn(testUserDTO);

        mockMvc.perform(multipart("/api/users/1/avatar")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("测试获取用户标签")
    @WithMockUser
    void testGetUserTags() throws Exception {
        when(userTagService.getUserTags(anyInt())).thenReturn(Arrays.asList(testTag));

        mockMvc.perform(get("/api/users/1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("测试标签"));
    }

    @Test
    @DisplayName("测试获取用户关注列表")
    @WithMockUser
    void testGetFollowing() throws Exception {
        when(userService.getUserFollowing(anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(testUserDTO));

        mockMvc.perform(get("/api/users/1/following")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @DisplayName("测试获取用户粉丝列表")
    @WithMockUser
    void testGetFollowers() throws Exception {
        when(userService.getUserFollowers(anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(testUserDTO));

        mockMvc.perform(get("/api/users/1/followers")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @DisplayName("测试关注用户")
    @WithMockUser
    void testFollow() throws Exception {
        when(userService.toggleFollow(anyInt(), anyInt())).thenReturn("关注成功");

        mockMvc.perform(post("/api/users/1/follow"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试取消关注用户")
    @WithMockUser
    void testUnfollow() throws Exception {
        when(userService.toggleFollow(anyInt(), anyInt())).thenReturn("已取消关注");

        mockMvc.perform(delete("/api/users/1/follow"))
                .andExpect(status().isOk());
    }
} 