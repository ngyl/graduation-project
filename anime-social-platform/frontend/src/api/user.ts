import axios from '@/api/axios';
import type { UserDetailResponse, Post, FavoriteResource, FollowingUser } from '@/types/user';
import type { ApiResponse } from '@/types/auth';

// 获取用户详情
export const getUserDetail = (userId: number) => {
    return axios.get<ApiResponse<UserDetailResponse>>(`/api/users/${userId}`);
};

// 获取用户帖子列表
export const getUserPosts = (userId: number) => {
    return axios.get<ApiResponse<Post[]>>(`/api/users/${userId}/posts`);
};

// 获取用户收藏列表
export const getUserFavorites = (userId: number) => {
    return axios.get<ApiResponse<FavoriteResource[]>>(`/api/users/${userId}/favorites`);
};

// 获取用户关注列表
export const getUserFollowing = (userId: number) => {
    return axios.get<ApiResponse<FollowingUser[]>>(`/api/users/${userId}/following`);
};

// 更新用户信息
export const updateUserInfo = (userId: number, data: { bio?: string; avatar?: string }) => {
    return axios.put<ApiResponse<void>>(`/api/users/${userId}`, data);
};

// 关注/取消关注用户
export const followUser = (userId: number) => {
    return axios.post<ApiResponse<string>>(`/api/users/${userId}/follow`);
}; 