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

import com.animesocial.platform.model.Comment;
import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.CommentDTO;
import com.animesocial.platform.model.dto.CreateCommentRequest;
import com.animesocial.platform.repository.CommentRepository;
import com.animesocial.platform.service.impl.CommentServiceImpl;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @InjectMocks
    private CommentServiceImpl commentServiceImpl;

    private CommentService commentService;

    private Comment testComment;
    private CommentDTO testCommentDTO;
    private CreateCommentRequest testCreateRequest;
    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        commentService = commentServiceImpl;

        // 初始化测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        testPost = new Post();
        testPost.setId(1);
        testPost.setTitle("测试帖子");

        testComment = new Comment();
        testComment.setId(1);
        testComment.setContent("测试评论");
        testComment.setUserId(1);
        testComment.setPostId(1);
        testComment.setCreatedAt(LocalDateTime.now());

        testCommentDTO = new CommentDTO();
        testCommentDTO.setId(1);
        testCommentDTO.setContent("测试评论");
        testCommentDTO.setUserId(1);
        testCommentDTO.setPostId(1);
        testCommentDTO.setCreatedAt(LocalDateTime.now());

        testCreateRequest = new CreateCommentRequest();
        testCreateRequest.setContent("测试评论");
        testCreateRequest.setPostId(1);
    }

    @Test
    @DisplayName("测试根据ID获取评论")
    void testGetCommentById() {
        // 配置mock行为
        when(commentRepository.findById(anyInt())).thenReturn(testComment);
        when(userService.getUserDTOById(anyInt())).thenReturn(null);

        // 执行测试
        CommentDTO result = commentService.getCommentById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(testComment.getContent(), result.getContent());
    }

    @Test
    @DisplayName("测试根据帖子ID获取评论列表")
    void testGetCommentsByPostId() {
        // 配置mock行为
        when(commentRepository.findByPostId(anyInt(), isNull())).thenReturn(Arrays.asList(testComment));
        when(commentRepository.findByPostId(anyInt(), anyInt())).thenReturn(Arrays.asList());
        when(userService.getUserDTOById(anyInt())).thenReturn(null);

        // 执行测试
        List<CommentDTO> result = commentService.getCommentsByPostId(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testComment.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("测试创建评论")
    void testCreateComment() {
        // 配置mock行为
        doNothing().when(commentRepository).insert(any(Comment.class));
        when(userService.getUserDTOById(anyInt())).thenReturn(null);

        // 执行测试
        CommentDTO result = commentService.createComment(1, testCreateRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(testCreateRequest.getContent(), result.getContent());
        verify(commentRepository).insert(any(Comment.class));
    }

    @Test
    @DisplayName("测试删除评论")
    void testDeleteComment() {
        // 配置mock行为
        when(commentRepository.findById(anyInt())).thenReturn(testComment);
        doNothing().when(commentRepository).delete(anyInt());

        // 执行测试
        assertDoesNotThrow(() -> commentService.deleteComment(1));

        // 验证mock调用
        verify(commentRepository).delete(1);
    }

    @Test
    @DisplayName("测试获取帖子评论数量")
    void testGetCommentCountByPostId() {
        // 配置mock行为
        when(commentRepository.countByPostId(anyInt())).thenReturn(1);

        // 执行测试
        Integer result = commentService.getCommentCountByPostId(1);

        // 验证结果
        assertEquals(1, result);
    }

    @Test
    @DisplayName("测试检查是否是回复评论")
    void testIsReplyComment() {
        // 配置mock行为
        when(commentRepository.findById(anyInt())).thenReturn(testComment);

        // 执行测试
        boolean result = commentService.isReplyComment(1);

        // 验证结果
        assertFalse(result);
    }

    @Test
    @DisplayName("测试获取评论的回复列表")
    void testGetRepliesByCommentId() {
        // 配置mock行为
        when(commentRepository.findById(anyInt())).thenReturn(testComment);
        when(commentRepository.findByParentId(anyInt())).thenReturn(Arrays.asList(testComment));
        when(userService.getUserDTOById(anyInt())).thenReturn(null);

        // 执行测试
        List<CommentDTO> result = commentService.getRepliesByCommentId(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testComment.getContent(), result.get(0).getContent());
    }
} 