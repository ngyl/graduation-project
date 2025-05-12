package com.animesocial.platform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.dto.ResourceDTO;
import com.animesocial.platform.model.dto.ResourceListResponse;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.repository.FavoriteRepository;
import com.animesocial.platform.repository.ResourceLikeRepository;
import com.animesocial.platform.repository.ResourceRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.impl.ResourceServiceImpl;
import com.animesocial.platform.util.FileStorageUtil;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ResourceLikeRepository resourceLikeRepository;

    @Mock
    private TagService tagService;

    @Mock
    private UserService userService;

    @Mock
    private FileStorageUtil fileStorageUtil;

    @InjectMocks
    private ResourceServiceImpl resourceServiceImpl;

    private ResourceService resourceService;

    private Resource testResource;
    private ResourceDTO testResourceDTO;
    private UserDTO testUserDTO;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        resourceService = resourceServiceImpl;

        // 初始化测试数据
        testResource = new Resource();
        testResource.setId(1);
        testResource.setUserId(1);
        testResource.setTitle("测试资源");
        testResource.setDescription("测试资源描述");
        testResource.setFileUrl("http://example.com/test.jpg");
        testResource.setFileType("image/jpeg");
        testResource.setFileSize(1024);
        testResource.setUploadTime(LocalDateTime.now());
        testResource.setDownloadCount(0);
        testResource.setLikeCount(0);

        testResourceDTO = new ResourceDTO();
        testResourceDTO.setId(1);
        testResourceDTO.setUserId(1);
        testResourceDTO.setTitle("测试资源");
        testResourceDTO.setDescription("测试资源描述");
        testResourceDTO.setFileUrl("http://example.com/test.jpg");
        testResourceDTO.setFileType("image/jpeg");
        testResourceDTO.setFileSize(1024);
        testResourceDTO.setUploadTime(LocalDateTime.now());
        testResourceDTO.setDownloadCount(0);
        testResourceDTO.setLikeCount(0);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setUsername("testuser");
        testUserDTO.setAvatar("http://example.com/avatar.jpg");

        testFile = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("测试根据ID获取资源")
    void testGetResourceById() {
        // 配置mock行为
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);
        when(tagService.getResourceTags(anyInt())).thenReturn(Arrays.asList());
        when(favoriteRepository.findByResourceId(anyInt())).thenReturn(Arrays.asList());

        // 执行测试
        ResourceDTO result = resourceService.getResourceById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(testResource.getTitle(), result.getTitle());
        assertEquals(testResource.getDescription(), result.getDescription());
        assertEquals(testUserDTO.getUsername(), result.getUsername());
    }

    @Test
    @DisplayName("测试获取不存在的资源")
    void testGetNonExistentResource() {
        // 配置mock行为
        when(resourceRepository.findById(anyInt())).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            resourceService.getResourceById(999);
        });
    }

    @Test
    @DisplayName("测试获取用户的资源列表")
    void testGetResourcesByUserId() {
        // 配置mock行为
        when(resourceRepository.findByUserId(anyInt(), anyInt(), anyInt())).thenReturn(Arrays.asList(testResource));

        // 执行测试
        List<ResourceDTO> result = resourceService.getResourcesByUserId(1, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testResource.getTitle(), result.get(0).getTitle());
    }

    @Test
    @DisplayName("测试获取所有资源")
    void testGetAllResources() {
        // 配置mock行为
        when(resourceRepository.findAll(anyString(), anyInt(), anyInt())).thenReturn(Arrays.asList(testResource));
        when(resourceRepository.count()).thenReturn(1);

        // 执行测试
        ResourceListResponse result = resourceService.getAllResources(1, 10, null, "latest");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试上传资源")
    void testUploadResource() throws IOException {
        // 配置mock行为
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);
        when(fileStorageUtil.storeResourceFile(any(), anyString())).thenReturn("test.jpg");
        when(fileStorageUtil.getPublicUrl(anyString())).thenReturn("http://example.com/test.jpg");
        doNothing().when(resourceRepository).insert(any(Resource.class));
        doNothing().when(tagService).updateResourceTags(anyInt(), anyList());

        // 执行测试
        ResourceDTO result = resourceService.uploadResource(1, "测试资源", "测试描述", Arrays.asList(1, 2), testFile);

        // 验证结果
        assertNotNull(result);
        assertEquals("测试资源", result.getTitle());
        assertEquals("测试描述", result.getDescription());
    }

    @Test
    @DisplayName("测试更新资源")
    void testUpdateResource() {
        // 配置mock行为
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        doNothing().when(resourceRepository).update(any(Resource.class));
        doNothing().when(tagService).updateResourceTags(anyInt(), anyList());
        when(userService.getUserDTOById(anyInt())).thenReturn(testUserDTO);
        when(tagService.getResourceTags(anyInt())).thenReturn(Arrays.asList());
        when(favoriteRepository.findByResourceId(anyInt())).thenReturn(Arrays.asList());

        // 执行测试
        ResourceDTO result = resourceService.updateResource(1, "更新后的标题", "更新后的描述", Arrays.asList(1, 2));

        // 验证结果
        assertNotNull(result);
        assertEquals("更新后的标题", result.getTitle());
        assertEquals("更新后的描述", result.getDescription());
    }

    @Test
    @DisplayName("测试删除资源")
    void testDeleteResource() {
        // 配置mock行为
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        doNothing().when(tagService).updateResourceTags(anyInt(), anyList());
        doNothing().when(favoriteRepository).deleteByResourceId(anyInt());
        doNothing().when(resourceRepository).delete(anyInt());
        doNothing().when(fileStorageUtil).deleteFile(anyString());

        // 执行测试
        assertDoesNotThrow(() -> {
            resourceService.deleteResource(1);
        });
    }

    @Test
    @DisplayName("测试点赞资源")
    void testLikeResource() {
        // 配置mock行为
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        when(resourceLikeRepository.exists(anyInt(), anyInt())).thenReturn(false);
        doNothing().when(resourceLikeRepository).save(anyInt(), anyInt());
        doNothing().when(resourceRepository).incrementLikeCount(anyInt());

        // 执行测试
        String result = resourceService.likeResource(1, 2);

        // 验证结果
        assertEquals("点赞成功", result);
    }

    @Test
    @DisplayName("测试取消点赞")
    void testUnlikeResource() {
        // 配置mock行为
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        when(resourceLikeRepository.exists(anyInt(), anyInt())).thenReturn(true);
        doNothing().when(resourceLikeRepository).delete(anyInt(), anyInt());
        doNothing().when(resourceRepository).decrementLikeCount(anyInt());

        // 执行测试
        String result = resourceService.unlikeResource(1, 2);

        // 验证结果
        assertEquals("取消点赞成功", result);
    }

    @Test
    @DisplayName("测试检查资源是否已点赞")
    void testIsResourceLiked() {
        // 配置mock行为
        when(resourceLikeRepository.exists(anyInt(), anyInt())).thenReturn(true);

        // 执行测试
        boolean result = resourceService.isResourceLiked(1, 2);

        // 验证结果
        assertTrue(result);
    }

    @Test
    @DisplayName("测试获取用户点赞的资源列表")
    void testGetLikedResources() {
        // 配置mock行为
        when(resourceLikeRepository.findResourceIdsByUserId(anyInt())).thenReturn(Arrays.asList(1));
        when(resourceRepository.findByIds(anyList())).thenReturn(Arrays.asList(testResource));
        when(resourceRepository.count()).thenReturn(1);

        // 执行测试
        ResourceListResponse result = resourceService.getLikedResources(1, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试搜索资源")
    void testSearchResources() {
        // 配置mock行为
        when(resourceRepository.search(anyString(), anyInt(), anyInt())).thenReturn(Arrays.asList(testResource));
        when(resourceRepository.countSearch(anyString())).thenReturn(1);

        // 执行测试
        ResourceListResponse result = resourceService.searchResources("测试", 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试获取热门资源")
    void testGetHotResources() {
        // 配置mock行为
        when(resourceRepository.findAll(anyString(), anyInt(), anyInt())).thenReturn(Arrays.asList(testResource));
        when(resourceRepository.count()).thenReturn(1);

        // 执行测试
        ResourceListResponse result = resourceService.getHotResources(10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotal());
    }
} 