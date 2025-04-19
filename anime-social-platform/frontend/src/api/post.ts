import axios from '@/api/axios';
import type { Post, CreatePostRequest, GetPostsParams, PostListResponse } from '@/types/post';
import type { ApiResponse } from '@/types/auth';

// 获取帖子列表
export const getPosts = (params: GetPostsParams) => {
    return axios.get<ApiResponse<PostListResponse>>('/api/posts', { params });
};

// 创建帖子
export const createPost = (data: CreatePostRequest) => {
    return axios.post<ApiResponse<Post>>('/api/posts', data);
};

// 获取帖子详情
export const getPostDetail = (id: number) => {
    return axios.get<ApiResponse<Post>>(`/api/posts/${id}`);
};

// 更新帖子
export const updatePost = (id: number, data: CreatePostRequest) => {
    return axios.put<ApiResponse<Post>>(`/api/posts/${id}`, data);
};

// 删除帖子
export const deletePost = (id: number) => {
    return axios.delete<ApiResponse<void>>(`/api/posts/${id}`);
};

// 点赞帖子
export const likePost = (id: number) => {
    return axios.post<ApiResponse<void>>(`/api/posts/${id}/like`);
};

// 取消点赞
export const unlikePost = (id: number) => {
    return axios.delete<ApiResponse<void>>(`/api/posts/${id}/like`);
}; 