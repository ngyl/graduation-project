import request from './axios';
import type { ApiResponse, FriendshipStatus, FriendshipCounts } from '../types/api';
import type { FollowingUser } from '../types/user';

/**
 * 关注用户
 * @param friendId 要关注的用户ID
 */
export function followUser(friendId: number) {
  return request<ApiResponse<string>>({
    url: `/api/friendships/follow/${friendId}`,
    method: 'post'
  });
}

/**
 * 取消关注用户
 * @param friendId 要取消关注的用户ID
 */
export function unfollowUser(friendId: number) {
  return request<ApiResponse<string>>({
    url: `/api/friendships/unfollow/${friendId}`,
    method: 'delete'
  });
}

/**
 * 获取用户关注的人列表
 * @param userId 用户ID
 * @param page 页码（可选）
 * @param size 每页大小（可选）
 */
export function getFollowings(userId: number, page?: number, size?: number) {
  return request<ApiResponse<FollowingUser[]>>({
    url: `/api/friendships/following/${userId}`,
    method: 'get',
    params: { page, size }
  });
}

/**
 * 获取用户的粉丝列表
 * @param userId 用户ID
 * @param page 页码（可选）
 * @param size 每页大小（可选）
 */
export function getFollowers(userId: number, page?: number, size?: number) {
  return request<ApiResponse<FollowingUser[]>>({
    url: `/api/friendships/followers/${userId}`,
    method: 'get',
    params: { page, size }
  });
}

/**
 * 获取互相关注的好友列表
 */
export function getMutualFriends(userId: number) {
  return request<ApiResponse<FollowingUser[]>>({
    url: '/api/friendships/mutual',
    method: 'get'
  });
}

/**
 * 检查与指定用户的关注状态
 * @param friendId 要检查关系的用户ID
 */
export function checkFriendshipStatus(friendId: number) {
  return request<ApiResponse<FriendshipStatus>>({
    url: `/api/friendships/status/${friendId}`,
    method: 'get'
  });
}

/**
 * 获取用户的关注数量和粉丝数量
 * @param userId 用户ID
 */
export function getFriendshipCounts(userId: number) {
  return request<ApiResponse<FriendshipCounts>>({
    url: `/api/friendships/counts/${userId}`,
    method: 'get'
  });
} 