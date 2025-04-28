import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { UserInfo } from '@/types/auth';
import { login, register, getCurrentUser, logout } from '@/api/auth';

export const useUserStore = defineStore('user', () => {
    // 用户状态
    const user = ref<UserInfo | null>(null);

    // 计算属性
    const isLoggedIn = computed(() => !!user.value);
    const isAdmin = computed(() => !!user.value?.isAdmin);
    const userInfo = computed(() => user.value ?? {
        id: 0,
        username: '',
        avatar: '',
        bio: '',
        isAdmin: false
    });

    // 登录
    const loginUser = async (username: string, password: string) => {
        try {
            const res = await login({ username, password });
            if (res.data.code === 200) {
                user.value = res.data.data.user;
                
                // 登录成功后设置会话标记
                localStorage.setItem('hasSession', 'true');
                
                // 登录后立即记录日志
                console.log('登录成功，用户信息:', user.value.username);
                return true;
            }
            return false;
        } catch (error: any) {
            console.error('登录失败:', error?.message ?? '未知错误');
            return false;
        }
    };

    // 注册
    const registerUser = async (username: string, password: string, confirmPassword: string) => {
        const res = await register({ username, password, confirmPassword });
        return res.data.code === 200;
    };

    // 获取当前用户信息
    const fetchUserInfo = async () => {
        try {
            console.log('正在获取用户信息...');
            const res = await getCurrentUser();
            if (res.data.code === 200) {
                user.value = res.data.data;
                // 获取用户信息成功，设置会话标记
                localStorage.setItem('hasSession', 'true');
                console.log('用户信息获取成功:', user.value.username);
                return true;
            }
            console.warn('获取用户信息API返回非成功状态:', res.data);
            // 如果用户未登录，清除状态
            user.value = null;
            // 清除会话标记
            localStorage.removeItem('hasSession');
            return false;
        } catch (error: any) {
            // 如果获取失败，可能是session过期，清除登录状态
            console.error('获取用户信息失败:', error?.message ?? '未知错误', 
                          '状态码:', error?.response?.status);
            
            // 清除用户状态
            user.value = null;
            // 清除会话标记
            localStorage.removeItem('hasSession');
            return false;
        }
    };

    // 登出
    const logoutUser = async () => {
        try {
            await logout();
        } catch (error: any) {
            console.error('登出请求失败:', error?.message ?? '未知错误');
        } finally {
            // 无论请求是否成功，都清除本地状态
            user.value = null;
            // 清除会话标记
            localStorage.removeItem('hasSession');
        }
    };

    return {
        user,
        isLoggedIn,
        isAdmin,
        userInfo,
        loginUser,
        registerUser,
        fetchUserInfo,
        logoutUser
    };
}); 