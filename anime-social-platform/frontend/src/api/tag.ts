import axios from '@/api/axios';
import type { TagDTO, UserTagsDTO } from '@/types/tag';
import type { ApiResponse } from '@/types/auth';

// 获取所有标签
export const getAllTags = () => {
    return axios.get<ApiResponse<TagDTO[]>>('/api/tags');
};

// 根据类型获取标签
export const getTagsByType = (type: string) => {
    return axios.get<ApiResponse<TagDTO[]>>(`/api/tags/type/${type}`);
};

// 获取用户标签
export const getUserTags = (userId: number) => {
    return axios.get<ApiResponse<UserTagsDTO>>(`/api/tags/user/${userId}`);
};

// 更新用户标签
export const updateUserTags = (userId: number, data: { postTagIds?: number[]; resourceTagIds?: number[] }) => {
    return axios.put<ApiResponse<void>>(`/api/tags/user/${userId}`, data);
}; 