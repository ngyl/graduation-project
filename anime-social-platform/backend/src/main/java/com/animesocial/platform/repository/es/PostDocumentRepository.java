package com.animesocial.platform.repository.es;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.animesocial.platform.model.es.PostDocument;

/**
 * 帖子ElasticSearch仓库接口
 */
@Repository
public interface PostDocumentRepository extends ElasticsearchRepository<PostDocument, Integer> {

    /**
     * 根据标题或内容搜索帖子
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 搜索结果
     */
    Page<PostDocument> findByTitleOrContent(String keyword, Pageable pageable);

    /**
     * 根据标签搜索帖子
     * @param tag 标签名
     * @param pageable 分页参数
     * @return 搜索结果
     */
    Page<PostDocument> findByTagsContaining(String tag, Pageable pageable);

    /**
     * 根据用户ID查找帖子
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 搜索结果
     */
    Page<PostDocument> findByUserId(Integer userId, Pageable pageable);

    /**
     * 获取置顶帖子
     * @return 置顶帖子列表
     */
    List<PostDocument> findByIsTopTrue();
} 