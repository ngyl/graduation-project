package com.animesocial.platform.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.animesocial.platform.model.Resource;
import com.animesocial.platform.service.ElasticsearchService;
import com.animesocial.platform.util.SpringContextHolder;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * Resource实体监听器
 * 用于在Resource增删改时自动同步ES索引
 */
@Slf4j
public class ResourceEntityListener {
    
    /**
     * 保存或更新后同步到ES
     */
    @PostPersist
    @PostUpdate
    public void postPersistOrUpdate(Resource resource) {
        try {
            log.debug("触发Resource实体监听：保存/更新, ID: {}", resource.getId());
            ElasticsearchService elasticsearchService = SpringContextHolder.getBean(ElasticsearchService.class);
            elasticsearchService.syncResourceToEs(resource.getId());
        } catch (Exception e) {
            log.error("Resource监听器同步ES失败: {}", resource.getId(), e);
        }
    }
    
    /**
     * 删除后从ES移除
     */
    @PostRemove
    public void postRemove(Resource resource) {
        try {
            log.debug("触发Resource实体监听：删除, ID: {}", resource.getId());
            ElasticsearchService elasticsearchService = SpringContextHolder.getBean(ElasticsearchService.class);
            elasticsearchService.deleteResourceFromEs(resource.getId());
        } catch (Exception e) {
            log.error("Resource监听器从ES删除失败: {}", resource.getId(), e);
        }
    }
} 