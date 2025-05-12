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
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.repository.TagRepository;
import com.animesocial.platform.service.impl.TagServiceImpl;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagServiceImpl;

    private TagService tagService;

    private Tag testTag;
    private TagDTO testTagDTO;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        tagService = tagServiceImpl;

        // 初始化测试数据
        testTag = new Tag();
        testTag.setId(1);
        testTag.setName("测试标签");
        testTag.setType("post");
        testTag.setCategory("动漫");

        testTagDTO = new TagDTO();
        testTagDTO.setId(1);
        testTagDTO.setName("测试标签");
        testTagDTO.setType("post");
        testTagDTO.setCategory("动漫");
    }

    @Test
    @DisplayName("测试获取所有标签")
    void testGetAllTags() {
        // 配置mock行为
        when(tagRepository.findAll()).thenReturn(Arrays.asList(testTag));

        // 执行测试
        List<TagDTO> result = tagService.getAllTags();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTag.getName(), result.get(0).getName());
    }

    @Test
    @DisplayName("测试根据类型获取标签")
    void testGetTagsByType() {
        // 配置mock行为
        when(tagRepository.findByType(anyString())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        List<TagDTO> result = tagService.getTagsByType("post");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("post", result.get(0).getType());
    }

    @Test
    @DisplayName("测试根据ID获取标签")
    void testGetTagById() {
        // 配置mock行为
        when(tagRepository.findById(anyInt())).thenReturn(testTag);

        // 执行测试
        TagDTO result = tagService.getTagById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(testTag.getId(), result.getId());
        assertEquals(testTag.getName(), result.getName());
    }

    @Test
    @DisplayName("测试创建标签")
    void testCreateTag() {
        // 配置mock行为
        when(tagRepository.findByName(anyString())).thenReturn(null);
        doNothing().when(tagRepository).save(any(Tag.class));

        // 执行测试
        TagDTO result = tagService.createTag(testTag);

        // 验证结果
        assertNotNull(result);
        assertEquals(testTag.getName(), result.getName());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    @DisplayName("测试创建标签 - 标签名已存在")
    void testCreateTagNameExists() {
        // 配置mock行为
        when(tagRepository.findByName(anyString())).thenReturn(testTag);

        // 执行测试并验证异常
        Exception exception = assertThrows(RuntimeException.class, 
            () -> tagService.createTag(testTag));

        assertTrue(exception.getMessage().contains("标签名已存在"));
    }

    @Test
    @DisplayName("测试更新标签")
    void testUpdateTag() {
        // 配置mock行为
        when(tagRepository.findById(anyInt())).thenReturn(testTag);
        doNothing().when(tagRepository).update(any(Tag.class));

        // 执行测试
        TagDTO result = tagService.updateTag(1, testTag);

        // 验证结果
        assertNotNull(result);
        assertEquals(testTag.getName(), result.getName());
        verify(tagRepository).update(any(Tag.class));
    }

    @Test
    @DisplayName("测试删除标签")
    void testDeleteTag() {
        // 配置mock行为
        when(tagRepository.findById(anyInt())).thenReturn(testTag);
        when(tagRepository.countContentByTagId(anyInt())).thenReturn(0);
        doNothing().when(tagRepository).deleteById(anyInt());

        // 执行测试
        assertDoesNotThrow(() -> tagService.deleteTag(1));

        // 验证mock调用
        verify(tagRepository).deleteById(1);
    }

    @Test
    @DisplayName("测试获取帖子标签")
    void testGetPostTags() {
        // 配置mock行为
        when(tagRepository.findByPostId(anyInt())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        List<TagDTO> result = tagService.getPostTags(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("post", result.get(0).getType());
    }

    @Test
    @DisplayName("测试获取资源标签")
    void testGetResourceTags() {
        // 配置mock行为
        when(tagRepository.findByResourceId(anyInt())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        List<TagDTO> result = tagService.getResourceTags(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("post", result.get(0).getType());
    }

    @Test
    @DisplayName("测试更新帖子标签")
    void testUpdatePostTags() {
        // 配置mock行为
        doNothing().when(tagRepository).deletePostTags(anyInt());
        doNothing().when(tagRepository).savePostTags(anyInt(), anyList());

        // 执行测试
        assertDoesNotThrow(() -> tagService.updatePostTags(1, Arrays.asList(1, 2, 3)));

        // 验证mock调用
        verify(tagRepository).deletePostTags(1);
        verify(tagRepository).savePostTags(eq(1), anyList());
    }

    @Test
    @DisplayName("测试更新资源标签")
    void testUpdateResourceTags() {
        // 配置mock行为
        doNothing().when(tagRepository).deleteResourceTags(anyInt());
        doNothing().when(tagRepository).saveResourceTags(anyInt(), anyList());

        // 执行测试
        assertDoesNotThrow(() -> tagService.updateResourceTags(1, Arrays.asList(1, 2, 3)));

        // 验证mock调用
        verify(tagRepository).deleteResourceTags(1);
        verify(tagRepository).saveResourceTags(eq(1), anyList());
    }
} 