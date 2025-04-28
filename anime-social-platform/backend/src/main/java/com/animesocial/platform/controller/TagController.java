package com.animesocial.platform.controller;

import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.service.TagService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final TagService tagService;
    
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
} 