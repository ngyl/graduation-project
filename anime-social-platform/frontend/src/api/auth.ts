import service from './axios';
import type { LoginRequest, RegisterRequest, LoginResponse, UserInfo } from '@/types/auth';
import type { ApiResponse } from '@/types/api';

/**
 * 用户注册
 * @param data 注册请求参数
 */
export const register = (data: RegisterRequest) => {
    return service.post<ApiResponse<void>>('/auth/register', data);
};

/**
 * 用户登录
 * @param data 登录请求参数
 */
export const login = (data: LoginRequest) => {
    return service.post<ApiResponse<LoginResponse>>('/auth/login', data);
};

/**
 * 获取当前用户信息
 */
export const getCurrentUser = () => {
    return service.get<ApiResponse<UserInfo>>('/auth/info');
};

/**
 * 用户登出
 */
export const logout = () => {
    return service.post<ApiResponse<void>>('/auth/logout');
}; 