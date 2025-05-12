package com.animesocial.platform.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.animesocial.platform.model.Post;
import com.animesocial.platform.model.Resource;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.Tag;
import com.animesocial.platform.model.dto.TagDTO;
import com.animesocial.platform.model.es.PostDocument;
import com.animesocial.platform.model.es.ResourceDocument;
import com.animesocial.platform.model.es.UserDocument;
import com.animesocial.platform.repository.PostRepository;
import com.animesocial.platform.repository.ResourceRepository;
import com.animesocial.platform.repository.UserTagRepository;
import com.animesocial.platform.repository.es.PostDocumentRepository;
import com.animesocial.platform.repository.es.ResourceDocumentRepository;
import com.animesocial.platform.repository.es.UserDocumentRepository;
import com.animesocial.platform.service.ElasticsearchService;
import com.animesocial.platform.service.TagService;
import com.animesocial.platform.service.UserService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * ElasticSearch服务实现类
 * 负责处理ElasticSearch的索引、搜索等操作
 */
@Service
@Slf4j
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Autowired
    private PostDocumentRepository postDocumentRepository;

    @Autowired
    private ResourceDocumentRepository resourceDocumentRepository;

    @Autowired
    private UserDocumentRepository userDocumentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserTagRepository userTagRepository;

    /**
     * 应用启动时初始化索引
     */
    @PostConstruct
    public void init() {
        try {
            log.info("正在初始化ElasticSearch索引...");
            initIndices();
            log.info("ElasticSearch索引初始化完成");
        } catch (Exception e) {
            log.error("初始化ElasticSearch索引失败", e);
        }
    }

    /**
     * 初始化所有索引
     * 创建索引映射并同步MySQL数据到ES
     */
    @Override
    public void initIndices() {
        try {
            log.info("创建ElasticSearch索引结构");
            // 默认情况下，Spring Data Elasticsearch会自动创建索引
            
            log.info("开始从MySQL迁移数据到ElasticSearch...");
            // 同步所有数据
            syncAllData();
            log.info("ElasticSearch索引数据同步完成");
        } catch (Exception e) {
            log.error("ElasticSearch索引初始化失败", e);
            throw new RuntimeException("ElasticSearch索引初始化失败", e);
        }
    }

    /**
     * 重建所有索引
     * 清空当前索引并重新同步数据
     */
    @Override
    public void rebuildIndices() {
        log.info("开始重建ElasticSearch索引");
        
        try {
            // 清空现有索引
            clearAllIndices();
            
            // 同步所有数据
            syncAllData();
            
            log.info("ElasticSearch索引重建完成");
        } catch (Exception e) {
            log.error("ElasticSearch索引重建失败", e);
            throw new RuntimeException("ElasticSearch索引重建失败", e);
        }
    }
    
    /**
     * 清空所有索引数据
     */
    private void clearAllIndices() {
        log.info("清空所有ElasticSearch索引数据");
        postDocumentRepository.deleteAll();
        resourceDocumentRepository.deleteAll();
        userDocumentRepository.deleteAll();
    }
    
    /**
     * 同步所有MySQL数据到ElasticSearch
     */
    private void syncAllData() {
        log.info("开始同步所有MySQL数据到ElasticSearch");
        
        // 同步帖子数据
        List<Post> allPosts = postRepository.findAllPosts(0, Integer.MAX_VALUE);
        log.info("开始同步{}条帖子数据", allPosts.size());
        for (Post post : allPosts) {
            syncPostToEs(post.getId());
        }
        
        // 同步资源数据 - 修复: 提供默认排序字段
        List<Resource> allResources = resourceRepository.findAll("upload_time DESC", 0, Integer.MAX_VALUE);
        log.info("开始同步{}条资源数据", allResources.size());
        for (Resource resource : allResources) {
            syncResourceToEs(resource.getId());
        }
        
        // 同步用户数据
        List<UserDTO> allUsers = userService.getAllUsers(1, Integer.MAX_VALUE);
        log.info("开始同步{}条用户数据", allUsers.size());
        for (UserDTO user : allUsers) {
            syncUserToEs(user.getId());
        }
        
        log.info("所有数据同步完成");
    }

    /**
     * 同步单个帖子到ES
     */
    @Override
    public void syncPostToEs(Integer postId) {
        try {
            Post post = postRepository.findById(postId);
            if (post == null) {
                log.warn("帖子不存在，无法同步到ElasticSearch: {}", postId);
                return;
            }

            UserDTO author = userService.getUserDTOById(post.getUserId());
            String username = author != null ? author.getUsername() : "未知用户";

            // 获取帖子标签
            List<TagDTO> tagEntities = tagService.getPostTags(postId);
            List<String> tags = tagEntities.stream()
                    .map(TagDTO::getName)
                    .collect(Collectors.toList());

            // 创建文档
            PostDocument document = PostDocument.builder()
                    .id(post.getId())
                    .userId(post.getUserId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .isTop(post.getIsTop())
                    .username(username)
                    .tags(tags)
                    .build();

            // 保存到ES
            postDocumentRepository.save(document);
            log.info("帖子同步到ElasticSearch成功: {}", postId);
        } catch (Exception e) {
            log.error("帖子同步到ElasticSearch失败: {}", postId, e);
        }
    }

    /**
     * 同步单个资源到ES
     */
    @Override
    public void syncResourceToEs(Integer resourceId) {
        try {
            Resource resource = resourceRepository.findById(resourceId);
            if (resource == null) {
                log.warn("资源不存在，无法同步到ElasticSearch: {}", resourceId);
                return;
            }

            UserDTO author = userService.getUserDTOById(resource.getUserId());
            String username = author != null ? author.getUsername() : "未知用户";

            // 获取资源标签
            List<TagDTO> tagEntities = tagService.getResourceTags(resourceId);
            List<String> tags = tagEntities.stream()
                    .map(TagDTO::getName)
                    .collect(Collectors.toList());

            // 创建文档
            ResourceDocument document = ResourceDocument.builder()
                    .id(resource.getId())
                    .userId(resource.getUserId())
                    .title(resource.getTitle())
                    .description(resource.getDescription())
                    .fileUrl(resource.getFileUrl())
                    .coverUrl(resource.getCoverUrl())
                    .fileType(resource.getFileType())
                    .fileSize(resource.getFileSize())
                    .uploadTime(resource.getUploadTime())
                    .downloadCount(resource.getDownloadCount())
                    .likeCount(resource.getLikeCount())
                    .username(username)
                    .tags(tags)
                    .build();

            // 保存到ES
            resourceDocumentRepository.save(document);
            log.info("资源同步到ElasticSearch成功: {}", resourceId);
        } catch (Exception e) {
            log.error("资源同步到ElasticSearch失败: {}", resourceId, e);
        }
    }

    /**
     * 同步单个用户到ES
     */
    @Override
    public void syncUserToEs(Integer userId) {
        try {
            UserDTO user = userService.getUserDTOById(userId);
            if (user == null) {
                log.warn("用户不存在，无法同步到ElasticSearch: {}", userId);
                return;
            }

            // 获取用户标签
            List<Tag> tagEntities = userTagRepository.findTagsByUserId(userId);
            List<String> tags = tagEntities.stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList());

            // 创建文档
            UserDocument document = UserDocument.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .bio(user.getBio())
                    .avatar(user.getAvatar())
                    .isAdmin(user.getIsAdmin())
                    .status(user.getStatus())
                    .registerTime(user.getRegisterTime())
                    .lastLoginTime(user.getLastLoginTime())
                    .tags(tags)
                    .build();

            // 保存到ES
            userDocumentRepository.save(document);
            log.info("用户同步到ElasticSearch成功: {}", userId);
        } catch (Exception e) {
            log.error("用户同步到ElasticSearch失败: {}", userId, e);
        }
    }

    /**
     * 从ES中删除帖子
     */
    @Override
    public void deletePostFromEs(Integer postId) {
        try {
            postDocumentRepository.deleteById(postId);
            log.info("帖子从ElasticSearch中删除成功: {}", postId);
        } catch (Exception e) {
            log.error("帖子从ElasticSearch中删除失败: {}", postId, e);
        }
    }

    /**
     * 从ES中删除资源
     */
    @Override
    public void deleteResourceFromEs(Integer resourceId) {
        try {
            resourceDocumentRepository.deleteById(resourceId);
            log.info("资源从ElasticSearch中删除成功: {}", resourceId);
        } catch (Exception e) {
            log.error("资源从ElasticSearch中删除失败: {}", resourceId, e);
        }
    }

    /**
     * 从ES中删除用户
     */
    @Override
    public void deleteUserFromEs(Integer userId) {
        try {
            userDocumentRepository.deleteById(userId);
            log.info("用户从ElasticSearch中删除成功: {}", userId);
        } catch (Exception e) {
            log.error("用户从ElasticSearch中删除失败: {}", userId, e);
        }
    }

    /**
     * 搜索帖子
     */
    @Override
    public Page<PostDocument> searchPosts(String keyword, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        try {
            return postDocumentRepository.findByTitleOrContentOrderByCreateTimeDesc(keyword, pageRequest);
        } catch (Exception e) {
            log.error("搜索帖子失败", e);
            return Page.empty();
        }
    }

    /**
     * 搜索资源
     */
    @Override
    public Page<ResourceDocument> searchResources(String keyword, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "uploadTime"));
        try {
            return resourceDocumentRepository.findByTitleOrDescriptionOrderByUploadTimeDesc(keyword, pageRequest);
        } catch (Exception e) {
            log.error("搜索资源失败", e);
            return Page.empty();
        }
    }

    /**
     * 搜索用户
     */
    @Override
    public Page<UserDocument> searchUsers(String keyword, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "registerTime"));
        try {
            return userDocumentRepository.findByUsernameOrBioOrderByCreateTimeDesc(keyword, pageRequest);
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            return Page.empty();
        }
    }

    /**
     * 综合搜索（包括帖子、资源和用户）
     */
    @Override
    public List<Object> searchAll(String keyword, Integer page, Integer size) {
        try {
            List<Object> results = new ArrayList<>();
            
            // 搜索帖子
            Page<PostDocument> posts = searchPosts(keyword, page, size);
            results.addAll(posts.getContent());
            
            // 搜索资源
            Page<ResourceDocument> resources = searchResources(keyword, page, size);
            results.addAll(resources.getContent());
            
            // 搜索用户
            Page<UserDocument> users = searchUsers(keyword, page, size);
            results.addAll(users.getContent());
            
            return results;
        } catch (Exception e) {
            log.error("综合搜索失败", e);
            return Collections.emptyList();
        }
    }
} 