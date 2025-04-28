import service from './axios';
import type { UserDetail, Post, FavoriteResource, UserDTO, Resource } from '@/types/user';
import type { ApiResponse } from '@/types/api';

/**
 * 获取用户详情
 * @param userId 用户ID
 */
export const getUserDetail = (userId: number) => {
    return service.get<ApiResponse<UserDetail>>(`/users/${userId}`);
};

/**
 * 获取用户帖子列表
 * @param userId 用户ID
 * @param page 页码
 * @param size 每页数量
 */
export const getUserPosts = (userId: number, page: number, size: number) => {
    return service.get<ApiResponse<Post[]>>(`/users/${userId}/posts?page=${page}&size=${size}`);
};

/** 
 * 获取用户上传的资源列表
 * @param userId 用户ID
 * @param page 页码
 * @param size 每页数量
 */
export const getUserResources = (userId: number, page: number, size: number) => {
    return service.get<ApiResponse<Resource[]>>(`/users/${userId}/resources?page=${page}&size=${size}`);
};

/**
 * 获取用户收藏列表
 * @param userId 用户ID
 * @param page 页码
 * @param size 每页数量
 */
export const getUserFavorites = (userId: number, page: number, size: number) => {
    return service.get<ApiResponse<FavoriteResource[]>>(`/users/${userId}/favorites?page=${page}&size=${size}`);
};

/**
 * 获取用户关注列表
 * @param userId 用户ID
 */
export const getUserFollowing = (userId: number) => {
    return service.get<ApiResponse<UserDTO[]>>(`/users/${userId}/following`);
};

/**
 * 获取用户粉丝列表
 * @param userId 用户ID
 */
export const getUserFollowers = (userId: number) => {
    return service.get<ApiResponse<UserDTO[]>>(`/users/${userId}/followers`);
};

/**
 * 获取用户互相关注列表
 * @param userId 用户ID
 */
export const getUserMutual = (userId: number) => {
    return service.get<ApiResponse<UserDTO[]>>(`/users/${userId}/mutual`);
};

/**
 * 更新用户信息
 * @param userId 用户ID
 * @param data 用户信息
 */
export const updateUserInfo = (userId: number, data: { bio?: string; avatar?: string }) => {
    return service.put<ApiResponse<void>>(`/users/${userId}`, data);
};

/**
 * 上传用户头像
 * @param file 头像文件
 */
export const uploadAvatar = (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return service.post<ApiResponse<{url: string}>>('/upload/avatar', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
};

/**
 * 切换关注状态（关注/取消关注用户）
 * @param userId 用户ID
 */
export const toggleFollow = (userId: number) => {
    return service.post<ApiResponse<string>>(`/users/${userId}/follow`);
};

/**
 * @deprecated 使用 toggleFollow 代替
 * 关注/取消关注用户
 * @param userId 用户ID
 */
export const followUser = (userId: number) => {
    return toggleFollow(userId);
};

/**
 * @deprecated 使用 toggleFollow 代替
 * 取消关注用户
 * @param userId 用户ID
 */
export const unfollowUser = (userId: number) => {
    return toggleFollow(userId);
}; 