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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.dto.CreatePostRequest;
import com.animesocial.platform.model.dto.PostDTO;
import com.animesocial.platform.model.dto.PostListResponse;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.repository.PostLikeRepository;
import com.animesocial.platform.repository.PostRepository;
import com.animesocial.platform.repository.TagRepository;
import com.animesocial.platform.service.impl.PostServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private TagService tagService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private PostServiceImpl postServiceImpl;

    private PostService postService;

    private Post testPost;
    private PostDTO testPostDTO;
    private CreatePostRequest testCreateRequest;
    private UserDTO testUserDTO;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        postService = postServiceImpl;

        // 初始化测试数据
        testPost = new Post();
        testPost.setId(1);
        testPost.setTitle("测试帖子");
        testPost.setContent("测试内容");
        testPost.setUserId(1);
        testPost.setViewCount(0);
        testPost.setLikeCount(0);
        testPost.setIsTop(false);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        testPostDTO = new PostDTO();
        testPostDTO.setId(1);
        testPostDTO.setTitle("测试帖子");
        testPostDTO.setContent("测试内容");
        testPostDTO.setViewCount(0);
        testPostDTO.setLikeCount(0);
        testPostDTO.setCommentCount(0);
        testPostDTO.setIsTop(false);
        testPostDTO.setIsLiked(false);

        testCreateRequest = new CreatePostRequest();
        testCreateRequest.setTitle("测试帖子");
        testCreateRequest.setContent("测试内容");
        testCreateRequest.setTagIds(Arrays.asList(1, 2));

        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setUsername("testuser");

        testTag = new Tag();
        testTag.setId(1);
        testTag.setName("测试标签");
        testTag.setType("post");
        testTag.setCategory("动漫");

        // 设置RequestContextHolder
        when(request.getSession()).thenReturn(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("测试根据ID获取帖子")
    void testGetPostById() {
        // 配置mock行为
        when(postRepository.findById(anyInt())).thenReturn(testPost);
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);
        when(tagRepository.findByPostId(anyInt())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        PostDTO result = postService.getPostById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(testPost.getTitle(), result.getTitle());
        assertEquals(testPost.getContent(), result.getContent());
    }

    @Test
    @DisplayName("测试获取用户的帖子列表")
    void testGetPostsByUserId() {
        // 配置mock行为
        when(postRepository.findByUserId(anyInt(), anyInt(), anyInt())).thenReturn(Arrays.asList(testPost));
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);
        when(tagRepository.findByPostId(anyInt())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        List<PostDTO> result = postService.getPostsByUserId(1, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPost.getTitle(), result.get(0).getTitle());
    }

    @Test
    @DisplayName("测试创建帖子")
    void testCreatePost() {
        // 配置mock行为
        doNothing().when(postRepository).save(any(Post.class));
        when(tagRepository.findById(anyInt())).thenReturn(testTag);
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);

        // 执行测试
        PostDTO result = postService.createPost(1, testCreateRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(testCreateRequest.getTitle(), result.getTitle());
        assertEquals(testCreateRequest.getContent(), result.getContent());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("测试更新帖子")
    void testUpdatePost() {
        // 配置mock行为
        when(postRepository.findById(anyInt())).thenReturn(testPost);
        doNothing().when(postRepository).update(any(Post.class));
        when(tagRepository.findById(anyInt())).thenReturn(testTag);
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);

        // 执行测试
        PostDTO result = postService.updatePost(1, testCreateRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(testCreateRequest.getTitle(), result.getTitle());
        assertEquals(testCreateRequest.getContent(), result.getContent());
        verify(postRepository).update(any(Post.class));
    }

    @Test
    @DisplayName("测试删除帖子")
    void testDeletePost() {
        // 配置mock行为
        when(postRepository.findById(anyInt())).thenReturn(testPost);
        doNothing().when(postRepository).deleteById(anyInt());

        // 执行测试
        assertDoesNotThrow(() -> postService.deletePost(1));

        // 验证mock调用
        verify(postRepository).deleteById(1);
    }

    @Test
    @DisplayName("测试点赞帖子")
    void testLikePost() {
        // 配置mock行为
        when(postRepository.findById(anyInt())).thenReturn(testPost);
        when(postLikeRepository.exists(anyInt(), anyInt())).thenReturn(false);
        doNothing().when(postLikeRepository).save(anyInt(), anyInt());
        doNothing().when(postRepository).increaseLikeCount(anyInt());

        // 执行测试
        String result = postService.likePost(1, 1);

        // 验证结果
        assertNotNull(result);
        verify(postLikeRepository).save(1, 1);
        verify(postRepository).increaseLikeCount(1);
    }

    @Test
    @DisplayName("测试取消点赞")
    void testUnlikePost() {
        // 配置mock行为
        when(postRepository.findById(anyInt())).thenReturn(testPost);
        when(postLikeRepository.exists(anyInt(), anyInt())).thenReturn(true);
        doNothing().when(postLikeRepository).delete(anyInt(), anyInt());
        doNothing().when(postRepository).decreaseLikeCount(anyInt());

        // 执行测试
        String result = postService.unlikePost(1, 1);

        // 验证结果
        assertNotNull(result);
        verify(postLikeRepository).delete(1, 1);
        verify(postRepository).decreaseLikeCount(1);
    }

    @Test
    @DisplayName("测试检查帖子是否已点赞")
    void testIsPostLiked() {
        // 配置mock行为
        when(postLikeRepository.exists(anyInt(), anyInt())).thenReturn(true);

        // 执行测试
        boolean result = postService.isPostLiked(1, 1);

        // 验证结果
        assertTrue(result);
        verify(postLikeRepository).exists(1, 1);
    }

    @Test
    @DisplayName("测试获取用户点赞的帖子列表")
    void testGetLikedPosts() {
        // 配置mock行为
        when(postRepository.findLikedPostsByUserIdPaged(anyInt(), anyInt(), anyInt())).thenReturn(Arrays.asList(testPost));
        when(postRepository.countLikedPostsByUserId(anyInt())).thenReturn(1);
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);
        when(tagRepository.findByPostId(anyInt())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        PostListResponse result = postService.getLikedPosts(1, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试增加帖子浏览量")
    void testIncreaseViewCount() {
        // 配置mock行为
        when(postRepository.findById(anyInt())).thenReturn(testPost);
        doNothing().when(postRepository).increaseViewCount(anyInt());

        // 执行测试
        assertDoesNotThrow(() -> postService.increaseViewCount(1));

        // 验证mock调用
        verify(postRepository).increaseViewCount(1);
    }

    @Test
    @DisplayName("测试设置帖子置顶状态")
    void testSetPostTop() {
        // 配置mock行为
        when(postRepository.findById(anyInt())).thenReturn(testPost);
        doNothing().when(postRepository).updateTopStatus(anyInt(), anyInt());

        // 执行测试
        assertDoesNotThrow(() -> postService.setPostTop(1, 1));

        // 验证mock调用
        verify(postRepository).updateTopStatus(1, 1);
    }

    @Test
    @DisplayName("测试获取置顶帖子列表")
    void testGetTopPosts() {
        // 配置mock行为
        when(postRepository.findTopPosts()).thenReturn(Arrays.asList(testPost));
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);
        when(tagRepository.findByPostId(anyInt())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        List<PostDTO> result = postService.getTopPosts();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsTop());
    }

    @Test
    @DisplayName("测试搜索帖子")
    void testSearchPosts() {
        // 配置mock行为
        when(postRepository.searchPosts(anyString(), anyInt(), anyInt())).thenReturn(Arrays.asList(testPost));
        when(postRepository.countSearchPosts(anyString())).thenReturn(1);
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);
        when(tagRepository.findByPostId(anyInt())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        PostListResponse result = postService.searchPosts("测试", 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试获取热门帖子")
    void testGetHotPosts() {
        // 配置mock行为
        when(postRepository.findHotPosts(anyInt())).thenReturn(Arrays.asList(testPost));
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);
        when(tagRepository.findByPostId(anyInt())).thenReturn(Arrays.asList(testTag));

        // 执行测试
        PostListResponse result = postService.getHotPosts(10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotal());
    }
} 