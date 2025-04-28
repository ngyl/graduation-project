package com.animesocial.platform.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.animesocial.platform.model.es.PostDocument;
import com.animesocial.platform.model.es.ResourceDocument;
import com.animesocial.platform.model.es.UserDocument;

/**
 * ElasticSearch服务接口
 * 负责处理ElasticSearch的索引、搜索等操作
 */
public interface ElasticsearchService {

    /**
     * 初始化所有索引
     */
    void initIndices();

    /**
     * 重建所有索引
     */
    void rebuildIndices();

    /**
     * 同步单个帖子到ES
     * @param postId 帖子ID
     */
    void syncPostToEs(Integer postId);

    /**
     * 同步单个资源到ES
     * @param resourceId 资源ID
     */
    void syncResourceToEs(Integer resourceId);

    /**
     * 同步单个用户到ES
     * @param userId 用户ID
     */
    void syncUserToEs(Integer userId);

    /**
     * 从ES中删除帖子
     * @param postId 帖子ID
     */
    void deletePostFromEs(Integer postId);

    /**
     * 从ES中删除资源
     * @param resourceId 资源ID
     */
    void deleteResourceFromEs(Integer resourceId);

    /**
     * 从ES中删除用户
     * @param userId 用户ID
     */
    void deleteUserFromEs(Integer userId);

    /**
     * 搜索帖子
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    Page<PostDocument> searchPosts(String keyword, Integer page, Integer size);

    /**
     * 搜索资源
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    Page<ResourceDocument> searchResources(String keyword, Integer page, Integer size);

    /**
     * 搜索用户
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    Page<UserDocument> searchUsers(String keyword, Integer page, Integer size);

    /**
     * 全文搜索（帖子、资源、用户）
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果，包含所有类型的结果
     */
    List<Object> searchAll(String keyword, Integer page, Integer size);
} 