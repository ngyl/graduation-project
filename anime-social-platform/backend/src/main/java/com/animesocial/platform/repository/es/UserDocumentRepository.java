package com.animesocial.platform.repository.es;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.animesocial.platform.model.es.UserDocument;

/**
 * 用户ElasticSearch仓库接口
 */
@Repository
public interface UserDocumentRepository extends ElasticsearchRepository<UserDocument, Integer> {

    /**
     * 根据用户名或个人简介搜索用户
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 搜索结果
     */
    @Query("""
        {
            "multi_match": {
                "query": "?0",
                "fields": ["username", "bio"]
            }
        }
        """)
    Page<UserDocument> findByUsernameOrBioOrderByCreateTimeDesc(String keyword, Pageable pageable);

    /**
     * 根据标签搜索用户
     * @param tag 标签名
     * @param pageable 分页参数
     * @return 搜索结果
     */
    Page<UserDocument> findByTags(String tag, Pageable pageable);

    /**
     * 查找管理员用户
     * @param pageable 分页参数
     * @return 搜索结果
     */
    @Query("""
        {
            "query": {
                "term": {
                    "isAdmin": true
                }
            }
        }
        """)
    Page<UserDocument> findByIsAdminTrue(Pageable pageable);
} 