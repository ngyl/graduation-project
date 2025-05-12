package com.animesocial.platform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.UserTag;
import com.animesocial.platform.repository.TagRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.repository.UserTagRepository;
import com.animesocial.platform.service.impl.UserTagServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserTagServiceTest {

    @Mock
    private UserTagRepository userTagRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserTagServiceImpl userTagServiceImpl;

    private UserTagService userTagService;

    private Tag testTag;
    private UserTag testUserTag;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        userTagService = userTagServiceImpl;

        // 初始化测试数据
        testTag = new Tag();
        testTag.setId(1);
        testTag.setName("测试标签");
        testTag.setType("post");
        testTag.setCategory("动漫");

        testUserTag = new UserTag();
        testUserTag.setId(1);
        testUserTag.setUserId(1);
        testUserTag.setTagId(1);
        testUserTag.setTagName("测试标签");
        testUserTag.setTagCategory("动漫");
        testUserTag.setTagType("post");
    }

    @Test
    @DisplayName("测试添加用户标签")
    void testAddUserTag() {
        // 配置mock行为
        when(userTagRepository.exists(anyInt(), anyInt())).thenReturn(0);
        doNothing().when(userTagRepository).save(anyInt(), anyInt());

        // 执行测试
        boolean result = userTagService.addUserTag(1, 1);

        // 验证结果
        assertTrue(result);
        verify(userTagRepository).save(eq(1), eq(1));
    }

    @Test
    @DisplayName("测试添加已存在的用户标签")
    void testAddExistingUserTag() {
        // 配置mock行为
        when(userTagRepository.exists(anyInt(), anyInt())).thenReturn(1);

        // 执行测试
        boolean result = userTagService.addUserTag(1, 1);

        // 验证结果
        assertFalse(result);
        verify(userTagRepository, never()).save(anyInt(), anyInt());
    }

    @Test
    @DisplayName("测试批量更新用户标签")
    void testUpdateUserTags() {
        // 配置mock行为
        doNothing().when(userTagRepository).deleteByUserId(anyInt());
        doNothing().when(userTagRepository).batchSave(anyInt(), anyList());
        when(tagRepository.findById(anyInt())).thenReturn(testTag);

        // 执行测试
        List<Tag> result = userTagService.updateUserTags(1, Arrays.asList(1, 2, 3));

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(userTagRepository).deleteByUserId(1);
        verify(userTagRepository).batchSave(eq(1), anyList());
    }

    @Test
    @DisplayName("测试按类型批量更新用户标签")
    void testUpdateUserTagsByType() {
        // 配置mock行为
        doNothing().when(userTagRepository).deleteByUserIdAndTagType(anyInt(), anyString());
        doNothing().when(userTagRepository).batchSave(anyInt(), anyList());
        when(tagRepository.findById(anyInt())).thenReturn(testTag);

        // 执行测试
        List<Tag> result = userTagService.updateUserTagsByType(1, Arrays.asList(1, 2, 3), "post");

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(userTagRepository).deleteByUserIdAndTagType(1, "post");
        verify(userTagRepository).batchSave(eq(1), anyList());
    }

    @Test
    @DisplayName("测试移除用户标签")
    void testRemoveUserTag() {
        // 配置mock行为
        when(userTagRepository.delete(anyInt(), anyInt())).thenReturn(1);

        // 执行测试
        boolean result = userTagService.removeUserTag(1, 1);

        // 验证结果
        assertTrue(result);
        verify(userTagRepository).delete(1, 1);
    }

    @Test
    @DisplayName("测试获取用户标签列表")
    void testGetUserTags() {
        // 配置mock行为
        when(userTagRepository.findTagsByUserId(anyInt())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        List<Tag> result = userTagService.getUserTags(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTag.getName(), result.get(0).getName());
    }

    @Test
    @DisplayName("测试获取用户标签详情列表")
    void testGetUserTagDetails() {
        // 配置mock行为
        when(userTagRepository.findDetailsByUserId(anyInt())).thenReturn(Arrays.asList(testUserTag));

        // 执行测试
        List<UserTag> result = userTagService.getUserTagDetails(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserTag.getTagName(), result.get(0).getTagName());
    }

    @Test
    @DisplayName("测试检查用户是否拥有标签")
    void testHasTag() {
        // 配置mock行为
        when(userTagRepository.exists(anyInt(), anyInt())).thenReturn(1);

        // 执行测试
        boolean result = userTagService.hasTag(1, 1);

        // 验证结果
        assertTrue(result);
        verify(userTagRepository).exists(1, 1);
    }
} 