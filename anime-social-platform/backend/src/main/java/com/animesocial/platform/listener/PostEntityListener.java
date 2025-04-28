package com.animesocial.platform.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.animesocial.platform.model.Post;
import com.animesocial.platform.service.ElasticsearchService;
import com.animesocial.platform.util.SpringContextHolder;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * Post实体监听器
 * 用于在Post增删改时自动同步ES索引
 */
@Slf4j
public class PostEntityListener {
    
    /**
     * 保存或更新后同步到ES
     */
    @PostPersist
    @PostUpdate
    public void postPersistOrUpdate(Post post) {
        try {
            log.debug("触发Post实体监听：保存/更新, ID: {}", post.getId());
            // 由于监听器不是由Spring创建的，所以需要手动获取Bean
            ElasticsearchService elasticsearchService = SpringContextHolder.getBean(ElasticsearchService.class);
            elasticsearchService.syncPostToEs(post.getId());
        } catch (Exception e) {
            log.error("Post监听器同步ES失败: {}", post.getId(), e);
        }
    }
    
    /**
     * 删除后从ES移除
     */
    @PostRemove
    public void postRemove(Post post) {
        try {
            log.debug("触发Post实体监听：删除, ID: {}", post.getId());
            ElasticsearchService elasticsearchService = SpringContextHolder.getBean(ElasticsearchService.class);
            elasticsearchService.deletePostFromEs(post.getId());
        } catch (Exception e) {
            log.error("Post监听器从ES删除失败: {}", post.getId(), e);
        }
    }
} 