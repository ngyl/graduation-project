import service from './axios';
import type { ApiResponse } from '@/types/api';
import type { FavoriteResource } from '@/types/user';
import type { FavoriteData } from '@/types/favorite';
import { ResourceListResponse } from '@/types/resource';

/**
 * 获取用户收藏列表
 * @param page 页码
 * @param size 每页数量
 */
export const getFavorites = (page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<FavoriteResource[]>>('/favorites', {
    params: { page, size }
  });
};

/**
 * 添加收藏
 * @param resourceId 资源ID
 * @param userId 用户ID
 */
export const addFavorite = (resourceId: number, userId: number) => {
  return service.post<ApiResponse<FavoriteData>>('/favorites', null, {
    params: { resourceId, userId }
  });
};

/**
 * 删除收藏
 * @param resourceId 资源ID
 * @param userId 用户ID
 */
export const removeFavorite = (resourceId: number, userId: number) => {
  return service.delete<ApiResponse<boolean>>('/favorites', {
    params: { resourceId, userId }
  });
};

/**
 * 检查是否已收藏
 * @param resourceId 资源ID
 * @param userId 用户ID
 */
export const checkFavorite = (resourceId: number, userId: number) => {
  return service.get<ApiResponse<boolean>>('/favorites/check', {
    params: { resourceId, userId }
  });
};

/**
 * 获取用户收藏列表
 * @param userId 用户ID
 */
export const getUserFavorites = (userId: number) => {
  return service.get<ApiResponse<ResourceListResponse>>(`/favorites/user/${userId}`);
};

/**
 * 获取用户收藏列表（分页）
 * @param userId 用户ID
 * @param page 页码
 * @param size 每页数量
 */
export const getUserFavoritesWithPagination = (userId: number, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<ResourceListResponse>>(`/favorites/user/${userId}/page`, {
    params: {
      page,
      size
    }
  });
};

/**
 * 获取资源收藏数量
 * @param resourceId 资源ID
 */
export const getResourceFavoriteCount = (resourceId: number) => {
  return service.get<ApiResponse<number>>(`/favorites/resource/${resourceId}/count`);
}; 