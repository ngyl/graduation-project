import service from './axios';
import type { Post, CreatePostRequest, GetPostsParams, PostListResponse } from '@/types/post';
import type { ApiResponse } from '@/types/api';

/**
 * 获取帖子列表
 * @param params 查询参数
 */
export const getPosts = (params: GetPostsParams) => {
    return service.get<ApiResponse<PostListResponse>>('/posts', { params });
};

/**
 * 创建帖子
 * @param data 帖子数据
 */
export const createPost = (data: CreatePostRequest) => {
    return service.post<ApiResponse<Post>>('/posts', data);
};

/**
 * 获取帖子详情
 * @param id 帖子ID
 */
export const getPostDetail = (id: number) => {
    return service.get<ApiResponse<Post>>(`/posts/${id}`);
};

/**
 * 更新帖子
 * @param id 帖子ID
 * @param data 帖子数据
 */
export const updatePost = (id: number, data: CreatePostRequest) => {
    return service.put<ApiResponse<Post>>(`/posts/${id}`, data);
};

/**
 * 删除帖子
 * @param id 帖子ID
 */
export const deletePost = (id: number) => {
    return service.delete<ApiResponse<void>>(`/posts/${id}`);
};

/**
 * 点赞帖子
 * @param id 帖子ID
 */
export const likePost = (id: number) => {
    return service.post<ApiResponse<void>>(`/posts/${id}/like`);
};

/**
 * 取消点赞
 * @param id 帖子ID
 */
export const unlikePost = (id: number) => {
    return service.delete<ApiResponse<void>>(`/posts/${id}/like`);
}; 