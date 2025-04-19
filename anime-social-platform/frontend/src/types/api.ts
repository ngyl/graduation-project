/**
 * API响应的通用格式
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

/**
 * 好友关系状态
 */
export interface FriendshipStatus {
  isFollowing: boolean;
  isFollower: boolean;
  isMutual: boolean;
}

/**
 * 好友关系统计
 */
export interface FriendshipCounts {
  following: number;
  followers: number;
} 