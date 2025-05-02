package com.animesocial.platform.controller;

import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.CreateEventRequest;
import com.animesocial.platform.model.dto.EventDTO;
import com.animesocial.platform.model.dto.PostDTO;
import com.animesocial.platform.model.dto.PostListResponse;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.dto.ResourceListResponse;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.service.EventService;
import com.animesocial.platform.service.PostService;
import com.animesocial.platform.service.ResourceService;
import com.animesocial.platform.service.UserService;
import com.animesocial.platform.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 管理员控制器
 * 处理与管理员相关的所有HTTP请求，包括：
 * 1. 用户管理
 * 2. 内容管理(帖子/资源)
 * 3. 活动管理
 * 4. 标签管理
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private TagService tagService;
    
    /**
     * 检查管理员权限
     * @param session HTTP会话
     * @return 是否有管理员权限
     */
    private boolean checkAdminPermission(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return isAdmin != null && isAdmin;
    }
    
    /**
     * 获取所有用户列表
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 用户列表
     */
    @GetMapping("/users")
    public ApiResponse<List<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success(userService.getAllUsers(page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取所有管理员列表
     * @param session HTTP会话
     * @return 管理员列表
     */
    @GetMapping("/admins")
    public ApiResponse<List<UserDTO>> getAllAdmins(HttpSession session) {
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success(userService.getAllAdmins());
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 匹配的用户列表
     */
    @GetMapping("/users/search")
    public ApiResponse<List<UserDTO>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success(userService.searchUsers(keyword, page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 搜索帖子
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 匹配的帖子列表
     */
    @GetMapping("/posts/search")
    public ApiResponse<PostListResponse> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success(postService.searchPosts(keyword, page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 搜索资源
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 匹配的资源列表
     */
    @GetMapping("/resources/search")
    public ApiResponse<ResourceListResponse> searchResources(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success(resourceService.searchResources(keyword, page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员禁用/启用用户
     * @param id 用户ID
     * @param status 状态(0禁用,1正常)
     * @param session HTTP会话
     * @return 操作结果
     */
    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        if (status != 0 && status != 1) {
            return ApiResponse.validateFailed("状态值必须是0(禁用)或1(正常)");
        }
        
        try {
            userService.updateUserStatus(id, status);
            return ApiResponse.success(status == 1 ? "用户已启用" : "用户已禁用", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员删除帖子
     * @param id 帖子ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Integer id, HttpSession session) {
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            postService.deletePost(id);
            return ApiResponse.success("帖子删除成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员置顶/取消置顶帖子
     * @param id 帖子ID
     * @param isTop 是否置顶(1置顶,0取消置顶)
     * @param session HTTP会话
     * @return 操作结果
     */
    @PutMapping("/posts/{id}/top")
    public ApiResponse<Void> topPost(
            @PathVariable Integer id,
            @RequestParam Integer isTop,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        if (isTop != 0 && isTop != 1) {
            return ApiResponse.validateFailed("置顶值必须是0(取消置顶)或1(置顶)");
        }
        
        try {
            postService.setPostTop(id, isTop);
            return ApiResponse.success(isTop == 1 ? "帖子已置顶" : "已取消置顶", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员删除资源
     * @param id 资源ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/resources/{id}")
    public ApiResponse<Void> deleteResource(@PathVariable Integer id, HttpSession session) {
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            resourceService.deleteResource(id);
            return ApiResponse.success("资源删除成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员创建活动
     * @param eventRequest 活动信息
     * @param session HTTP会话
     * @return 创建的活动
     */
    @PostMapping("/events")
    public ApiResponse<EventDTO> createEvent(@RequestBody CreateEventRequest eventRequest, HttpSession session) {
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        // 获取当前用户ID
        Integer currentUserId = (Integer) session.getAttribute("userId");
        
        try {
            return ApiResponse.success("活动创建成功", eventService.createEvent(currentUserId, eventRequest));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员更新活动
     * @param id 活动ID
     * @param eventRequest 活动信息
     * @param session HTTP会话
     * @return 更新的活动
     */
    @PutMapping("/events/{id}")
    public ApiResponse<EventDTO> updateEvent(
            @PathVariable Integer id,
            @RequestBody CreateEventRequest eventRequest,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success("活动更新成功", eventService.updateEvent(id, eventRequest));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员删除活动
     * @param id 活动ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/events/{id}")
    public ApiResponse<Void> deleteEvent(@PathVariable Integer id, HttpSession session) {
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            eventService.deleteEvent(id);
            return ApiResponse.success("活动删除成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 管理员更新活动状态
     * @param id 活动ID
     * @param status 状态(0下线,1上线)
     * @param session HTTP会话
     * @return 操作结果
     */
    @PutMapping("/events/{id}/status")
    public ApiResponse<Void> updateEventStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        if (status != 0 && status != 1) {
            return ApiResponse.validateFailed("状态值必须是0(下线)或1(上线)");
        }
        
        try {
            eventService.updateEventStatus(id, status);
            return ApiResponse.success(status == 1 ? "活动已上线" : "活动已下线", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取系统统计信息
     * 包括用户总数、帖子总数、资源总数、活动总数等
     * @param session HTTP会话
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getSystemStatistics(HttpSession session) {
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            // 构建统计信息
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取各种计数
            int userCount = userService.countUsers();
            
            // 帖子数量
            int postCount = postService.countPosts();
            
            // 资源数量
            int resourceCount = resourceService.countResources();
            
            // 活动数量
            int eventCount = eventService.countEvents(null);
            
            // 标签数量
            int tagCount = tagService.countTags(null);
            
            // 添加到统计Map
            statistics.put("userCount", userCount);
            statistics.put("postCount", postCount);
            statistics.put("resourceCount", resourceCount);
            statistics.put("eventCount", eventCount);
            statistics.put("tagCount", tagCount);
            
            // 获取最新用户
            List<UserDTO> latestUsers = userService.getAllUsers(1, 5);
            statistics.put("latestUsers", latestUsers);
            
            // 获取置顶帖子
            List<PostDTO> topPosts = postService.getTopPosts();
            statistics.put("topPosts", topPosts);
            
            // 获取当前活动
            List<EventDTO> currentEvents = eventService.getCurrentEvents();
            statistics.put("currentEvents", currentEvents);
            
            // 获取热门标签
            List<TagDTO> hotTags = tagService.getHotPostTags(5);
            statistics.put("hotTags", hotTags);
            
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取所有帖子列表
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 帖子列表
     */
    @GetMapping("/posts")
    public ApiResponse<PostListResponse> getAllPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer tagId,
            @RequestParam(defaultValue = "latest") String sort,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success(postService.getAllPosts(page, size, tagId, sort));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取所有资源列表
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 资源列表
     */
    @GetMapping("/resources")
    public ApiResponse<ResourceListResponse> getAllResources(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer tagId,
            @RequestParam(defaultValue = "latest") String sort,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success(resourceService.getAllResources(page, size, tagId, sort));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取所有活动列表
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 活动列表
     */
    @GetMapping("/events")
    public ApiResponse<List<EventDTO>> getAllEvents(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endTime,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            return ApiResponse.success(eventService.getAllEvents(status, startTime, endTime));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取所有标签列表
     * @param type 标签类型(post/resource/user，可选)
     * @param session HTTP会话
     * @return 标签列表
     */
    @GetMapping("/tags")
    public ApiResponse<List<TagDTO>> getAllTags(
            @RequestParam(required = false) String type,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            if (type != null && !type.isEmpty()) {
                return ApiResponse.success(tagService.getTagsByType(type));
            } else {
                return ApiResponse.success(tagService.getAllTags());
            }
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取标签详情
     * @param id 标签ID
     * @param session HTTP会话
     * @return 标签详情
     */
    @GetMapping("/tags/{id}")
    public ApiResponse<TagDTO> getTagById(
            @PathVariable Integer id,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            TagDTO tag = tagService.getTagById(id);
            if (tag == null) {
                return ApiResponse.failed("标签不存在");
            }
            return ApiResponse.success(tag);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 创建标签
     * @param tagDTO 标签信息
     * @param session HTTP会话
     * @return 创建的标签
     */
    @PostMapping("/tags")
    public ApiResponse<TagDTO> createTag(
            @RequestBody TagDTO tagDTO,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            // 验证标签类型
            if (tagDTO.getType() == null || 
                (!tagDTO.getType().equals("post") && 
                 !tagDTO.getType().equals("resource"))) {
                return ApiResponse.validateFailed("标签类型必须是post或resource");
            }
            
            // 验证标签名称
            if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
                return ApiResponse.validateFailed("标签名称不能为空");
            }
            
            // 检查标签名称是否已存在
            TagDTO existingTag = tagService.getTagByName(tagDTO.getName());
            if (existingTag != null) {
                return ApiResponse.validateFailed("标签名称已存在");
            }
            
            TagDTO newTag = tagService.createTag(tagDTO);
            return ApiResponse.success("标签创建成功", newTag);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 更新标签
     * @param id 标签ID
     * @param tagDTO 标签信息
     * @param session HTTP会话
     * @return 更新后的标签
     */
    @PutMapping("/tags/{id}")
    public ApiResponse<TagDTO> updateTag(
            @PathVariable Integer id,
            @RequestBody TagDTO tagDTO,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            // 验证标签是否存在
            TagDTO existingTag = tagService.getTagById(id);
            if (existingTag == null) {
                return ApiResponse.failed("标签不存在");
            }
            
            // 验证标签名称
            if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
                return ApiResponse.validateFailed("标签名称不能为空");
            }
            
            // 创建新的Tag实体并设置ID
            Tag tagEntity = tagService.convertToEntity(tagDTO);
            tagEntity.setId(id);
            
            // 更新标签
            TagDTO updatedTag = tagService.updateTag(id, tagEntity);
            return ApiResponse.success("标签更新成功", updatedTag);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 删除标签
     * @param id 标签ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(
            @PathVariable Integer id,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            // 验证标签是否存在
            TagDTO existingTag = tagService.getTagById(id);
            if (existingTag == null) {
                return ApiResponse.failed("标签不存在");
            }
            
            // 删除标签
            tagService.deleteTag(id);
            return ApiResponse.success("标签删除成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取标签统计信息
     * @param session HTTP会话
     * @return 标签统计信息
     */
    @GetMapping("/tags/statistics")
    public ApiResponse<Map<String, Object>> getTagStatistics(HttpSession session) {
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取各类型标签数量
            int totalTagCount = tagService.countTags(null);
            int postTagCount = tagService.countTags("post");
            int resourceTagCount = tagService.countTags("resource");
            int userTagCount = tagService.countTags("user");
            
            // 获取热门标签
            List<TagDTO> hotPostTags = tagService.getHotPostTags(5);
            List<TagDTO> hotResourceTags = tagService.getHotResourceTags(5);
            
            // 添加到统计Map
            statistics.put("totalTagCount", totalTagCount);
            statistics.put("postTagCount", postTagCount);
            statistics.put("resourceTagCount", resourceTagCount);
            statistics.put("userTagCount", userTagCount);
            statistics.put("hotPostTags", hotPostTags);
            statistics.put("hotResourceTags", hotResourceTags);

            
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 搜索标签
     * @param keyword 搜索关键词
     * @param type 标签类型(post/resource/user，可选)
     * @param session HTTP会话
     * @return 匹配的标签列表
     */
    @GetMapping("/tags/search")
    public ApiResponse<List<TagDTO>> searchTags(
            @RequestParam String keyword,
            @RequestParam(required = false) String type,
            HttpSession session) {
        
        if (!checkAdminPermission(session)) {
            return ApiResponse.forbidden();
        }
        
        try {
            // 由于接口中没有直接的搜索功能，所以我们获取所有标签然后在内存中过滤
            List<TagDTO> allTags;
            if (type != null && !type.isEmpty()) {
                allTags = tagService.getTagsByType(type);
            } else {
                allTags = tagService.getAllTags();
            }
            
            // 过滤匹配的标签
            List<TagDTO> matchedTags = allTags.stream()
                .filter(tag -> {
                    boolean nameMatch = tag.getName() != null && 
                                       tag.getName().toLowerCase().contains(keyword.toLowerCase());
                    boolean categoryMatch = tag.getCategory() != null && 
                                           tag.getCategory().toLowerCase().contains(keyword.toLowerCase());
                    return nameMatch || categoryMatch;
                })
                .toList();
            
            return ApiResponse.success(matchedTags);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
} 