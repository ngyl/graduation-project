package com.animesocial.platform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Friendship;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.repository.FriendshipRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.impl.FriendshipServiceImpl;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FriendshipServiceImpl friendshipServiceImpl;

    private FriendshipService friendshipService;

    private User testUser;
    private User testFriend;
    private UserDTO testUserDTO;
    private UserDTO testFriendDTO;
    private Friendship testFriendship;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        friendshipService = friendshipServiceImpl;

        // 初始化测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        testFriend = new User();
        testFriend.setId(2);
        testFriend.setUsername("testfriend");

        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setUsername("testuser");

        testFriendDTO = new UserDTO();
        testFriendDTO.setId(2);
        testFriendDTO.setUsername("testfriend");

        testFriendship = new Friendship();
        testFriendship.setId(1);
        testFriendship.setUserId(1);
        testFriendship.setFriendId(2);
        testFriendship.setStatus(0);
        testFriendship.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试关注用户")
    void testFollow() {
        // 配置mock行为
        when(userService.getUserDTOById(anyInt())).thenReturn(testFriendDTO);
        when(friendshipRepository.exists(anyInt(), anyInt())).thenReturn(false);
        when(friendshipRepository.exists(anyInt(), anyInt())).thenReturn(false);

        // 执行测试
        String result = friendshipService.follow(1, 2);

        // 验证结果
        assertEquals("关注成功", result);
        verify(friendshipRepository).insert(anyInt(), anyInt());
    }

    @Test
    @DisplayName("测试关注自己")
    void testFollowSelf() {
        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            friendshipService.follow(1, 1);
        });
    }

    @Test
    @DisplayName("测试关注不存在的用户")
    void testFollowNonExistentUser() {
        // 配置mock行为
        when(userService.getUserDTOById(anyInt())).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            friendshipService.follow(1, 2);
        });
    }

    @Test
    @DisplayName("测试关注已关注的用户")
    void testFollowExistingFriend() {
        // 配置mock行为
        when(userService.getUserDTOById(anyInt())).thenReturn(testFriendDTO);
        when(friendshipRepository.exists(anyInt(), anyInt())).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            friendshipService.follow(1, 2);
        });
    }

    @Test
    @DisplayName("测试取消关注")
    void testUnfollow() {
        // 配置mock行为
        when(userService.getUserDTOById(anyInt())).thenReturn(testFriendDTO);
        when(friendshipRepository.exists(anyInt(), anyInt())).thenReturn(true);
        when(friendshipRepository.isMutualFollow(anyInt(), anyInt())).thenReturn(false);

        // 执行测试
        String result = friendshipService.unfollow(1, 2);

        // 验证结果
        assertEquals("取消关注成功", result);
        verify(friendshipRepository).delete(anyInt(), anyInt());
    }

    @Test
    @DisplayName("测试取消关注不存在的用户")
    void testUnfollowNonExistentUser() {
        // 配置mock行为
        when(userService.getUserDTOById(anyInt())).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            friendshipService.unfollow(1, 2);
        });
    }

    @Test
    @DisplayName("测试取消关注未关注的用户")
    void testUnfollowNonExistingFriend() {
        // 配置mock行为
        when(userService.getUserDTOById(anyInt())).thenReturn(testFriendDTO);
        when(friendshipRepository.exists(anyInt(), anyInt())).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            friendshipService.unfollow(1, 2);
        });
    }

    @Test
    @DisplayName("测试获取关注列表")
    void testGetFollowing() {
        // 配置mock行为
        when(userService.getUserFollowing(anyInt(), anyInt(), anyInt())).thenReturn(Arrays.asList(testFriendDTO));

        // 执行测试
        List<UserDTO> result = friendshipService.getFollowing(1, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFriendDTO.getUsername(), result.get(0).getUsername());
    }

    @Test
    @DisplayName("测试获取粉丝列表")
    void testGetFollowers() {
        // 配置mock行为
        when(userService.getUserFollowers(anyInt(), anyInt(), anyInt())).thenReturn(Arrays.asList(testUserDTO));

        // 执行测试
        List<UserDTO> result = friendshipService.getFollowers(2, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDTO.getUsername(), result.get(0).getUsername());
    }

    @Test
    @DisplayName("测试检查是否已关注")
    void testIsFollowing() {
        // 配置mock行为
        when(friendshipRepository.exists(anyInt(), anyInt())).thenReturn(true);

        // 执行测试
        boolean result = friendshipService.isFollowing(1, 2);

        // 验证结果
        assertTrue(result);
    }

    @Test
    @DisplayName("测试检查是否互相关注")
    void testIsMutualFollow() {
        // 配置mock行为
        when(friendshipRepository.isMutualFollow(anyInt(), anyInt())).thenReturn(true);

        // 执行测试
        boolean result = friendshipService.isMutualFollow(1, 2);

        // 验证结果
        assertTrue(result);
    }

    @Test
    @DisplayName("测试获取关注数量")
    void testCountFollowing() {
        // 配置mock行为
        when(friendshipRepository.countFollowing(anyInt())).thenReturn(5);

        // 执行测试
        int result = friendshipService.countFollowing(1);

        // 验证结果
        assertEquals(5, result);
    }

    @Test
    @DisplayName("测试获取粉丝数量")
    void testCountFollowers() {
        // 配置mock行为
        when(friendshipRepository.countFollowers(anyInt())).thenReturn(3);

        // 执行测试
        int result = friendshipService.countFollowers(1);

        // 验证结果
        assertEquals(3, result);
    }
} 