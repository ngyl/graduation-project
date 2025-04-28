package com.animesocial.platform.service.impl;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.dto.ResourceDTO;
import com.animesocial.platform.model.dto.ResourceListResponse;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.repository.FavoriteRepository;
import com.animesocial.platform.repository.ResourceRepository;
import com.animesocial.platform.repository.ResourceLikeRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.ResourceService;
import com.animesocial.platform.service.TagService;
import com.animesocial.platform.service.UserService;
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
import java.util.Collections;
import java.util.List;
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
    private ResourceLikeRepository resourceLikeRepository;
    
    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

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
        UserDTO user = userService.getUserDTOById(resource.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }
        
        // 加载标签
        dto.setTags(tagService.getResourceTags(id));

        // 加载收藏次数
        dto.setFavoriteCount(favoriteRepository.findByResourceId(id).size());
        
        return dto;
    }

    /**
     * 分页获取用户的资源列表
     */
    @Override
    public List<ResourceDTO> getResourcesByUserId(Integer userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Resource> resources = resourceRepository.findByUserId(userId, offset, size);
        return resources.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 分页获取资源列表
     */
    @Override
    public ResourceListResponse getAllResources(Integer page, Integer size, Integer tagId, String sort) {
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
                .toList();
        
        // 补充额外信息
        for (ResourceDTO dto : dtoList) {
            UserDTO user = userService.getUserDTOById(dto.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            }
            
            dto.setTags(tagService.getResourceTags(dto.getId()));
            dto.setFavoriteCount(favoriteRepository.findByResourceId(dto.getId()).size());
        }
        
        return new ResourceListResponse(dtoList, total);
    }

    /**
     * 上传资源
     */
    @Override
    @Transactional
    public ResourceDTO uploadResource(Integer userId, String title, String description, List<Integer> tagIds, MultipartFile file) {
        // 验证用户
        UserDTO user = userService.getUserDTOById(userId);
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
            
            // 使用新的方法按照类型存储资源文件
            String storedFilePath = fileStorageUtil.storeResourceFile(file, fileType);
            
            // 使用工具类获取文件的公共URL
            String fileUrl = fileStorageUtil.getPublicUrl(storedFilePath);
            
            // 默认封面为空
            String coverUrl = null;
            
            // 创建资源记录
            Resource resource = new Resource();
            resource.setUserId(userId);
            resource.setTitle(title);
            resource.setDescription(description);
            resource.setCoverUrl(coverUrl); // 设置为null或默认封面
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
     * 上传资源（带封面）
     * @param userId 用户ID
     * @param title 资源标题
     * @param description 资源描述
     * @param tagIds 标签ID列表
     * @param file 资源文件
     * @param coverFile 封面文件（可为null）
     * @return 资源DTO
     */
    @Override
    @Transactional
    public ResourceDTO uploadResourceWithCover(Integer userId, String title, String description, 
                                           List<Integer> tagIds, MultipartFile file, MultipartFile coverFile) {
        // 验证用户
        UserDTO user = userService.getUserDTOById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("资源文件不能为空");
        }
        
        try {
            // 保存资源文件
            String originalFilename = file.getOriginalFilename();
            String fileType = getFileType(originalFilename);
            
            String storedFilePath = fileStorageUtil.storeResourceFile(file, fileType);
            
            // 使用工具类获取文件的公共URL
            String fileUrl = fileStorageUtil.getPublicUrl(storedFilePath);
            
            // 处理封面文件（如果有）
            String coverUrl = null;
            if (coverFile != null && !coverFile.isEmpty()) {
                // 验证封面是否为图片
                String coverFileName = coverFile.getOriginalFilename();
                String coverFileType = getFileType(coverFileName);
                
                if (!"image".equals(coverFileType)) {
                    throw new BusinessException("封面文件必须是图片格式");
                }
                
                // 保存封面文件
                String storedCoverPath = fileStorageUtil.storeFile(coverFile, "covers");
                
                // 使用工具类获取封面的公共URL
                coverUrl = fileStorageUtil.getPublicUrl(storedCoverPath);
            }
            
            // 创建资源记录
            Resource resource = new Resource();
            resource.setUserId(userId);
            resource.setTitle(title);
            resource.setDescription(description);
            resource.setCoverUrl(coverUrl); // 可能为null
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
            
            // 如果有封面，也删除封面
            if (resource.getCoverUrl() != null && !resource.getCoverUrl().isEmpty()) {
                String coverName = resource.getCoverUrl().replace(fileDomain, "");
                fileStorageUtil.deleteFile(coverName);
            }
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileName, e);
            // 文件删除失败不影响数据库操作
        }
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
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 获取资源的文件URL
        String fileUrl = resource.getFileUrl();
        
        // 生成下载URL（简单实现，实际可能需要生成签名URL等）
        return fileUrl + "?download=true";
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
    public ResourceListResponse searchResources(String keyword, Integer page, Integer size) {
        int offset = (page - 1) * size;
        
        List<Resource> resources = resourceRepository.search(keyword, offset, size);
        Integer total = resourceRepository.countSearch(keyword);
        
        // 转换为DTO
        List<ResourceDTO> dtoList = resources.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 补充额外信息
        for (ResourceDTO dto : dtoList) {
            UserDTO user = userService.getUserDTOById(dto.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            }
            
            dto.setTags(tagService.getResourceTags(dto.getId()));
            dto.setFavoriteCount(favoriteRepository.findByResourceId(dto.getId()).size());
        }
        
        return new ResourceListResponse(dtoList, total);
    }
    
    /**
     * 根据ID列表批量查询资源
     * @param ids 资源ID列表
     * @return 资源列表
     */
    @Override
    public List<ResourceDTO> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<ResourceDTO> dtoList = new ArrayList<>();
        for (Integer id : ids) {
            Resource resource = resourceRepository.findById(id);
            if (resource != null) {
                ResourceDTO dto = convertToDTO(resource);
                
                // 补充用户信息
                UserDTO user = userService.getUserDTOById(resource.getUserId());
                if (user != null) {
                    dto.setUsername(user.getUsername());
                    dto.setUserAvatar(user.getAvatar());
                }
                
                // 补充标签信息
                dto.setTags(tagService.getResourceTags(resource.getId()));
                
                // 补充收藏数量
                dto.setFavoriteCount(favoriteRepository.findByResourceId(resource.getId()).size());
                
                dtoList.add(dto);
            }
        }
        
        return dtoList;
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

    /**
     * 获取资源总数
     * @return 资源总数
     */
    @Override
    public int countResources() {
        return resourceRepository.count();
    }

    /**
     * 点赞资源
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 操作结果描述
     */
    @Override
    @Transactional
    public String likeResource(Integer resourceId, Integer userId) {
        // 检查资源是否存在
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
            
        // 检查是否已点赞
        if (resourceLikeRepository.exists(resourceId, userId)) {
            throw new BusinessException("已经点赞过了");
        }
        
        // 保存点赞记录
        resourceLikeRepository.save(resourceId, userId);
        
        // 更新点赞数
        resourceRepository.incrementLikeCount(resourceId);
        
        return "点赞成功";
    }
    
    /**
     * 取消点赞资源
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 操作结果描述
     */
    @Override
    @Transactional
    public String unlikeResource(Integer resourceId, Integer userId) {
        // 检查资源是否存在
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
            
        // 检查是否已点赞
        if (!resourceLikeRepository.exists(resourceId, userId)) {
            throw new BusinessException("未点赞该资源");
        }
        
        // 删除点赞记录
        resourceLikeRepository.delete(resourceId, userId);
        
        // 更新点赞数
        resourceRepository.decrementLikeCount(resourceId);
        
        return "取消点赞成功";
    }
    
    /**
     * 检查用户是否已点赞资源
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    @Override
    public boolean isResourceLiked(Integer resourceId, Integer userId) {
        return resourceLikeRepository.exists(resourceId, userId);
    }
    
    /**
     * 获取用户点赞的资源列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 资源列表和总数
     */
    @Override
    public ResourceListResponse getLikedResources(Integer userId, Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取用户点赞的资源ID列表
        List<Integer> resourceIds = resourceLikeRepository.findResourceIdsByUserId(userId);
        
        // 如果没有点赞任何资源，直接返回空列表
        if (resourceIds.isEmpty()) {
            return new ResourceListResponse(new ArrayList<>(), 0);
        }
        
        // 分页查询资源
        List<Resource> resources = new ArrayList<>();
        int total = resourceIds.size();
        
        // 如果有点赞的资源，获取分页数据
        if (!resourceIds.isEmpty()) {
            // 注：这里需要增加根据ID列表查询资源的方法，暂时使用已有方法模拟
            resources = resourceIds.stream()
                    .skip(offset)
                    .limit(size)
                    .map(resourceRepository::findById)
                    .filter(r -> r != null)
                    .toList();
        }
        
        // 转换为DTO
        List<ResourceDTO> dtoList = resources.stream()
                .map(this::convertToDTO)
                .toList();
        
        // 补充额外信息
        for (ResourceDTO dto : dtoList) {
            UserDTO user = userService.getUserDTOById(dto.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            }
            
            dto.setTags(tagService.getResourceTags(dto.getId()));
            dto.setFavoriteCount(favoriteRepository.findByResourceId(dto.getId()).size());
            dto.setIsLiked(true); // 这里肯定是已点赞的
        }
        
        return new ResourceListResponse(dtoList, total);
    }

    /**
     * 使用已上传的文件路径创建资源
     * @param userId 用户ID
     * @param title 资源标题
     * @param description 资源描述
     * @param filePath 已上传的文件路径
     * @param coverPath 已上传的封面图路径（可选）
     * @param tagIds 标签ID列表
     * @return 创建的资源DTO
     */
    @Override
    @Transactional
    public ResourceDTO createResource(Integer userId, String title, String description, 
                                     String filePath, String coverPath, List<Integer> tagIds) {
        // 验证用户
        UserDTO user = userService.getUserDTOById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证路径
        if (filePath == null || filePath.isEmpty()) {
            throw new BusinessException("文件路径不能为空");
        }
        
        try {
            // 获取文件类型
            String fileType = getFileTypeFromPath(filePath);
            // 获取文件大小（这里使用估计值，因为文件已上传）
            int fileSize = 1024; // 默认1MB
            
            // 使用工具类获取文件的公共URL
            String fileUrl = fileStorageUtil.getPublicUrl(filePath);
            
            // 处理封面URL（如果有）
            String coverUrl = null;
            if (coverPath != null && !coverPath.isEmpty()) {
                coverUrl = fileStorageUtil.getPublicUrl(coverPath);
            }
            
            // 创建资源记录
            Resource resource = new Resource();
            resource.setUserId(userId);
            resource.setTitle(title);
            resource.setDescription(description);
            resource.setFileUrl(fileUrl);
            resource.setFileType(fileType);
            resource.setFileSize(fileSize); // 设置为估计值
            resource.setUploadTime(LocalDateTime.now());
            resource.setDownloadCount(0);
            resource.setLikeCount(0);
            
            // 设置封面URL（如果有）
            if (coverUrl != null) {
                resource.setCoverUrl(coverUrl);
            }
            
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
            
        } catch (Exception e) {
            log.error("创建资源失败", e);
            throw new BusinessException("创建资源失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据文件路径获取文件类型
     * @param filePath 文件路径
     * @return 文件类型
     */
    private String getFileTypeFromPath(String filePath) {
        // 检查文件路径是否包含类型信息
        if (filePath.contains("resources/")) {
            String[] parts = filePath.split("/");
            if (parts.length >= 2) {
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("resources".equals(parts[i]) && i + 1 < parts.length) {
                        return parts[i + 1];
                    }
                }
            }
        }
        
        // 如果路径中没有类型信息，尝试从文件名判断
        return getFileType(filePath);
    }
} 