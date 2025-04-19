package com.animesocial.platform.service;

import com.animesocial.platform.model.dto.ResourceDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 资源服务接口
 * 定义资源相关的业务逻辑方法
 */
public interface ResourceService {
    
    /**
     * 根据ID获取资源
     * @param id 资源ID
     * @return 资源详情
     */
    ResourceDTO getResourceById(Integer id);
    
    /**
     * 获取用户上传的资源列表
     * @param userId 用户ID
     * @return 资源列表
     */
    List<ResourceDTO> getResourcesByUserId(Integer userId);
    
    /**
     * 分页获取资源列表
     * @param page 页码
     * @param size 每页数量
     * @param tagId 标签ID（可选）
     * @param sort 排序方式（latest/downloads/likes）
     * @return 资源列表和总数
     */
    Map<String, Object> getAllResources(Integer page, Integer size, Integer tagId, String sort);
    
    /**
     * 上传资源
     * @param userId 用户ID
     * @param title 资源标题
     * @param description 资源描述（可选）
     * @param tagIds 标签ID列表（可选）
     * @param file 上传文件
     * @return 上传的资源信息
     */
    ResourceDTO uploadResource(Integer userId, String title, String description, List<Integer> tagIds, MultipartFile file);
    
    /**
     * 更新资源信息
     * @param id 资源ID
     * @param title 资源标题
     * @param description 资源描述（可选）
     * @param tagIds 标签ID列表（可选）
     * @return 更新后的资源信息
     */
    ResourceDTO updateResource(Integer id, String title, String description, List<Integer> tagIds);
    
    /**
     * 删除资源
     * @param id 资源ID
     */
    void deleteResource(Integer id);
    
    /**
     * 收藏资源
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 操作结果描述
     */
    String favoriteResource(Integer resourceId, Integer userId);
    
    /**
     * 取消收藏
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 操作结果描述
     */
    String unfavoriteResource(Integer resourceId, Integer userId);
    
    /**
     * 检查用户是否已收藏资源
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return true表示已收藏，false表示未收藏
     */
    boolean isResourceFavorited(Integer resourceId, Integer userId);
    
    /**
     * 获取资源下载URL
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 下载URL
     */
    String getDownloadUrl(Integer resourceId, Integer userId);
    
    /**
     * 增加资源下载计数
     * @param resourceId 资源ID
     */
    void incrementDownloadCount(Integer resourceId);
    
    /**
     * 搜索资源
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @return 匹配的资源列表和总数
     */
    Map<String, Object> searchResources(String keyword, Integer page, Integer size);
} 