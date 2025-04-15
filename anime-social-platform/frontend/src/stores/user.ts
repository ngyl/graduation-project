import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { UserInfo } from '@/types/auth';
import { login, register, getCurrentUser, logout } from '@/api/auth';

export const useUserStore = defineStore('user', () => {
    const user = ref<UserInfo | null>(null);
    const token = ref<string>('');

    // 计算属性
    const isLoggedIn = computed(() => !!user.value);
    const userInfo = computed(() => user.value || {
        id: 0,
        username: '',
        avatar: '',
        bio: '',
        isAdmin: false
    });

    // 登录
    const loginUser = async (username: string, password: string) => {
        const res = await login({ username, password });
        if (res.data.code === 200) {
            user.value = res.data.data.user;
            token.value = res.data.data.token;
            return true;
        }
        return false;
    };

    // 注册
    const registerUser = async (username: string, password: string, confirmPassword: string) => {
        const res = await register({ username, password, confirmPassword });
        return res.data.code === 200;
    };

    // 获取当前用户信息
    const fetchUserInfo = async () => {
        const res = await getCurrentUser();
        if (res.data.code === 200) {
            user.value = res.data.data;
            return true;
        }
        return false;
    };

    // 登出
    const logoutUser = async () => {
        await logout();
        user.value = null;
        token.value = '';
    };

    return {
        user,
        token,
        isLoggedIn,
        userInfo,
        loginUser,
        registerUser,
        fetchUserInfo,
        logoutUser
    };
}); 