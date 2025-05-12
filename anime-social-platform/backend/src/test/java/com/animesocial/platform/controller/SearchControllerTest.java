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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.animesocial.platform.model.es.PostDocument;
import com.animesocial.platform.model.es.ResourceDocument;
import com.animesocial.platform.model.es.UserDocument;
import com.animesocial.platform.service.ElasticsearchService;
import com.animesocial.platform.service.PostService;
import com.animesocial.platform.service.ResourceService;
import com.animesocial.platform.service.UserService;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElasticsearchService elasticsearchService;

    @MockBean
    private PostService postService;

    @MockBean
    private ResourceService resourceService;

    @MockBean
    private UserService userService;

    private PostDocument testPostDocument;
    private ResourceDocument testResourceDocument;
    private UserDocument testUserDocument;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testPostDocument = PostDocument.builder()
                .id(1)
                .title("测试帖子")
                .content("测试内容")
                .userId(1)
                .username("testuser")
                .createdAt(LocalDateTime.now())
                .build();

        testResourceDocument = ResourceDocument.builder()
                .id(1)
                .title("测试资源")
                .description("测试描述")
                .userId(1)
                .username("testuser")
                .uploadTime(LocalDateTime.now())
                .build();

        testUserDocument = UserDocument.builder()
                .id(1)
                .username("testuser")
                .bio("测试简介")
                .registerTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("测试搜索所有内容")
    @WithMockUser
    void testSearch() throws Exception {
        // 模拟搜索结果
        Page<PostDocument> postPage = new PageImpl<>(Arrays.asList(testPostDocument));
        Page<ResourceDocument> resourcePage = new PageImpl<>(Arrays.asList(testResourceDocument));
        Page<UserDocument> userPage = new PageImpl<>(Arrays.asList(testUserDocument));

        when(elasticsearchService.searchPosts(anyString(), anyInt(), anyInt())).thenReturn(postPage);
        when(elasticsearchService.searchResources(anyString(), anyInt(), anyInt())).thenReturn(resourcePage);
        when(elasticsearchService.searchUsers(anyString(), anyInt(), anyInt())).thenReturn(userPage);

        mockMvc.perform(get("/api/search")
                .param("keyword", "测试")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.posts[0].id").value(1))
                .andExpect(jsonPath("$.data.resources[0].id").value(1))
                .andExpect(jsonPath("$.data.users[0].id").value(1));
    }

    @Test
    @DisplayName("测试搜索帖子")
    @WithMockUser
    void testSearchPosts() throws Exception {
        Page<PostDocument> postPage = new PageImpl<>(Arrays.asList(testPostDocument));
        when(elasticsearchService.searchPosts(anyString(), anyInt(), anyInt())).thenReturn(postPage);

        mockMvc.perform(get("/api/search")
                .param("keyword", "测试")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.posts[0].id").value(1))
                .andExpect(jsonPath("$.data.posts[0].title").value("测试帖子"));
    }

    @Test
    @DisplayName("测试搜索资源")
    @WithMockUser
    void testSearchResources() throws Exception {
        Page<ResourceDocument> resourcePage = new PageImpl<>(Arrays.asList(testResourceDocument));
        when(elasticsearchService.searchResources(anyString(), anyInt(), anyInt())).thenReturn(resourcePage);

        mockMvc.perform(get("/api/search")
                .param("keyword", "测试")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.resources[0].id").value(1))
                .andExpect(jsonPath("$.data.resources[0].title").value("测试资源"));
    }

    @Test
    @DisplayName("测试搜索用户")
    @WithMockUser
    void testSearchUsers() throws Exception {
        Page<UserDocument> userPage = new PageImpl<>(Arrays.asList(testUserDocument));
        when(elasticsearchService.searchUsers(anyString(), anyInt(), anyInt())).thenReturn(userPage);

        mockMvc.perform(get("/api/search")
                .param("keyword", "测试")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.users[0].id").value(1))
                .andExpect(jsonPath("$.data.users[0].username").value("testuser"));
    }
} 