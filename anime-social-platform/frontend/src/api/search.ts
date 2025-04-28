import service from './axios';
import type { ApiResponse } from '@/types/api';
import type { SearchResult } from '@/types/search';
import type { PostListResponse } from '@/types/post';
import type { ResourceListResponse } from '@/types/resource';
import type { UserDetail } from '@/types/user';

/**
 * 全文搜索（同时搜索帖子、资源、用户）
 * @param keyword 搜索关键词
 * @param page 页码
 * @param size 每页数量
 * @returns 搜索结果
 */
export const searchAll = (keyword: string, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<SearchResult>>('/search', {
    params: {
      keyword,
      page,
      size
    }
  });
};

/**
 * 只搜索帖子
 * @param keyword 搜索关键词
 * @param page 页码
 * @param size 每页数量
 * @returns 搜索结果
 */
export const searchPosts = (keyword: string, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<PostListResponse>>('/search/posts', {
    params: {
      keyword,
      page,
      size
    }
  });
};

/**
 * 只搜索资源
 * @param keyword 搜索关键词
 * @param page 页码
 * @param size 每页数量
 * @returns 搜索结果
 */
export const searchResources = (keyword: string, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<ResourceListResponse>>('/search/resources', {
    params: {
      keyword,
      page,
      size
    }
  });
};

/**
 * 只搜索用户
 * @param keyword 搜索关键词
 * @param page 页码
 * @param size 每页数量
 * @returns 搜索结果
 */
export const searchUsers = (keyword: string, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<{ total: number, data: UserDetail[] }>>('/search/users', {
    params: {
      keyword,
      page,
      size
    }
  });
};

/**
 * 重建索引（仅管理员使用）
 * @returns 操作结果
 */
export const rebuildIndices = () => {
  return service.get<ApiResponse<string>>('/search/rebuild-indices');
}; 