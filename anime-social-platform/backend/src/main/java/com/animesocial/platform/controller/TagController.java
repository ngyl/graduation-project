package com.animesocial.platform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.service.TagService;
import com.animesocial.platform.service.UserTagService;

import jakarta.servlet.http.HttpSession;
import lombok.Data;

/**
 * 标签控制器
 * 处理与标签相关的所有HTTP请求，包括：
 * 1. 获取标签列表
 * 2. 按类型获取标签
 * 3. 管理用户标签
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {
    @Autowired
    private final TagService tagService;

    @Autowired
    private UserTagService userTagService;
    
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }
    
    /**
     * 获取所有标签
     * @return 标签列表
     */
    @GetMapping
    public ApiResponse<List<TagDTO>> getAllTags() {
        return ApiResponse.success(tagService.getAllTags());
    }
    
    /**
     * 根据类型获取标签
     * @param type 标签类型(post/resource/user)
     * @return 特定类型的标签列表
     */
    @GetMapping("/type/{type}")
    public ApiResponse<List<TagDTO>> getTagsByType(@PathVariable String type) {
        if (!type.matches("post|resource|user")) {
            return ApiResponse.validateFailed("标签类型必须是 post、resource 或 user");
        }
        return ApiResponse.success(tagService.getTagsByType(type));
    }
    
    
    /**
     * 管理员创建新标签
     * @param tagDTO 标签数据
     * @param session HTTP会话
     * @return 创建的标签
     */
    @PostMapping
    public ApiResponse<TagDTO> createTag(@RequestBody TagDTO tagDTO, HttpSession session) {
        // 检查管理员权限
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success("标签创建成功", tagService.createTag(tagDTO));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员删除标签
     * @param id 标签ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Integer id, HttpSession session) {
        // 检查管理员权限
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ApiResponse.forbidden();
        }
        
        try {
            tagService.deleteTag(id);
            return ApiResponse.success("标签删除成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 更新用户标签
     * @param userId 用户ID
     * @param tagIds 标签ID列表
     * @param session HTTP会话
     * @return 操作结果
     */
    @PutMapping("/user/{userId}")
    public ApiResponse<Void> updateUserTags(@PathVariable Integer userId, @RequestBody UpdateUserTagsRequest request, HttpSession session) {
        // 检查用户权限
        Integer user = (Integer) session.getAttribute("userId");
        if (user == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            // 分别更新不同类型的标签，而不是完全覆盖
            // 为postTagIds创建一个单独的更新方法调用
            if (request.getPostTagIds() != null) {
                // 这里应该增加对标签类型的验证，确保只更新post类型的标签
                userTagService.updateUserTagsByType(userId, request.getPostTagIds(), "post");
            }
            
            // 为resourceTagIds创建一个单独的更新方法调用
            if (request.getResourceTagIds() != null) {
                // 这里应该增加对标签类型的验证，确保只更新resource类型的标签
                userTagService.updateUserTagsByType(userId, request.getResourceTagIds(), "resource");
            }
            
            return ApiResponse.success("用户标签更新成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    @Data
    public static class UpdateUserTagsRequest {
        private List<Integer> postTagIds;
        private List<Integer> resourceTagIds;
    }
} 