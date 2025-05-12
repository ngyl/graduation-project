package com.animesocial.platform.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.es.PostDocument;
import com.animesocial.platform.model.es.ResourceDocument;
import com.animesocial.platform.model.es.UserDocument;
import com.animesocial.platform.repository.PostRepository;
import com.animesocial.platform.repository.ResourceRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.ElasticsearchService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Post testPost;
    private Resource testResource;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        elasticsearchOperations.indexOps(PostDocument.class).delete();
        elasticsearchOperations.indexOps(ResourceDocument.class).delete();
        elasticsearchOperations.indexOps(UserDocument.class).delete();
        
        // 初始化测试数据
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setStatus(1);
        testUser.setRegisterTime(LocalDateTime.now());
        testUser.setLastLoginTime(LocalDateTime.now());
        userRepository.insert(testUser);

        testPost = new Post();
        testPost.setUserId(testUser.getId());
        testPost.setTitle("测试帖子");
        testPost.setContent("测试内容");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
        postRepository.save(testPost);

        testResource = new Resource();
        testResource.setUserId(testUser.getId());
        testResource.setTitle("测试资源");
        testResource.setDescription("测试描述");
        testResource.setUploadTime(LocalDateTime.now());
        resourceRepository.insert(testResource);

        // 同步数据到 Elasticsearch
        elasticsearchService.syncUserToEs(testUser.getId());
        elasticsearchService.syncPostToEs(testPost.getId());
        elasticsearchService.syncResourceToEs(testResource.getId());
    }

    @Test
    @DisplayName("测试搜索功能集成")
    @WithMockUser
    void testSearchIntegration() throws Exception {
        // 等待索引刷新
        Thread.sleep(1000);

        // 测试搜索所有内容
        mockMvc.perform(get("/api/search")
                .param("keyword", "测试")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.posts[0].id").value(testPost.getId()))
                .andExpect(jsonPath("$.data.resources[0].id").value(testResource.getId()))
                .andExpect(jsonPath("$.data.users[0].id").value(testUser.getId()));
    }

    @Test
    @DisplayName("测试搜索帖子集成")
    @WithMockUser
    void testSearchPostsIntegration() throws Exception {
        // 等待索引刷新
        Thread.sleep(1000);

        mockMvc.perform(get("/api/search")
                .param("keyword", "帖子")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.posts[0].id").value(testPost.getId()))
                .andExpect(jsonPath("$.data.posts[0].title").value("测试帖子"));
    }

    @Test
    @DisplayName("测试搜索资源集成")
    @WithMockUser
    void testSearchResourcesIntegration() throws Exception {
        // 等待索引刷新
        Thread.sleep(1000);

        mockMvc.perform(get("/api/search")
                .param("keyword", "资源")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.resources[0].id").value(testResource.getId()))
                .andExpect(jsonPath("$.data.resources[0].title").value("测试资源"));
    }

    @Test
    @DisplayName("测试搜索用户集成")
    @WithMockUser
    void testSearchUsersIntegration() throws Exception {
        // 等待索引刷新
        Thread.sleep(1000);

        mockMvc.perform(get("/api/search")
                .param("keyword", "testuser")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.users[0].id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.users[0].username").value("testuser"));
    }

    @Test
    @DisplayName("测试数据同步集成")
    @WithMockUser
    void testDataSyncIntegration() throws Exception {
        // 创建新数据
        Post newPost = new Post();
        newPost.setUserId(testUser.getId());
        newPost.setTitle("新测试帖子");
        newPost.setContent("新测试内容");
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setUpdatedAt(LocalDateTime.now());
        postRepository.save(newPost);

        // 同步到 Elasticsearch
        elasticsearchService.syncPostToEs(newPost.getId());

        // 等待索引刷新
        Thread.sleep(1000);

        // 验证搜索结果
        mockMvc.perform(get("/api/search")
                .param("keyword", "新测试")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.posts[0].id").value(newPost.getId()))
                .andExpect(jsonPath("$.data.posts[0].title").value("新测试帖子"));
    }
} 