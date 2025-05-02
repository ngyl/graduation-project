package com.animesocial.platform.repository.es;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.animesocial.platform.model.es.ResourceDocument;

/**
 * 资源ElasticSearch仓库接口
 */
@Repository
public interface ResourceDocumentRepository extends ElasticsearchRepository<ResourceDocument, Integer> {

    /**
     * 根据标题或描述搜索资源
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 搜索结果
     */
    @Query("""
        {
            "multi_match": {
                "query": "?0",
                "fields": ["title", "description"]
            }
        }
        """)
    Page<ResourceDocument> findByTitleOrDescriptionOrderByUploadTimeDesc(String keyword, Pageable pageable);

    /**
     * 根据标签搜索资源
     * @param tag 标签名
     * @param pageable 分页参数
     * @return 搜索结果
     */
    Page<ResourceDocument> findByTags(String tag, Pageable pageable);

    /**
     * 根据用户ID查找资源
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 搜索结果
     */
    Page<ResourceDocument> findByUserId(Integer userId, Pageable pageable);

    /**
     * 根据文件类型搜索资源
     * @param fileType 文件类型
     * @param pageable 分页参数
     * @return 搜索结果
     */
    @Query("""
            {
                "query": {
                    "term": {
                        "fileType": "?0"
                    }
                }
            }
        """)
    Page<ResourceDocument> findByFileType(String fileType, Pageable pageable);
} 