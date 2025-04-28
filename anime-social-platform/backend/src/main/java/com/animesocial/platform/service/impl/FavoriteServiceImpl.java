package com.animesocial.platform.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.Favorite;
import com.animesocial.platform.repository.FavoriteRepository;
import com.animesocial.platform.repository.ResourceRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.FavoriteService;
import com.animesocial.platform.service.ResourceService;
import com.animesocial.platform.model.dto.ResourceListResponse;
import com.animesocial.platform.model.dto.ResourceDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 收藏服务实现类
 * 实现FavoriteService接口定义的所有业务方法
 */
@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ResourceService resourceService;
    
    /**
     * 添加收藏
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 收藏对象
     * @throws BusinessException 如果用户或资源不存在
     */
    @Override
    @Transactional
    public Favorite addFavorite(Integer userId, Integer resourceId) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查资源是否存在
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 检查是否已收藏
        Favorite existFavorite = favoriteRepository.findByUserIdAndResourceId(userId, resourceId);
        if (existFavorite != null) {
            return existFavorite; // 已收藏，直接返回
        }
        
        // 创建收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setResourceId(resourceId);
        favorite.setCreatedAt(LocalDateTime.now());
        
        // 保存收藏
        favoriteRepository.insert(favorite);
        
        return favorite;
    }
    
    /**
     * 取消收藏
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否成功
     * @throws BusinessException 如果用户或资源不存在
     */
    @Override
    @Transactional
    public boolean removeFavorite(Integer userId, Integer resourceId) {
        // 检查用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查资源是否存在
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 检查是否已收藏
        Favorite favorite = favoriteRepository.findByUserIdAndResourceId(userId, resourceId);
        if (favorite == null) {
            return false; // 未收藏，返回false
        }
        
        // 删除收藏
        int result = favoriteRepository.deleteByUserIdAndResourceId(userId, resourceId);
        
        return result > 0;
    }
    
    /**
     * 检查用户是否已收藏资源
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return true表示已收藏，false表示未收藏
     */
    @Override
    public boolean isFavorited(Integer userId, Integer resourceId) {
        Favorite favorite = favoriteRepository.findByUserIdAndResourceId(userId, resourceId);
        return favorite != null;
    }
    
    /**
     * 获取用户的收藏"资源"列表
     * 
     * @param userId 用户ID
     * @return 收藏DTO列表
     */
    @Override
    public ResourceListResponse getUserFavorites(Integer userId) {
        // 获取收藏的资源ID列表
        List<Integer> resourceIds = favoriteRepository.findResourceIdsByUserId(userId);
        
        // 使用ResourceService获取资源详情
        ResourceListResponse response = new ResourceListResponse();
        List<ResourceDTO> resourceDTOs = new ArrayList<>();
        
        // 对每个资源ID调用resourceService.getResourceById
        for (Integer resourceId : resourceIds) {
            try {
                ResourceDTO resourceDTO = resourceService.getResourceById(resourceId);
                // 已收藏标记设为true
                resourceDTO.setIsFavorited(true);
                resourceDTOs.add(resourceDTO);
            } catch (Exception e) {
                log.error("获取资源详情失败，资源ID: " + resourceId, e);
                // 忽略不存在的资源，继续处理其他资源
            }
        }
        
        response.setItems(resourceDTOs);
        response.setTotal(resourceDTOs.size());
        
        return response;
    }
    
    /**
     * 分页获取用户的收藏"资源"列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 包含收藏列表和总数的Map
     */
    @Override
    public ResourceListResponse getUserFavoritesWithPagination(Integer userId, Integer page, Integer size) {        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 使用分页方法查询资源ID
        List<Integer> resourceIds = favoriteRepository.findResourceIdsByUserIdWithPagination(userId, offset, size);
        
        // 使用ResourceService获取资源详情
        ResourceListResponse response = new ResourceListResponse();
        List<ResourceDTO> resourceDTOs = new ArrayList<>();
        
        // 对每个资源ID调用resourceService.getResourceById
        for (Integer resourceId : resourceIds) {
            try {
                ResourceDTO resourceDTO = resourceService.getResourceById(resourceId);
                // 已收藏标记设为true
                resourceDTO.setIsFavorited(true);
                resourceDTOs.add(resourceDTO);
            } catch (Exception e) {
                log.error("获取资源详情失败，资源ID: " + resourceId, e);
                // 忽略不存在的资源，继续处理其他资源
            }
        }
        
        response.setItems(resourceDTOs);
        response.setTotal(resourceDTOs.size());
        
        return response;
    }
    
    /**
     * 获取用户的收藏数量
     * 
     * @param userId 用户ID
     * @return 收藏数量
     */
    @Override
    public int getUserFavoriteCount(Integer userId) {
        return favoriteRepository.countByUserId(userId);
    }
    
    /**
     * 获取资源被收藏的数量
     * 
     * @param resourceId 资源ID
     * @return 收藏数量
     */
    @Override
    public int getResourceFavoriteCount(Integer resourceId) {
        List<Favorite> favorites = favoriteRepository.findByResourceId(resourceId);
        return favorites.size();
    }
} 