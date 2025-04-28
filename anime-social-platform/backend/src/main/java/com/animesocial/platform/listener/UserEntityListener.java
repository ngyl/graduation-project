package com.animesocial.platform.listener;

import org.springframework.stereotype.Component;

import com.animesocial.platform.model.User;
import com.animesocial.platform.service.ElasticsearchService;
import com.animesocial.platform.util.SpringContextHolder;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * User实体监听器
 * 用于在User增删改时自动同步ES索引
 */
@Slf4j
public class UserEntityListener {
    
    /**
     * 保存或更新后同步到ES
     */
    @PostPersist
    @PostUpdate
    public void postPersistOrUpdate(User user) {
        try {
            log.debug("触发User实体监听：保存/更新, ID: {}", user.getId());
            ElasticsearchService elasticsearchService = SpringContextHolder.getBean(ElasticsearchService.class);
            elasticsearchService.syncUserToEs(user.getId());
        } catch (Exception e) {
            log.error("User监听器同步ES失败: {}", user.getId(), e);
        }
    }
    
    /**
     * 删除后从ES移除
     */
    @PostRemove
    public void postRemove(User user) {
        try {
            log.debug("触发User实体监听：删除, ID: {}", user.getId());
            ElasticsearchService elasticsearchService = SpringContextHolder.getBean(ElasticsearchService.class);
            elasticsearchService.deleteUserFromEs(user.getId());
        } catch (Exception e) {
            log.error("User监听器从ES删除失败: {}", user.getId(), e);
        }
    }
} 