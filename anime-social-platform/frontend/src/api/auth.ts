import axios from 'axios';
import type { LoginRequest, RegisterRequest, LoginResponse, UserInfo, ApiResponse } from '@/types/auth';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    withCredentials: true, // 确保发送凭证
    timeout: 5000
});

// 用户注册
export const register = (data: RegisterRequest) => {
    return api.post<ApiResponse<void>>('/auth/register', data);
};

// 用户登录
export const login = (data: LoginRequest) => {
    return api.post<ApiResponse<LoginResponse>>('/auth/login', data);
};

// 获取当前用户信息
export const getCurrentUser = () => {
    return api.get<ApiResponse<UserInfo>>('/auth/info');
};

// 用户登出
export const logout = () => {
    return api.post<ApiResponse<void>>('/auth/logout');
}; 