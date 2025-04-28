/**
 * 活动基础接口
 */
export interface Event {
  id?: number;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  status: number;
  createdBy?: number;
  createdByUsername?: string;
  creatorName?: string;
  createdAt?: string;
  isOngoing?: boolean;
}

/**
 * 活动DTO对象
 */
export interface EventDTO extends Event {
  // 可以在这里扩展额外字段
}

/**
 * 创建活动请求
 */
export interface CreateEventRequest {
  title: string;
  description: string;
  startTime: string;
  endTime: string;
} 