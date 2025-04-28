package com.animesocial.platform.controller;

import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.ResourceDTO;
import com.animesocial.platform.model.dto.ResourceListResponse;
import com.animesocial.platform.service.ResourceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * 资源控制器
 * 处理与资源相关的所有HTTP请求，包括：
 * 1. 资源上传和下载
 * 2. 资源管理(收藏、点赞等)
 * 3. 资源查询和筛选
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    /**
     * 获取指定ID的资源详情
     * @param id 资源ID
     * @return 资源详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ResourceDTO> getResource(@PathVariable Integer id) {
        try {
            return ApiResponse.success(resourceService.getResourceById(id));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 分页获取所有资源
     * @param page 页码(默认1)
     * @param size 每页数量(默认10)
     * @param tagId 标签ID（可选）
     * @param sort 排序方式（可选，支持：latest/downloads/likes）
     * @return 资源列表和总数
     */
    @GetMapping
    public ApiResponse<ResourceListResponse> getAllResources(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer tagId,
            @RequestParam(defaultValue = "latest") String sort) {
        try {
            return ApiResponse.success(resourceService.getAllResources(page, size, tagId, sort));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取指定用户的所有资源
     * @param page 页码
     * @param size 每页数量
     * @return 资源列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<ResourceDTO>> getUserResources(@PathVariable Integer userId,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        try {
            return ApiResponse.success(resourceService.getResourcesByUserId(userId, page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 上传新资源
     * @param title 资源标题
     * @param description 资源描述
     * @param tagIds 标签ID列表
     * @param file 资源文件
     * @param session HTTP会话
     * @return 新创建的资源对象
     */
    @PostMapping
    public ApiResponse<ResourceDTO> uploadResource(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<Integer> tagIds,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) MultipartFile cover,
            HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        ResourceDTO resource = null;
        try {
            if (cover != null && !cover.isEmpty()) {
               resource = resourceService.uploadResourceWithCover(userId, title, description, tagIds, file, cover);
            } else {
                resource = resourceService.uploadResource(userId, title, description, tagIds, file);
            }
            return ApiResponse.success("资源上传成功", resource);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 更新指定ID的资源
     * @param id 资源ID
     * @param title 资源标题
     * @param description 资源描述
     * @param tagIds 标签ID列表
     * @param session HTTP会话
     * @return 更新后的资源对象
     */
    @PutMapping("/{id}")
    public ApiResponse<ResourceDTO> updateResource(
            @PathVariable Integer id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<Integer> tagIds,
            HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            // 验证当前用户是否是资源的上传者或管理员
            ResourceDTO resource = resourceService.getResourceById(id);
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (!resource.getUserId().equals(userId) && (isAdmin == null || !isAdmin)) {
                return ApiResponse.forbidden();
            }
            
            ResourceDTO updatedResource = resourceService.updateResource(id, title, description, tagIds);
            return ApiResponse.success("资源更新成功", updatedResource);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 删除指定ID的资源
     * @param id 资源ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResource(@PathVariable Integer id, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            // 验证当前用户是否是资源的上传者或管理员
            ResourceDTO resource = resourceService.getResourceById(id);
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (!resource.getUserId().equals(userId) && (isAdmin == null || !isAdmin)) {
                return ApiResponse.forbidden();
            }
            
            resourceService.deleteResource(id);
            return ApiResponse.success("资源已删除", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 记录资源下载
     * @param id 资源ID
     * @param session HTTP会话
     * @return 资源下载URL
     */
    @GetMapping("/{id}/download")
    public ApiResponse<String> downloadResource(@PathVariable Integer id, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            String downloadUrl = resourceService.getDownloadUrl(id, userId);
            resourceService.incrementDownloadCount(id);
            return ApiResponse.success(downloadUrl);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 点赞资源
     * @param id 资源ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @PostMapping("/{id}/like")
    public ApiResponse<Void> likeResource(@PathVariable Integer id, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            resourceService.likeResource(id, userId);
            return ApiResponse.success("点赞成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 取消点赞资源
     * @param id 资源ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/{id}/like")
    public ApiResponse<Void> unlikeResource(@PathVariable Integer id, HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            resourceService.unlikeResource(id, userId);
            return ApiResponse.success("取消点赞成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取用户点赞的资源列表
     * @param page 页码(默认1)
     * @param size 每页数量(默认10)
     * @param session HTTP会话
     * @return 资源列表和总数
     */
    @GetMapping("/liked")
    public ApiResponse<ResourceListResponse> getLikedResources(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            return ApiResponse.success(resourceService.getLikedResources(userId, page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 使用已上传的文件创建资源
     * @param title 资源标题
     * @param description 资源描述
     * @param filePath 已上传的文件路径
     * @param coverPath 已上传的封面图路径（可选）
     * @param tagIds 标签ID列表
     * @param session HTTP会话
     * @return 新创建的资源对象
     */
    @PostMapping("/create")
    public ApiResponse<ResourceDTO> createResource(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String filePath,
            @RequestParam(required = false) String coverPath,
            @RequestParam(required = false) List<Integer> tagIds,
            HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            ResourceDTO resource = resourceService.createResource(userId, title, description, filePath, coverPath, tagIds);
            return ApiResponse.success("资源创建成功", resource);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 上传资源（带封面）
     */
    @PostMapping("/upload-with-cover")
    public ResponseEntity<ApiResponse<ResourceDTO>> uploadResourceWithCover(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "tagIds", required = false) List<Integer> tagIds,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            HttpSession session) {
        
        // 获取当前登录用户
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.unauthorized());
        }
        
        // 上传带封面的资源
        try {
            ResourceDTO resource = resourceService.uploadResourceWithCover(userId, title, description, tagIds, file, cover);
            return ResponseEntity.ok(ApiResponse.success("上传成功", resource));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.failed(e.getMessage()));
        }
    }
} 