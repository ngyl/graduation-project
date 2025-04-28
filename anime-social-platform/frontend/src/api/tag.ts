import service from './axios';
import type { TagDTO, UserTagsDTO } from '@/types/tag';
import type { ApiResponse } from '@/types/api';

/**
 * 获取所有标签
 */
export const getAllTags = () => {
    return service.get<ApiResponse<TagDTO[]>>('/tags');
};

/**
 * 根据类型获取标签
 * @param type 标签类型
 */
export const getTagsByType = (type: string) => {
    return service.get<ApiResponse<TagDTO[]>>(`/tags/type/${type}`);
};

/**
 * 获取用户标签
 * @param userId 用户ID
 */
export const getUserTags = (userId: number) => {
    return service.get<ApiResponse<UserTagsDTO>>(`/tags/user/${userId}`);
};

/**
 * 更新用户标签
 * @param userId 用户ID
 * @param data 标签数据
 */
export const updateUserTags = (userId: number, data: { postTagIds?: number[]; resourceTagIds?: number[] }) => {
    return service.put<ApiResponse<void>>(`/tags/user/${userId}`, data);
};

/**
 * 创建标签
 * @param tag 标签数据
 */
export const createTag = (tag: Omit<TagDTO, 'id'>) => {
    return service.post<ApiResponse<TagDTO>>('/tags', tag);
};

/**
 * 更新标签
 * @param id 标签ID
 * @param tag 标签数据
 */
export const updateTag = (id: number, tag: Omit<TagDTO, 'id'>) => {
    return service.put<ApiResponse<TagDTO>>(`/tags/${id}`, tag);
};

/**
 * 删除标签
 * @param id 标签ID
 */
export const deleteTag = (id: number) => {
    return service.delete<ApiResponse<void>>(`/tags/${id}`);
};
