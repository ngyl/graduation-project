/**
 * 收藏数据响应
 */
export interface FavoriteData {
  id: number;
  userId: number;
  resourceId: number;
  createdAt: string;
}

/**
 * 收藏请求参数
 */
export interface FavoriteRequest {
  resourceId: number;
  userId: number;
} 