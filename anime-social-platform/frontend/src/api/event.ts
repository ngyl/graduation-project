import service from './axios';
import type { ApiResponse } from '@/types/api';
import type { Event, CreateEventRequest, EventDTO } from '@/types/event';

/**
 * 获取当前进行中的活动
 */
export function getCurrentEvents() {
  return service.get<ApiResponse<Event[]>>('/event/current');
}

/**
 * 获取即将开始的活动
 */
export function getUpcomingEvents() {
  return service.get<ApiResponse<Event[]>>('/event/upcoming');
}

/**
 * 创建新活动
 */
export function createEvent(data: CreateEventRequest) {
  return service.post<ApiResponse<Event>>('/event/create', data);
}

/**
 * 参加活动
 * @param eventId 活动ID
 */
export function participateEvent(eventId: number) {
  return service.post<ApiResponse<void>>('/event/participate', { eventId });
}

/**
 * 检查用户是否已参加活动
 * @param eventId 活动ID
 */
export function checkParticipation(eventId: number) {
  return service.get<ApiResponse<boolean>>('/event/check-participation', {
    params: { eventId }
  });
}

/**
 * 获取活动详情
 * @param eventId 活动ID
 */
export function getEventDetail(eventId: number) {
  return service.get<ApiResponse<Event>>(`/event/${eventId}`);
}

/**
 * 获取用户参加的活动列表
 */
export function getUserEvents() {
  return service.get<ApiResponse<Event[]>>('/event/user-events');
}

/**
 * 取消参加活动
 * @param eventId 活动ID
 */
export function cancelParticipation(eventId: number) {
  return service.post<ApiResponse<void>>('/event/cancel-participation', { eventId });
}

/**
 * 获取活动参与者列表
 * @param eventId 活动ID
 */
export function getEventParticipants(eventId: number) {
  return service.get<ApiResponse<any[]>>(`/event/${eventId}/participants`);
}

/**
 * 获取当前用户参与的活动记录
 */
export function getUserParticipations() {
  return service.get<ApiResponse<any[]>>('/event/participations');
} 