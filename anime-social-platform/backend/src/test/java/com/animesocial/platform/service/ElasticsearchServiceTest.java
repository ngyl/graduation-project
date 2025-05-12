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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.es.PostDocument;
import com.animesocial.platform.model.es.ResourceDocument;
import com.animesocial.platform.model.es.UserDocument;
import com.animesocial.platform.repository.PostRepository;
import com.animesocial.platform.repository.ResourceRepository;
import com.animesocial.platform.repository.UserTagRepository;
import com.animesocial.platform.repository.es.PostDocumentRepository;
import com.animesocial.platform.repository.es.ResourceDocumentRepository;
import com.animesocial.platform.repository.es.UserDocumentRepository;
import com.animesocial.platform.service.impl.ElasticsearchServiceImpl;

@ExtendWith(MockitoExtension.class)
class ElasticsearchServiceTest {

    @Mock
    private PostDocumentRepository postDocumentRepository;

    @Mock
    private ResourceDocumentRepository resourceDocumentRepository;

    @Mock
    private UserDocumentRepository userDocumentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserService userService;

    @Mock
    private TagService tagService;

    @Mock
    private UserTagRepository userTagRepository;

    @InjectMocks
    private ElasticsearchServiceImpl elasticsearchServiceImpl;

    private ElasticsearchService elasticsearchService;

    private Post testPost;
    private Resource testResource;
    private UserDTO testUser;
    private PostDocument testPostDocument;
    private ResourceDocument testResourceDocument;
    private UserDocument testUserDocument;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        elasticsearchService = elasticsearchServiceImpl;

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

        testResource = new Resource();
        testResource.setId(1);
        testResource.setUserId(1);
        testResource.setTitle("测试资源");
        testResource.setDescription("测试描述");
        testResource.setFileUrl("http://example.com/test.jpg");
        testResource.setFileType("image/jpeg");
        testResource.setFileSize(1024);
        testResource.setUploadTime(LocalDateTime.now());
        testResource.setDownloadCount(0);
        testResource.setLikeCount(0);

        testUser = new UserDTO();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setBio("测试简介");
        testUser.setAvatar("http://example.com/avatar.jpg");
        testUser.setIsAdmin(false);
        testUser.setStatus(1);
        testUser.setRegisterTime(LocalDateTime.now());
        testUser.setLastLoginTime(LocalDateTime.now());

        testPostDocument = PostDocument.builder()
                .id(1)
                .userId(1)
                .title("测试帖子")
                .content("测试内容")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .viewCount(0)
                .likeCount(0)
                .isTop(false)
                .username("testuser")
                .tags(Arrays.asList("测试标签"))
                .build();

        testResourceDocument = ResourceDocument.builder()
                .id(1)
                .userId(1)
                .title("测试资源")
                .description("测试描述")
                .fileUrl("http://example.com/test.jpg")
                .fileType("image/jpeg")
                .fileSize(1024)
                .uploadTime(LocalDateTime.now())
                .downloadCount(0)
                .likeCount(0)
                .username("testuser")
                .tags(Arrays.asList("测试标签"))
                .build();

