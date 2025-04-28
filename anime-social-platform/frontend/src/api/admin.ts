import service from './axios';
import type { ApiResponse } from '@/types/api';
import type { UserDTO } from '@/types/user';
import type { Post } from '@/types/post';
import type { Resource } from '@/types/resource';
import type { Event } from '@/types/event';
import type { StatisticsData } from '@/types/admin';

/**
 * 获取管理员面板统计数据
 */
export const getDashboardStatistics = () => {
  return service.get<ApiResponse<StatisticsData>>('/admin/statistics');
};

/**
 * 获取所有用户列表（分页）
 * @param page 页码
 * @param size 每页数量
 */
export const getAllUsers = (page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<UserDTO[]>>('/admin/users', {
    params: { page, size }
  });
};

/**
 * 获取所有管理员列表
 */
export const getAllAdmins = () => {
  return service.get<ApiResponse<UserDTO[]>>('/admin/admins');
};

/**
 * 禁用/启用用户
 * @param userId 用户ID
 * @param status 状态(0禁用,1启用)
 */
export const updateUserStatus = (userId: number, status: number) => {
  return service.put<ApiResponse<void>>(`/admin/users/${userId}/status`, null, {
    params: { status }
  });
};

/**
 * 设置/取消管理员权限
 * @param userId 用户ID
 * @param isAdmin 是否为管理员
 */
export const updateUserAdminRole = (userId: number, isAdmin: boolean) => {
  return service.put<ApiResponse<void>>(`/admin/users/${userId}/role`, { isAdmin });
};

/**
 * 获取所有帖子（分页）
 * @param page 页码
 * @param size 每页数量
 * @param tagId 标签ID (可选)
 * @param sort 排序方式 (可选)
 */
export const getAllPosts = (page: number = 1, size: number = 10, tagId?: number | null, sort: string = 'latest') => {
  console.log('请求帖子列表参数:', { page, size, tagId, sort });
  
  return service.get<ApiResponse<any>>('/admin/posts', {
    params: { page, size, tagId, sort }
  });
};

/**
 * 搜索帖子
 * @param keyword 搜索关键词
 * @param page 页码
 * @param size 每页数量
 */
export const searchPosts = (keyword: string, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<Post[]>>('/admin/posts/search', {
    params: { keyword, page, size }
  });
};

/**
 * 置顶/取消置顶帖子
 * @param postId 帖子ID
 * @param isTop 是否置顶(1置顶,0取消置顶)
 */
export const updatePostTopStatus = (postId: number, isTop: number) => {
  return service.put<ApiResponse<void>>(`/admin/posts/${postId}/top`, null, {
    params: { isTop }
  });
};

/**
 * 删除帖子
 * @param postId 帖子ID
 */
export const deletePost = (postId: number) => {
  return service.delete<ApiResponse<void>>(`/admin/posts/${postId}`);
};

/**
 * 获取所有资源（分页）
 * @param page 页码
 * @param size 每页数量
 * @param tagId 标签ID (可选)
 * @param sort 排序方式 (可选)
 */
export const getAllResources = (page: number = 1, size: number = 10, tagId?: number | null, sort: string = 'latest') => {
  console.log('请求资源列表参数:', { page, size, tagId, sort });
  
  return service.get<ApiResponse<any>>('/admin/resources', {
    params: { page, size, tagId, sort }
  });
};

/**
 * 搜索资源
 * @param keyword 搜索关键词
 * @param page 页码
 * @param size 每页数量
 */
export const searchResources = (keyword: string, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<Resource[]>>('/admin/resources/search', {
    params: { keyword, page, size }
  });
};

/**
 * 删除资源
 * @param resourceId 资源ID
 */
export const deleteResource = (resourceId: number) => {
  return service.delete<ApiResponse<void>>(`/admin/resources/${resourceId}`);
};

/**
 * 获取所有活动（分页）
 * @param page 页码
 * @param size 每页数量
 */
export const getAllEvents = (page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<Event[]>>('/admin/events', {
    params: { page, size }
  });
};

/**
 * 创建活动
 * @param data 活动数据
 */
export const createEvent = (data: Omit<Event, 'id'>) => {
  return service.post<ApiResponse<Event>>('/admin/events', data);
};

/**
 * 更新活动
 * @param eventId 活动ID
 * @param data 活动数据
 */
export const updateEvent = (eventId: number, data: Omit<Event, 'id'>) => {
  return service.put<ApiResponse<Event>>(`/admin/events/${eventId}`, data);
};

/**
 * 删除活动
 * @param eventId 活动ID
 */
export const deleteEvent = (eventId: number) => {
  return service.delete<ApiResponse<void>>(`/admin/events/${eventId}`);
};

/**
 * 更新活动状态
 * @param eventId 活动ID
 * @param status 状态(0下线,1上线)
 */
export const updateEventStatus = (eventId: number, status: number) => {
  return service.put<ApiResponse<void>>(`/admin/events/${eventId}/status`, null, {
    params: { status }
  });
};

/**
 * 搜索用户
 * @param keyword 搜索关键词
 * @param page 页码
 * @param size 每页数量
 */
export const searchUsers = (keyword: string, page: number = 1, size: number = 10) => {
  return service.get<ApiResponse<UserDTO[]>>('/admin/users/search', {
    params: { keyword, page, size }
  });
};

/**
 * 获取所有标签
 * @param type 标签类型(可选，post/resource)
 */
export const getAllTags = (type?: string) => {
  return service.get<ApiResponse<any[]>>('/admin/tags', {
    params: { type }
  });
};

/**
 * 获取标签详情
 * @param tagId 标签ID
 */
export const getTagById = (tagId: number) => {
  return service.get<ApiResponse<any>>(`/admin/tags/${tagId}`);
};

/**
 * 创建标签
 * @param data 标签数据
 */
export const createTag = (data: { name: string; type: string; category?: string }) => {
  return service.post<ApiResponse<any>>('/admin/tags', data);
};

/**
 * 更新标签
 * @param tagId 标签ID
 * @param data 标签数据
 */
export const updateTag = (tagId: number, data: { name: string; type: string; category?: string }) => {
  return service.put<ApiResponse<any>>(`/admin/tags/${tagId}`, data);
};

/**
 * 删除标签
 * @param tagId 标签ID
 */
export const deleteTag = (tagId: number) => {
  return service.delete<ApiResponse<void>>(`/admin/tags/${tagId}`);
};

/**
 * 搜索标签
 * @param keyword 搜索关键词
 * @param type 标签类型(可选，post/resource)
 */
export const searchTags = (keyword: string, type?: string) => {
  return service.get<ApiResponse<any[]>>('/admin/tags/search', {
    params: { keyword, type }
  });
};

/**
 * 获取标签统计信息
 */
export const getTagStatistics = () => {
  return service.get<ApiResponse<any>>('/admin/tags/statistics');
}; 