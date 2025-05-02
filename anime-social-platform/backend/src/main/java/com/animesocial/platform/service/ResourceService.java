package com.animesocial.platform.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.animesocial.platform.model.dto.ResourceDTO;
import com.animesocial.platform.model.dto.ResourceListResponse;

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
     * 分页获取用户上传的资源列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 资源列表
     */
    List<ResourceDTO> getResourcesByUserId(Integer userId, Integer page, Integer size);
    
    /**
     * 分页获取资源列表
     * @param page 页码
     * @param size 每页数量
     * @param tagId 标签ID（可选）
     * @param sort 排序方式（latest/downloads/likes）
     * @return 资源列表和总数
     */
    ResourceListResponse getAllResources(Integer page, Integer size, Integer tagId, String sort);
    
    /**
     * 上传资源
     * @param userId 用户ID
     * @param title 资源标题
     * @param description 资源描述
     * @param tagIds 标签ID列表
     * @param file 资源文件
     * @return 资源DTO
     */
    ResourceDTO uploadResource(Integer userId, String title, String description, List<Integer> tagIds, MultipartFile file);

    /**
     * 上传资源（带封面）
     * @param userId 用户ID
     * @param title 资源标题
     * @param description 资源描述
     * @param tagIds 标签ID列表
     * @param file 资源文件
     * @param coverFile 封面文件（可为null）
     * @return 资源DTO
     */
    ResourceDTO uploadResourceWithCover(Integer userId, String title, String description, List<Integer> tagIds, MultipartFile file, MultipartFile coverFile);
    
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
     * @param size 每页大小
     * @return 匹配的资源列表和总数
     */
    ResourceListResponse searchResources(String keyword, Integer page, Integer size);
    
    /**
     * 获取资源总数
     * @return 资源总数
     */
    int countResources();
    
    /**
     * 点赞资源
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 操作结果描述
     * @throws RuntimeException 如果资源不存在或已点赞
     */
    String likeResource(Integer resourceId, Integer userId);
    
    /**
     * 取消点赞
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 操作结果描述
     * @throws RuntimeException 如果资源不存在或未点赞
     */
    String unlikeResource(Integer resourceId, Integer userId);
    
    /**
     * 检查用户是否已点赞资源
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return true表示已点赞，false表示未点赞
     */
    boolean isResourceLiked(Integer resourceId, Integer userId);
    
    /**
     * 获取用户点赞的资源列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 资源列表和总数
     */
    ResourceListResponse getLikedResources(Integer userId, Integer page, Integer size);
    
    /**
     * 根据ID列表批量查询资源
     * @param ids 资源ID列表
     * @return 资源列表
     */
    List<ResourceDTO> findByIds(List<Integer> ids);
    
    /**
     * 使用已上传的文件路径创建资源
     * @param userId 用户ID
     * @param title 资源标题
     * @param description 资源描述
     * @param filePath 已上传的文件路径
     * @param coverPath 已上传的封面图路径
     * @param tagIds 标签ID列表
     * @return 创建的资源DTO
     */
    ResourceDTO createResource(Integer userId, String title, String description, String filePath, String coverPath, List<Integer> tagIds);
    
    /**
     * 获取热门资源
     * @param limit 获取数量，默认为12
     * @return 热门资源列表
     */
    ResourceListResponse getHotResources(Integer limit);
} 