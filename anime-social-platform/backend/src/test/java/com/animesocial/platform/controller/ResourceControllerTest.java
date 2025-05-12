package com.animesocial.platform.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.dto.ResourceDTO;
import com.animesocial.platform.model.dto.ResourceListResponse;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.service.ResourceService;
import com.animesocial.platform.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ResourceController.class)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceService resourceService;

    @MockBean
    private TagService tagService;

    private Resource testResource;
    private ResourceDTO testResourceDTO;
    private TagDTO testTagDTO;
    private ResourceListResponse resourceListResponse;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testResource = new Resource();
        testResource.setId(1);
        testResource.setUserId(1);
        testResource.setTitle("测试资源");
        testResource.setDescription("测试描述");
        testResource.setFileUrl("http://example.com/test.mp4");
        testResource.setUploadTime(LocalDateTime.now());
        testResource.setDownloadCount(0);
        testResource.setLikeCount(0);

        testResourceDTO = new ResourceDTO();
        testResourceDTO.setId(1);
        testResourceDTO.setTitle("测试资源");
        testResourceDTO.setDescription("测试描述");
        testResourceDTO.setFileUrl("http://example.com/test.mp4");
        testResourceDTO.setUploadTime(LocalDateTime.now());
        testResourceDTO.setDownloadCount(0);
        testResourceDTO.setLikeCount(0);

        testTagDTO = new TagDTO();
        testTagDTO.setId(1);
        testTagDTO.setName("测试标签");

        resourceListResponse = new ResourceListResponse(Arrays.asList(testResourceDTO), 1);
    }

    @Test
    @DisplayName("测试获取资源详情")
    @WithMockUser
    void testGetResource() throws Exception {
        when(resourceService.getResourceById(anyInt())).thenReturn(testResourceDTO);

        mockMvc.perform(get("/api/resources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("测试资源"))
                .andExpect(jsonPath("$.description").value("测试描述"));
    }

    @Test
    @DisplayName("测试获取资源列表")
    @WithMockUser
    void testGetResources() throws Exception {
        when(resourceService.getAllResources(anyInt(), anyInt(), any(), anyString()))
                .thenReturn(resourceListResponse);

        mockMvc.perform(get("/api/resources")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].title").value("测试资源"));
    }

    @Test
    @DisplayName("测试上传资源")
    @WithMockUser
    void testUploadResource() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes());

        when(resourceService.uploadResource(anyInt(), anyString(), anyString(), anyList(), any()))
                .thenReturn(testResourceDTO);

        mockMvc.perform(multipart("/api/resources")
                .file(file)
                .param("title", "测试资源")
                .param("description", "测试描述")
                .param("tagIds", "1", "2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("测试资源"));
    }

    @Test
    @DisplayName("测试更新资源")
    @WithMockUser
    void testUpdateResource() throws Exception {
        when(resourceService.updateResource(anyInt(), anyString(), anyString(), anyList()))
                .thenReturn(testResourceDTO);

        mockMvc.perform(put("/api/resources/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"更新后的资源\",\"description\":\"更新后的描述\",\"tagIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("测试资源"));
    }

    @Test
    @DisplayName("测试删除资源")
    @WithMockUser
    void testDeleteResource() throws Exception {
        doNothing().when(resourceService).deleteResource(anyInt());

        mockMvc.perform(delete("/api/resources/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("测试获取资源下载链接")
    @WithMockUser
    void testDownloadResource() throws Exception {
        when(resourceService.getDownloadUrl(anyInt(), anyInt())).thenReturn("http://example.com/download/test.mp4");

        mockMvc.perform(get("/api/resources/1/download"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("http://example.com/download/test.mp4"));
    }

    @Test
    @DisplayName("测试点赞资源")
    @WithMockUser
    void testLikeResource() throws Exception {
        when(resourceService.likeResource(anyInt(), anyInt())).thenReturn("点赞成功");

        mockMvc.perform(post("/api/resources/1/like"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试取消点赞资源")
    @WithMockUser
    void testUnlikeResource() throws Exception {
        when(resourceService.unlikeResource(anyInt(), anyInt())).thenReturn("取消点赞成功");

        mockMvc.perform(delete("/api/resources/1/like"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试获取资源标签")
    @WithMockUser
    void testGetResourceTags() throws Exception {
        when(tagService.getResourceTags(anyInt())).thenReturn(Arrays.asList(testTagDTO));

        mockMvc.perform(get("/api/resources/1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("测试标签"));
    }

    @Test
    @DisplayName("测试获取用户资源")
    @WithMockUser
    void testGetUserResources() throws Exception {
        when(resourceService.getResourcesByUserId(anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(testResourceDTO));

        mockMvc.perform(get("/api/resources/user/1")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("测试资源"));
    }

    @Test
    @DisplayName("测试获取用户点赞的资源")
    @WithMockUser
    void testGetUserLikedResources() throws Exception {
        when(resourceService.getLikedResources(anyInt(), anyInt(), anyInt()))
                .thenReturn(resourceListResponse);

        mockMvc.perform(get("/api/resources/liked")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].title").value("测试资源"));
    }
} 