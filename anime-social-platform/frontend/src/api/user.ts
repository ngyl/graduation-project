import axios from '@/api/axios';
import type { UserDetailResponse, Post, FavoriteResource, FollowingUser } from '@/types/user';
import type { ApiResponse } from '@/types/auth';

// 获取用户详情
export const getUserDetail = (userId: number) => {
    return axios.get<ApiResponse<UserDetailResponse>>(`/users/${userId}`);
};

// 获取用户帖子列表
export const getUserPosts = (userId: number) => {
    return axios.get<ApiResponse<Post[]>>(`/users/${userId}/posts`);
};

// 获取用户收藏列表
export const getUserFavorites = (userId: number) => {
    return axios.get<ApiResponse<FavoriteResource[]>>(`/users/${userId}/favorites`);
};

// 获取用户关注列表
export const getUserFollowing = (userId: number) => {
    return axios.get<ApiResponse<FollowingUser[]>>(`/users/${userId}/following`);
};

// 更新用户信息
export const updateUserInfo = (userId: number, data: { bio?: string; avatar?: string }) => {
    return axios.put<ApiResponse<void>>(`/users/${userId}`, data);
}; 