package com.animesocial.platform.service.impl;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.ResourceDTO;
import com.animesocial.platform.repository.FavoriteRepository;
import com.animesocial.platform.repository.ResourceRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.ResourceService;
import com.animesocial.platform.service.TagService;
import com.animesocial.platform.util.FileStorageUtil;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资源服务实现类
 */
@Service
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private FileStorageUtil fileStorageUtil;
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @Value("${app.file.domain}")
    private String fileDomain;

    /**
     * 获取资源详情
     */
    @Override
    public ResourceDTO getResourceById(Integer id) {
        Resource resource = resourceRepository.findById(id);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        ResourceDTO dto = convertToDTO(resource);
        
        // 加载用户信息
        User user = userRepository.findById(resource.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }
        
        // 加载标签
        dto.setTags(tagService.getResourceTags(id));
        
        return dto;
    }

    /**
     * 获取用户的资源列表
     */
    @Override
    public List<ResourceDTO> getResourcesByUserId(Integer userId) {
        List<Resource> resources = resourceRepository.findByUserId(userId);
        return resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取资源列表
     */
    @Override
    public Map<String, Object> getAllResources(Integer page, Integer size, Integer tagId, String sort) {
        int offset = (page - 1) * size;
        String orderBy;
        
        // 确定排序方式
        switch (sort) {
            case "downloads":
                orderBy = "download_count DESC";
                break;
            case "likes":
                orderBy = "like_count DESC";
                break;
            case "latest":
            default:
                orderBy = "upload_time DESC";
                break;
        }
        
        List<Resource> resources;
        Integer total;
        
        // 根据标签筛选
        if (tagId != null) {
            resources = resourceRepository.findByTagId(tagId, orderBy, offset, size);
            total = resourceRepository.countByTagId(tagId);
        } else {
            resources = resourceRepository.findAll(orderBy, offset, size);
            total = resourceRepository.count();
        }
        
        // 转换为DTO
        List<ResourceDTO> dtoList = resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 补充额外信息
        for (ResourceDTO dto : dtoList) {
            User user = userRepository.findById(dto.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            }
            
            dto.setTags(tagService.getResourceTags(dto.getId()));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("resources", dtoList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }

    /**
     * 上传资源
     */
    @Override
    @Transactional
    public ResourceDTO uploadResource(Integer userId, String title, String description, List<Integer> tagIds, MultipartFile file) {
        // 验证用户
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        try {
            // 保存文件
            String originalFilename = file.getOriginalFilename();
            String fileType = getFileType(originalFilename);
            String storedFileName = fileStorageUtil.storeFile(file);
            String fileUrl = fileDomain + storedFileName;
            
            // 创建资源记录
            Resource resource = new Resource();
            resource.setUserId(userId);
            resource.setTitle(title);
            resource.setDescription(description);
            resource.setFileUrl(fileUrl);
            resource.setFileType(fileType);
            resource.setFileSize((int)(file.getSize() / 1024)); // 转为KB
            resource.setUploadTime(LocalDateTime.now());
            resource.setDownloadCount(0);
            resource.setLikeCount(0);
            
            // 保存资源
            resourceRepository.insert(resource);
            
            // 处理标签
            if (tagIds != null && !tagIds.isEmpty()) {
                tagService.updateResourceTags(resource.getId(), tagIds);
            }
            
            // 转换为DTO
            ResourceDTO dto = convertToDTO(resource);
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
            
            if (tagIds != null && !tagIds.isEmpty()) {
                dto.setTags(tagService.getResourceTags(resource.getId()));
            } else {
                dto.setTags(new ArrayList<>());
            }
            
            return dto;
            
        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 更新资源信息
     */
    @Override
    @Transactional
    public ResourceDTO updateResource(Integer id, String title, String description, List<Integer> tagIds) {
        // 验证资源是否存在
        Resource resource = resourceRepository.findById(id);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 更新资源信息
        resource.setTitle(title);
        resource.setDescription(description);
        
        // 保存更新
        resourceRepository.update(resource);
        
        // 更新标签
        if (tagIds != null) {
            tagService.updateResourceTags(id, tagIds);
        }
        
        // 获取完整信息
        return getResourceById(id);
    }

    /**
     * 删除资源
     */
    @Override
    @Transactional
    public void deleteResource(Integer id) {
        // 验证资源是否存在
        Resource resource = resourceRepository.findById(id);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 删除关联的标签
        tagService.updateResourceTags(id, new ArrayList<>());
        
        // 删除关联的收藏
        favoriteRepository.deleteByResourceId(id);
        
        // 删除资源记录
        resourceRepository.delete(id);
        
        // 删除物理文件
        String fileName = resource.getFileUrl().replace(fileDomain, "");
        try {
            fileStorageUtil.deleteFile(fileName);
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileName, e);
            // 文件删除失败不影响数据库操作
        }
    }

    /**
     * 收藏资源
     */
    @Override
    @Transactional
    public String favoriteResource(Integer resourceId, Integer userId) {
        // 验证资源
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 验证用户
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查是否已收藏
        if (isResourceFavorited(resourceId, userId)) {
            throw new BusinessException("已经收藏过该资源");
        }
        
        // 添加收藏
        favoriteRepository.insert(userId, resourceId);
        
        return "收藏成功";
    }

    /**
     * 取消收藏
     */
    @Override
    @Transactional
    public String unfavoriteResource(Integer resourceId, Integer userId) {
        // 验证是否已收藏
        if (!isResourceFavorited(resourceId, userId)) {
            throw new BusinessException("未收藏该资源");
        }
        
        // 删除收藏
        favoriteRepository.delete(userId, resourceId);
        
        return "已取消收藏";
    }

    /**
     * 检查是否已收藏
     */
    @Override
    public boolean isResourceFavorited(Integer resourceId, Integer userId) {
        return favoriteRepository.exists(userId, resourceId);
    }

    /**
     * 获取下载URL
     */
    @Override
    public String getDownloadUrl(Integer resourceId, Integer userId) {
        // 验证资源
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 验证用户
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 生成下载URL（简单实现，实际可能需要生成签名URL等）
        return resource.getFileUrl() + "?download=true";
    }

    /**
     * 增加下载计数
     */
    @Override
    public void incrementDownloadCount(Integer resourceId) {
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        resourceRepository.incrementDownloadCount(resourceId);
    }

    /**
     * 搜索资源
     */
    @Override
    public Map<String, Object> searchResources(String keyword, Integer page, Integer size) {
        int offset = (page - 1) * size;
        
        List<Resource> resources = resourceRepository.search(keyword, offset, size);
        Integer total = resourceRepository.countSearch(keyword);
        
        // 转换为DTO
        List<ResourceDTO> dtoList = resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 补充额外信息
        for (ResourceDTO dto : dtoList) {
            User user = userRepository.findById(dto.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            }
            
            dto.setTags(tagService.getResourceTags(dto.getId()));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("resources", dtoList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }
    
    /**
     * 将Resource实体转换为ResourceDTO
     */
    private ResourceDTO convertToDTO(Resource resource) {
        ResourceDTO dto = new ResourceDTO();
        BeanUtils.copyProperties(resource, dto);
        return dto;
    }
    
    /**
     * 从文件名获取文件类型
     */
    private String getFileType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unknown";
        }
        
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "unknown";
        }
        
        String extension = filename.substring(dotIndex + 1).toLowerCase();
        
        // 简单的文件类型判断
        if (extension.matches("jpg|jpeg|png|gif|webp|bmp")) {
            return "image";
        } else if (extension.matches("mp4|avi|mov|wmv|flv|mkv")) {
            return "video";
        } else if (extension.matches("mp3|ogg|wav|flac|aac")) {
            return "audio";
        } else if (extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt")) {
            return "document";
        } else if (extension.matches("zip|rar|7z|tar|gz")) {
            return "archive";
        }
        
        return extension;
    }
} 