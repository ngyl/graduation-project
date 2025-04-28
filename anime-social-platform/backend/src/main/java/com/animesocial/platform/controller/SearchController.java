package com.animesocial.platform.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.PostDTO;
import com.animesocial.platform.model.dto.ResourceDTO;
import com.animesocial.platform.model.dto.SearchResult;
import com.animesocial.platform.model.dto.SearchResultItem;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.es.PostDocument;
import com.animesocial.platform.model.es.ResourceDocument;
import com.animesocial.platform.model.es.UserDocument;
import com.animesocial.platform.service.ElasticsearchService;
import com.animesocial.platform.service.PostService;
import com.animesocial.platform.service.ResourceService;
import com.animesocial.platform.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * 搜索控制器
 * 处理全文搜索相关操作
 */
@RestController
@RequestMapping("/api/search")
@Slf4j
public class SearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 搜索所有类型的内容
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping
    public ApiResponse<SearchResult> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            log.info("执行全文搜索: keyword={}, page={}, size={}", keyword, page, size);
            
            // 分别搜索不同类型的内容
            Page<PostDocument> postDocuments = elasticsearchService.searchPosts(keyword, page, size);
            Page<ResourceDocument> resourceDocuments = elasticsearchService.searchResources(keyword, page, size);
            Page<UserDocument> userDocuments = elasticsearchService.searchUsers(keyword, page, size);
            
            // 构建搜索结果
            SearchResult result = new SearchResult();
            
            // 转换帖子结果
            List<SearchResultItem> postResults = new ArrayList<>();
            for (PostDocument doc : postDocuments.getContent()) {
                SearchResultItem item = new SearchResultItem();
                item.setId(doc.getId());
                item.setType("post");
                item.setTitle(doc.getTitle());
                item.setContent(truncateContent(doc.getContent(), 200));
                item.setUrl("/posts/" + doc.getId());
                item.setAuthor(doc.getUsername());
                item.setTags(doc.getTags());
                item.setCreatedAt(doc.getCreatedAt());
                postResults.add(item);
            }
            result.setPosts(postResults);
            result.setTotalPosts((int) postDocuments.getTotalElements());
            
            // 转换资源结果
            List<SearchResultItem> resourceResults = new ArrayList<>();
            for (ResourceDocument doc : resourceDocuments.getContent()) {
                SearchResultItem item = new SearchResultItem();
                item.setId(doc.getId());
                item.setType("resource");
                item.setTitle(doc.getTitle());
                item.setContent(truncateContent(doc.getDescription(), 200));
                item.setUrl("/resources/" + doc.getId());
                item.setAuthor(doc.getUsername());
                item.setTags(doc.getTags());
                item.setCreatedAt(doc.getUploadTime());
                resourceResults.add(item);
            }
            result.setResources(resourceResults);
            result.setTotalResources((int) resourceDocuments.getTotalElements());
            
            // 转换用户结果
            List<SearchResultItem> userResults = new ArrayList<>();
            for (UserDocument doc : userDocuments.getContent()) {
                SearchResultItem item = new SearchResultItem();
                item.setId(doc.getId());
                item.setType("user");
                item.setTitle(doc.getUsername());
                item.setContent(truncateContent(doc.getBio(), 200));
                item.setUrl("/profile/" + doc.getId());
                item.setAuthor(doc.getUsername());
                item.setTags(doc.getTags());
                item.setCreatedAt(doc.getRegisterTime());
                userResults.add(item);
            }
            result.setUsers(userResults);
            result.setTotalUsers((int) userDocuments.getTotalElements());
            
            // 统计总数
            result.setTotalResults(result.getTotalPosts() + result.getTotalResources() + result.getTotalUsers());
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("搜索失败", e);
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 只搜索帖子
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/posts")
    public ApiResponse<Map<String, Object>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            log.info("搜索帖子: keyword={}, page={}, size={}", keyword, page, size);
            
            Page<PostDocument> postDocuments = elasticsearchService.searchPosts(keyword, page, size);
            List<Integer> postIds = new ArrayList<>();
            for (PostDocument doc : postDocuments.getContent()) {
                postIds.add(doc.getId());
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", postDocuments.getTotalElements());
            result.put("data", postService.findByIds(postIds));
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("搜索帖子失败", e);
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 只搜索资源
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/resources")
    public ApiResponse<Map<String, Object>> searchResources(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            log.info("搜索资源: keyword={}, page={}, size={}", keyword, page, size);
            
            Page<ResourceDocument> resourceDocuments = elasticsearchService.searchResources(keyword, page, size);
            List<Integer> resourceIds = new ArrayList<>();
            for (ResourceDocument doc : resourceDocuments.getContent()) {
                resourceIds.add(doc.getId());
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", resourceDocuments.getTotalElements());
            result.put("data", resourceService.findByIds(resourceIds));
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("搜索资源失败", e);
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 只搜索用户
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            log.info("搜索用户: keyword={}, page={}, size={}", keyword, page, size);
            
            Page<UserDocument> userDocuments = elasticsearchService.searchUsers(keyword, page, size);
            List<Integer> userIds = new ArrayList<>();
            for (UserDocument doc : userDocuments.getContent()) {
                userIds.add(doc.getId());
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", userDocuments.getTotalElements());
            result.put("data", userService.findByIds(userIds));
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 重建索引（仅限管理员）
     * @return 操作结果
     */
    @GetMapping("/rebuild-indices")
    public ApiResponse<String> rebuildIndices() {
        try {
            log.info("开始重建索引");
            elasticsearchService.rebuildIndices();
            return ApiResponse.success("索引重建成功");
        } catch (Exception e) {
            log.error("索引重建失败", e);
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 截断内容，保留指定长度
     * @param content 原始内容
     * @param maxLength 最大长度
     * @return 截断后的内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        
        if (content.length() <= maxLength) {
            return content;
        }
        
        return content.substring(0, maxLength) + "...";
    }
} 