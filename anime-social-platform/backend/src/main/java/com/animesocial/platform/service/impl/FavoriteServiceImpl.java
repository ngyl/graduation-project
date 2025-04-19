package com.animesocial.platform.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.ResourceDTO;
import com.animesocial.platform.repository.FavoriteRepository;
import com.animesocial.platform.repository.ResourceRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.FavoriteService;

import lombok.extern.slf4j.Slf4j;

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
    
    /**
     * 添加收藏
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 操作结果描述
     * @throws BusinessException 如果用户或资源不存在，或已收藏
     */
    @Override
    @Transactional
    public String addFavorite(Integer userId, Integer resourceId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证资源是否存在
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 检查是否已收藏
        boolean isFavorite = favoriteRepository.exists(userId, resourceId);
        if (isFavorite) {
            throw new BusinessException("已经收藏过该资源");
        }
        
        // 添加收藏记录
        favoriteRepository.insert(userId, resourceId);
        
        return "收藏成功";
    }
    
    /**
     * 取消收藏
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 操作结果描述
     * @throws BusinessException 如果用户或资源不存在，或未收藏
     */
    @Override
    @Transactional
    public String removeFavorite(Integer userId, Integer resourceId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证资源是否存在
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 检查是否已收藏
        boolean isFavorite = favoriteRepository.exists(userId, resourceId);
        if (!isFavorite) {
            throw new BusinessException("未收藏该资源");
        }
        
        // 删除收藏记录
        favoriteRepository.delete(userId, resourceId);
        
        return "取消收藏成功";
    }
    
    /**
     * 检查用户是否已收藏资源
     * 
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return true表示已收藏，false表示未收藏
     */
    @Override
    public boolean isFavorite(Integer userId, Integer resourceId) {
        return favoriteRepository.exists(userId, resourceId);
    }
    
    /**
     * 获取用户的收藏列表
     * 
     * @param userId 用户ID
     * @return 资源列表
     */
    @Override
    public List<ResourceDTO> getUserFavorites(Integer userId) {
        List<Resource> resources = resourceRepository.findByUserId(userId);
        
        return convertToDTO(resources, userId);
    }
    
    /**
     * 分页获取用户的收藏列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 资源列表
     */
    @Override
    public List<ResourceDTO> getUserFavorites(Integer userId, Integer page, Integer size) {
        // 当前实现不支持分页，返回全部收藏
        // TODO: 实现分页功能
        return getUserFavorites(userId);
    }
    
    /**
     * 获取用户的收藏数量
     * 
     * @param userId 用户ID
     * @return 收藏数量
     */
    @Override
    public int countUserFavorites(Integer userId) {
        return favoriteRepository.countByUserId(userId);
    }
    
    /**
     * 将资源实体列表转换为DTO列表
     * 
     * @param resources 资源实体列表
     * @param userId 当前用户ID，用于判断是否已收藏
     * @return 资源DTO列表
     */
    private List<ResourceDTO> convertToDTO(List<Resource> resources, Integer userId) {
        return resources.stream()
            .map(resource -> {
                ResourceDTO dto = new ResourceDTO();
                BeanUtils.copyProperties(resource, dto);
                
                // 获取上传用户名
                User uploader = userRepository.findById(resource.getUserId());
                if (uploader != null) {
                    dto.setUsername(uploader.getUsername());
                }
                
                // 设置是否已收藏
                dto.setIsFavorited(favoriteRepository.exists(userId, resource.getId()));
                
                return dto;
            })
            .collect(Collectors.toList());
    }
} 