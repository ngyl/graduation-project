import service from './axios';
import type { ApiResponse } from '@/types/api';
import type { Resource, ResourceListResponse } from '@/types/resource';

/**
 * 获取资源列表
 * @param page 当前页码
 * @param size 每页数量
 * @param tagId 标签ID（可选）
 * @param sort 排序方式（latest/downloads/likes）
 * @returns 资源列表及分页信息
 */
export const getResources = (page: number = 1, size: number = 10, tagId?: number, sort: string = 'latest') => {
  return service.get<ApiResponse<ResourceListResponse>>('/resources', {
    params: {
      page,
      size,
      tagId,
      sort
    }
  });
};

/**
 * 获取指定ID的资源详情
 * @param id 资源ID
 * @returns 资源详情
 */
export const getResourceById = (id: number) => {
  return service.get<ApiResponse<Resource>>(`/resources/${id}`);
};

/**
 * 获取指定用户的资源列表
 * @param userId 用户ID
 * @param page 当前页码
 * @param size 每页数量
 * @returns 资源列表
 */
export const getUserResources = (userId: number, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<ResourceListResponse>>(`/resources/user/${userId}`, {
    params: {
      page,
      size
    }
  });
};

/**
 * 创建资源（文件已上传）
 * @param formData 包含资源信息的FormData对象，包括文件路径
 * @returns 创建结果
 */
export const createResource = (formData: FormData) => {
  return service.post<ApiResponse<Resource>>('/resources/create', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

/**
 * 更新资源
 * @param id 资源ID
 * @param formData 包含更新信息的FormData对象
 * @returns 更新结果
 */
export const updateResource = (id: number, formData: FormData) => {
  return service.put<ApiResponse<Resource>>(`/resources/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

/**
 * 删除资源
 * @param id 资源ID
 * @returns 删除结果
 */
export const deleteResource = (id: number) => {
  return service.delete<ApiResponse<void>>(`/resources/${id}`);
};

/**
 * 获取资源下载链接并增加下载计数
 * @param id 资源ID
 * @returns 下载链接
 */
export const downloadResource = (id: number) => {
  return service.get<ApiResponse<string>>(`/resources/${id}/download`);
};

/**
 * 添加资源收藏
 * @param resourceId 资源ID
 * @returns 收藏结果
 */
export const addFavorite = (resourceId: number) => {
  return service.post<ApiResponse<void>>(`/resources/${resourceId}/favorite`);
};

/**
 * 取消资源收藏
 * @param resourceId 资源ID
 * @returns 取消收藏结果
 */
export const removeFavorite = (resourceId: number) => {
  return service.delete<ApiResponse<void>>(`/resources/${resourceId}/favorite`);
};

/**
 * 点赞资源
 * @param resourceId 资源ID
 * @returns 点赞结果
 */
export const likeResource = (resourceId: number) => {
  return service.post<ApiResponse<void>>(`/resources/${resourceId}/like`);
};

/**
 * 取消点赞资源
 * @param resourceId 资源ID
 * @returns 取消点赞结果
 */
export const unlikeResource = (resourceId: number) => {
  return service.delete<ApiResponse<void>>(`/resources/${resourceId}/like`);
};

/**
 * 获取用户点赞的资源列表
 * @param page 当前页码
 * @param size 每页数量
 * @returns 点赞的资源列表
 */
export const getLikedResources = (page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<ResourceListResponse>>('/resources/liked', {
    params: {
      page,
      size
    }
  });
};

/**
 * 获取用户收藏的资源列表
 * @param page 当前页码
 * @param size 每页数量
 * @returns 收藏的资源列表
 */
export const getFavoriteResources = (page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<ResourceListResponse>>('/resources/favorites', {
    params: {
      page,
      size
    }
  });
};

/**
 * 搜索资源
 * @param keyword 搜索关键词
 * @param page 当前页码
 * @param size 每页数量
 * @returns 搜索结果
 */
export const searchResources = (keyword: string, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<ResourceListResponse>>('/resources/search', {
    params: {
      keyword,
      page,
      size
    }
  });
}; 

/**
 * 上传资源文件
 * @param file 资源文件
 */
export const uploadResource = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return service.post<ApiResponse<{url: string, fileType: string, fileName: string, fileSize: number}>>('/upload/resource', formData, {
      headers: {
          'Content-Type': 'multipart/form-data'
      }
  });
};

/**
* 上传封面图片
* @param file 封面图片
*/
export const uploadCover = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return service.post<ApiResponse<{url: string}>>('/upload/cover', formData, {
      headers: {
          'Content-Type': 'multipart/form-data'
      }
  });
};