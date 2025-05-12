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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.dto.PostDTO;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.dto.CreatePostRequest;
import com.animesocial.platform.model.dto.PostListResponse;
import com.animesocial.platform.service.PostService;
import com.animesocial.platform.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private TagService tagService;

    private Post testPost;
    private PostDTO testPostDTO;
    private TagDTO testTagDTO;
    private CreatePostRequest createPostRequest;
    private PostListResponse postListResponse;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testPost = new Post();
        testPost.setId(1);
        testPost.setUserId(1);
        testPost.setTitle("测试帖子");
        testPost.setContent("测试内容");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
        testPost.setViewCount(0);
        testPost.setLikeCount(0);
        testPost.setIsTop(false);

        testPostDTO = new PostDTO();
        testPostDTO.setId(1);
        testPostDTO.setTitle("测试帖子");
        testPostDTO.setContent("测试内容");
        testPostDTO.setCreatedAt(LocalDateTime.now());
        testPostDTO.setUpdatedAt(LocalDateTime.now());
        testPostDTO.setViewCount(0);
        testPostDTO.setLikeCount(0);
        testPostDTO.setIsTop(false);

        testTagDTO = new TagDTO();
        testTagDTO.setId(1);
        testTagDTO.setName("测试标签");

        createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("测试帖子");
        createPostRequest.setContent("测试内容");

        postListResponse = new PostListResponse(Arrays.asList(testPostDTO), 1);
    }

    @Test
    @DisplayName("测试获取帖子详情")
    @WithMockUser
    void testGetPost() throws Exception {
        when(postService.getPostById(anyInt())).thenReturn(testPostDTO);

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("测试帖子"))
                .andExpect(jsonPath("$.content").value("测试内容"));
    }

    @Test
    @DisplayName("测试获取帖子列表")
    @WithMockUser
    void testGetPosts() throws Exception {
        when(postService.getAllPosts(anyInt(), anyInt(), any(), anyString()))
                .thenReturn(postListResponse);

        mockMvc.perform(get("/api/posts")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].title").value("测试帖子"));
    }

    @Test
    @DisplayName("测试创建帖子")
    @WithMockUser
    void testCreatePost() throws Exception {
        when(postService.createPost(anyInt(), any(CreatePostRequest.class))).thenReturn(testPostDTO);

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("测试帖子"));
    }

    @Test
    @DisplayName("测试更新帖子")
    @WithMockUser
    void testUpdatePost() throws Exception {
        when(postService.updatePost(anyInt(), any(CreatePostRequest.class))).thenReturn(testPostDTO);

        mockMvc.perform(put("/api/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("测试帖子"));
    }

    @Test
    @DisplayName("测试删除帖子")
    @WithMockUser
    void testDeletePost() throws Exception {
        doNothing().when(postService).deletePost(anyInt());

        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("测试点赞帖子")
    @WithMockUser
    void testLikePost() throws Exception {
        when(postService.likePost(anyInt(), anyInt())).thenReturn("点赞成功");

        mockMvc.perform(post("/api/posts/1/like"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试取消点赞帖子")
    @WithMockUser
    void testUnlikePost() throws Exception {
        when(postService.unlikePost(anyInt(), anyInt())).thenReturn("取消点赞成功");

        mockMvc.perform(delete("/api/posts/1/like"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试获取帖子标签")
    @WithMockUser
    void testGetPostTags() throws Exception {
        when(tagService.getPostTags(anyInt())).thenReturn(Arrays.asList(testTagDTO));

        mockMvc.perform(get("/api/posts/1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("测试标签"));
    }

    @Test
    @DisplayName("测试获取用户帖子")
    @WithMockUser
    void testGetUserPosts() throws Exception {
        when(postService.getPostsByUserId(anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(testPostDTO));

        mockMvc.perform(get("/api/posts/user/1")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("测试帖子"));
    }

    @Test
    @DisplayName("测试获取用户点赞的帖子")
    @WithMockUser
    void testGetUserLikedPosts() throws Exception {
        when(postService.getLikedPosts(anyInt(), anyInt(), anyInt()))
                .thenReturn(postListResponse);

        mockMvc.perform(get("/api/posts/liked")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].title").value("测试帖子"));
    }
} 