        testUserDocument = UserDocument.builder()
                .id(1)
                .username("testuser")
                .bio("测试简介")
                .avatar("http://example.com/avatar.jpg")
                .isAdmin(false)
                .status(1)
                .registerTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .tags(Arrays.asList("测试标签"))
                .build();
    }

    @Test
    @DisplayName("测试初始化索引")
    void testInitIndices() {
        // 配置mock行为
        when(postRepository.findAllPosts(anyInt(), anyInt())).thenReturn(Arrays.asList(testPost));
        when(resourceRepository.findAll(anyString(), anyInt(), anyInt())).thenReturn(Arrays.asList(testResource));
        when(userService.getAllUsers(anyInt(), anyInt())).thenReturn(Arrays.asList(testUser));
        when(userService.getUserDTOById(anyInt())).thenReturn(testUser);
        when(tagService.getPostTags(anyInt())).thenReturn(Arrays.asList(new TagDTO()));
        when(tagService.getResourceTags(anyInt())).thenReturn(Arrays.asList(new TagDTO()));
        when(userTagRepository.findTagsByUserId(anyInt())).thenReturn(Arrays.asList(new Tag()));
        doNothing().when(postDocumentRepository).save(any(PostDocument.class));
        doNothing().when(resourceDocumentRepository).save(any(ResourceDocument.class));
        doNothing().when(userDocumentRepository).save(any(UserDocument.class));

        // 执行测试
        assertDoesNotThrow(() -> {
            elasticsearchService.initIndices();
        });
    }

    @Test
    @DisplayName("测试重建索引")
    void testRebuildIndices() {
        // 配置mock行为
        doNothing().when(postDocumentRepository).deleteAll();
        doNothing().when(resourceDocumentRepository).deleteAll();
        doNothing().when(userDocumentRepository).deleteAll();
        when(postRepository.findAllPosts(anyInt(), anyInt())).thenReturn(Arrays.asList(testPost));
        when(resourceRepository.findAll(anyString(), anyInt(), anyInt())).thenReturn(Arrays.asList(testResource));
        when(userService.getAllUsers(anyInt(), anyInt())).thenReturn(Arrays.asList(testUser));
        when(userService.getUserDTOById(anyInt())).thenReturn(testUser);
        when(tagService.getPostTags(anyInt())).thenReturn(Arrays.asList(new TagDTO()));
        when(tagService.getResourceTags(anyInt())).thenReturn(Arrays.asList(new TagDTO()));
        when(userTagRepository.findTagsByUserId(anyInt())).thenReturn(Arrays.asList(new Tag()));
        doNothing().when(postDocumentRepository).save(any(PostDocument.class));
        doNothing().when(resourceDocumentRepository).save(any(ResourceDocument.class));
        doNothing().when(userDocumentRepository).save(any(UserDocument.class));

        // 执行测试
        assertDoesNotThrow(() -> {
            elasticsearchService.rebuildIndices();
        });
    }

    @Test
    @DisplayName("测试同步帖子到ES")
    void testSyncPostToEs() {
        // 配置mock行为
        when(postRepository.findById(anyInt())).thenReturn(testPost);
        when(userService.getUserDTOById(anyInt())).thenReturn(testUser);
        when(tagService.getPostTags(anyInt())).thenReturn(Arrays.asList(new TagDTO()));
        doNothing().when(postDocumentRepository).save(any(PostDocument.class));

        // 执行测试
        assertDoesNotThrow(() -> {
            elasticsearchService.syncPostToEs(1);
        });
    }

    @Test
    @DisplayName("测试同步资源到ES")
    void testSyncResourceToEs() {
        // 配置mock行为
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        when(userService.getUserDTOById(anyInt())).thenReturn(testUser);
        when(tagService.getResourceTags(anyInt())).thenReturn(Arrays.asList(new TagDTO()));
        doNothing().when(resourceDocumentRepository).save(any(ResourceDocument.class));

        // 执行测试
        assertDoesNotThrow(() -> {
            elasticsearchService.syncResourceToEs(1);
        });
    }

    @Test
    @DisplayName("测试同步用户到ES")
    void testSyncUserToEs() {
        // 配置mock行为
        when(userService.getUserDTOById(anyInt())).thenReturn(testUser);
        when(userTagRepository.findTagsByUserId(anyInt())).thenReturn(Arrays.asList(new Tag()));
        doNothing().when(userDocumentRepository).save(any(UserDocument.class));

        // 执行测试
        assertDoesNotThrow(() -> {
            elasticsearchService.syncUserToEs(1);
        });
    }

    @Test
    @DisplayName("测试从ES删除帖子")
    void testDeletePostFromEs() {
        // 配置mock行为
        doNothing().when(postDocumentRepository).deleteById(anyInt());

        // 执行测试
        assertDoesNotThrow(() -> {
            elasticsearchService.deletePostFromEs(1);
        });
    }

    @Test
    @DisplayName("测试从ES删除资源")
    void testDeleteResourceFromEs() {
        // 配置mock行为
        doNothing().when(resourceDocumentRepository).deleteById(anyInt());

        // 执行测试
        assertDoesNotThrow(() -> {
            elasticsearchService.deleteResourceFromEs(1);
        });
    }

    @Test
    @DisplayName("测试从ES删除用户")
    void testDeleteUserFromEs() {
        // 配置mock行为
        doNothing().when(userDocumentRepository).deleteById(anyInt());

        // 执行测试
        assertDoesNotThrow(() -> {
            elasticsearchService.deleteUserFromEs(1);
        });
    }

    @Test
    @DisplayName("测试搜索帖子")
    void testSearchPosts() {
        // 配置mock行为
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostDocument> page = new PageImpl<>(Arrays.asList(testPostDocument));
        when(postDocumentRepository.findByTitleOrContentOrderByCreateTimeDesc(anyString(), any(PageRequest.class))).thenReturn(page);

        // 执行测试
        Page<PostDocument> result = elasticsearchService.searchPosts("测试", 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("测试帖子", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("测试搜索资源")
    void testSearchResources() {
        // 配置mock行为
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "uploadTime"));
        Page<ResourceDocument> page = new PageImpl<>(Arrays.asList(testResourceDocument));
        when(resourceDocumentRepository.findByTitleOrDescriptionOrderByUploadTimeDesc(anyString(), any(PageRequest.class))).thenReturn(page);

        // 执行测试
        Page<ResourceDocument> result = elasticsearchService.searchResources("测试", 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("测试资源", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("测试搜索用户")
    void testSearchUsers() {
        // 配置mock行为
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "registerTime"));
        Page<UserDocument> page = new PageImpl<>(Arrays.asList(testUserDocument));
        when(userDocumentRepository.findByUsernameOrBioOrderByCreateTimeDesc(anyString(), any(PageRequest.class))).thenReturn(page);

        // 执行测试
        Page<UserDocument> result = elasticsearchService.searchUsers("测试", 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
    }

    @Test
    @DisplayName("测试全文搜索")
    void testSearchAll() {
        // 配置mock行为
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PostDocument> postPage = new PageImpl<>(Arrays.asList(testPostDocument));
        Page<ResourceDocument> resourcePage = new PageImpl<>(Arrays.asList(testResourceDocument));
        Page<UserDocument> userPage = new PageImpl<>(Arrays.asList(testUserDocument));
        when(postDocumentRepository.findByTitleOrContentOrderByCreateTimeDesc(anyString(), any(PageRequest.class))).thenReturn(postPage);
        when(resourceDocumentRepository.findByTitleOrDescriptionOrderByUploadTimeDesc(anyString(), any(PageRequest.class))).thenReturn(resourcePage);
        when(userDocumentRepository.findByUsernameOrBioOrderByCreateTimeDesc(anyString(), any(PageRequest.class))).thenReturn(userPage);

        // 执行测试
        List<Object> result = elasticsearchService.searchAll("测试", 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
    }
} 