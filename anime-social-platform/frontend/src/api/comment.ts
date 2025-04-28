import service from './axios';
import type { ApiResponse } from '@/types/api';
import type { CommentDTO } from '@/types/comment';

/**
 * 根据帖子ID获取评论列表
 * @param postId 帖子ID
 * @returns 评论列表响应
 */
export function getCommentsByPost(postId: number) {
  return service.get<ApiResponse<CommentDTO[]>>('/comments/post/' + postId);
}

/**
 * 创建评论
 * @param data 评论数据
 * @returns 创建的评论响应
 */
export function createComment(data: {
  postId: number;
  content: string;
  parentId?: number;
}) {
  return service.post<ApiResponse<CommentDTO>>('/comments', data);
}

/**
 * 删除评论
 * @param id 评论ID
 * @returns 删除结果
 */
export function deleteComment(id: number) {
  return service.delete<ApiResponse<void>>(`/comments/${id}`);
}

/**
 * 获取用户的所有评论
 * @param userId 用户ID
 * @returns 评论列表响应
 */
export function getUserComments(userId: number) {
  return service.get<ApiResponse<CommentDTO[]>>(`/comments/user/${userId}`);
}

/**
 * 根据评论ID获取评论
 * @param id 评论ID
 * @returns 评论详情响应
 */
export function getCommentById(id: number) {
  return service.get<ApiResponse<CommentDTO>>(`/comments/${id}`);
} 