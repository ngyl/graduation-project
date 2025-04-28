/**
 * 管理员面板统计数据
 */
export interface StatisticsData {
  userCount: number;
  postCount: number;
  resourceCount: number;
  eventCount: number;
  tagCount: number;
  activeUserCount: number;
  dailyActiveUser: number[];
  postDistribution: {
    category: string;
    count: number;
  }[];
  resourceDistribution: {
    type: string;
    count: number;
  }[];
  latestUsers: any[];
  topPosts: any[];
  currentEvents: any[];
  hotTags: any[];
}

/**
 * 用户状态更新请求
 */
export interface UpdateStatusRequest {
  status: number;
}

/**
 * 管理员角色更新请求
 */
export interface UpdateAdminRoleRequest {
  isAdmin: boolean;
}

/**
 * 帖子置顶状态更新请求
 */
export interface UpdatePostTopRequest {
  isTop: boolean;
}

/**
 * 活动状态更新请求
 */
export interface UpdateEventStatusRequest {
  status: number;
} 