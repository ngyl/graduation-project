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

import com.animesocial.platform.model.Favorite;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.ResourceDTO;
import com.animesocial.platform.model.dto.ResourceListResponse;
import com.animesocial.platform.repository.FavoriteRepository;
import com.animesocial.platform.repository.ResourceRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.impl.FavoriteServiceImpl;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private FavoriteServiceImpl favoriteServiceImpl;

    private FavoriteService favoriteService;

    private Favorite testFavorite;
    private Resource testResource;
    private User testUser;
    private ResourceDTO testResourceDTO;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        favoriteService = favoriteServiceImpl;

        // 初始化测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        testResource = new Resource();
        testResource.setId(1);
        testResource.setTitle("测试资源");
        testResource.setDescription("测试资源描述");

        testFavorite = new Favorite();
        testFavorite.setId(1);
        testFavorite.setUserId(1);
        testFavorite.setResourceId(1);
        testFavorite.setCreatedAt(LocalDateTime.now());

        testResourceDTO = new ResourceDTO();
        testResourceDTO.setId(1);
        testResourceDTO.setTitle("测试资源");
        testResourceDTO.setDescription("测试资源描述");
    }

    @Test
    @DisplayName("测试添加收藏")
    void testAddFavorite() {
        // 配置mock行为
        when(userRepository.existsByIdOrUsername(anyInt(), isNull())).thenReturn(true);
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        when(favoriteRepository.findByUserIdAndResourceId(anyInt(), anyInt())).thenReturn(null);
        when(favoriteRepository.insert(any(Favorite.class))).thenReturn(1);

        // 执行测试
        Favorite result = favoriteService.addFavorite(1, 1);

        // 验证结果
        assertNotNull(result);
        assertEquals(testFavorite.getUserId(), result.getUserId());
        assertEquals(testFavorite.getResourceId(), result.getResourceId());
    }

    @Test
    @DisplayName("测试添加已存在的收藏")
    void testAddExistingFavorite() {
        // 配置mock行为
        when(userRepository.existsByIdOrUsername(anyInt(), isNull())).thenReturn(true);
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        when(favoriteRepository.findByUserIdAndResourceId(anyInt(), anyInt())).thenReturn(testFavorite);

        // 执行测试
        Favorite result = favoriteService.addFavorite(1, 1);

        // 验证结果
        assertNotNull(result);
        assertEquals(testFavorite.getId(), result.getId());
    }

    @Test
    @DisplayName("测试取消收藏")
    void testRemoveFavorite() {
        // 配置mock行为
        when(userRepository.existsByIdOrUsername(anyInt(), isNull())).thenReturn(true);
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        when(favoriteRepository.findByUserIdAndResourceId(anyInt(), anyInt())).thenReturn(testFavorite);
        when(favoriteRepository.deleteByUserIdAndResourceId(anyInt(), anyInt())).thenReturn(1);

        // 执行测试
        boolean result = favoriteService.removeFavorite(1, 1);

        // 验证结果
        assertTrue(result);
    }

    @Test
    @DisplayName("测试取消不存在的收藏")
    void testRemoveNonExistingFavorite() {
        // 配置mock行为
        when(userRepository.existsByIdOrUsername(anyInt(), isNull())).thenReturn(true);
        when(resourceRepository.findById(anyInt())).thenReturn(testResource);
        when(favoriteRepository.findByUserIdAndResourceId(anyInt(), anyInt())).thenReturn(null);

        // 执行测试
        boolean result = favoriteService.removeFavorite(1, 1);

        // 验证结果
        assertFalse(result);
    }

    @Test
    @DisplayName("测试检查是否已收藏")
    void testIsFavorited() {
        // 配置mock行为
        when(favoriteRepository.findByUserIdAndResourceId(anyInt(), anyInt())).thenReturn(testFavorite);

        // 执行测试
        boolean result = favoriteService.isFavorited(1, 1);

        // 验证结果
        assertTrue(result);
    }

    @Test
    @DisplayName("测试获取用户收藏列表")
    void testGetUserFavorites() {
        // 配置mock行为
        when(favoriteRepository.findResourceIdsByUserId(anyInt())).thenReturn(Arrays.asList(1));
        when(resourceService.getResourceById(anyInt())).thenReturn(testResourceDTO);

        // 执行测试
        ResourceListResponse result = favoriteService.getUserFavorites(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(testResourceDTO.getTitle(), result.getItems().get(0).getTitle());
    }

    @Test
    @DisplayName("测试分页获取用户收藏列表")
    void testGetUserFavoritesWithPagination() {
        // 配置mock行为
        when(favoriteRepository.findResourceIdsByUserIdWithPagination(anyInt(), anyInt(), anyInt())).thenReturn(Arrays.asList(1));
        when(resourceService.getResourceById(anyInt())).thenReturn(testResourceDTO);

        // 执行测试
        ResourceListResponse result = favoriteService.getUserFavoritesWithPagination(1, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(testResourceDTO.getTitle(), result.getItems().get(0).getTitle());
    }

    @Test
    @DisplayName("测试获取资源被收藏次数")
    void testGetResourceFavoriteCount() {
        // 配置mock行为
        when(favoriteRepository.findByResourceId(anyInt())).thenReturn(Arrays.asList(testFavorite));

        // 执行测试
        int result = favoriteService.getResourceFavoriteCount(1);

        // 验证结果
        assertEquals(1, result);
    }

    @Test
    @DisplayName("测试获取用户收藏数量")
    void testGetUserFavoriteCount() {
        // 配置mock行为
        when(favoriteRepository.countByUserId(anyInt())).thenReturn(1);

        // 执行测试
        int result = favoriteService.getUserFavoriteCount(1);

        // 验证结果
        assertEquals(1, result);
    }
} 