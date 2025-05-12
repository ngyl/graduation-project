package com.animesocial.platform.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.service.TagService;
import com.animesocial.platform.service.UserTagService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TagService tagService;

    @MockBean
    private UserTagService userTagService;

    private Tag testTag;
    private TagDTO testTagDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
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
    }

    @Test
    @DisplayName("测试获取标签详情")
    @WithMockUser
    void testGetTag() throws Exception {
        when(tagService.getTagById(anyInt())).thenReturn(testTagDTO);

        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("测试标签"));
    }

    @Test
    @DisplayName("测试获取所有标签")
    @WithMockUser
    void testGetTags() throws Exception {
        when(tagService.getAllTags()).thenReturn(Arrays.asList(testTagDTO));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("测试标签"));
    }

    @Test
    @DisplayName("测试根据类型获取标签")
    @WithMockUser
    void testGetTagsByType() throws Exception {
        when(tagService.getTagsByType(anyString())).thenReturn(Arrays.asList(testTagDTO));

        mockMvc.perform(get("/api/tags/type/post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("测试标签"));
    }

    @Test
    @DisplayName("测试创建标签")
    @WithMockUser
    void testCreateTag() throws Exception {
        when(tagService.createTag(any(TagDTO.class))).thenReturn(testTagDTO);

        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTagDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("测试标签"));
    }

    @Test
    @DisplayName("测试更新标签")
    @WithMockUser
    void testUpdateTag() throws Exception {
        when(tagService.updateTag(anyInt(), any(Tag.class))).thenReturn(testTagDTO);

        mockMvc.perform(put("/api/tags/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("测试标签"));
    }

    @Test
    @DisplayName("测试删除标签")
    @WithMockUser
    void testDeleteTag() throws Exception {
        doNothing().when(tagService).deleteTag(anyInt());

        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("测试获取帖子标签")
    @WithMockUser
    void testGetPostTags() throws Exception {
        when(tagService.getPostTags(anyInt())).thenReturn(Arrays.asList(testTagDTO));

        mockMvc.perform(get("/api/tags/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("测试标签"));
    }

    @Test
    @DisplayName("测试获取资源标签")
    @WithMockUser
    void testGetResourceTags() throws Exception {
        when(tagService.getResourceTags(anyInt())).thenReturn(Arrays.asList(testTagDTO));

        mockMvc.perform(get("/api/tags/resource/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("测试标签"));
    }

    @Test
    @DisplayName("测试更新帖子标签")
    @WithMockUser
    void testUpdatePostTags() throws Exception {
        doNothing().when(tagService).updatePostTags(anyInt(), anyList());

        mockMvc.perform(put("/api/tags/post/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[1, 2, 3]"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试更新资源标签")
    @WithMockUser
    void testUpdateResourceTags() throws Exception {
        doNothing().when(tagService).updateResourceTags(anyInt(), anyList());

        mockMvc.perform(put("/api/tags/resource/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[1, 2, 3]"))
                .andExpect(status().isOk());
    }
} 