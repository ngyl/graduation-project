import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useUserStore } from '@/stores/user';

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest' // 添加常用的AJAX请求头
  },
  withCredentials: true // 确保发送凭证
});

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 强制设置withCredentials为true，确保每个请求都携带cookie
    config.withCredentials = true;
    
    // 添加调试信息
    console.debug(`正在发送${config.method?.toUpperCase()}请求到: ${config.url}`);
    if (config.data) {
      console.debug('请求数据:', JSON.stringify(config.data, null, 2));
    }
    if (config.params) {
      console.debug('请求参数:', JSON.stringify(config.params, null, 2));
    }

    return config;
  },
  (error) => {
    console.error('Request error: ', error);
    return Promise.reject(new Error(error.message || 'Request failed'));
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 输出Cookie信息，帮助调试
    console.log(`收到来自${response.config.url}的响应，状态: ${response.status}`);
    console.log(response.data);
    console.log(response.data.data);
    
    return response;
  },
  (error: AxiosError) => {
    if (error.response) {
      const { status } = error.response;
      console.error(`请求错误: ${status}`, error.response);
      
      // 详细记录错误信息
      try {
        const responseData = error.response.data as any;
        console.error('错误详情:', {
          status: status,
          url: error.config?.url,
          method: error.config?.method,
          data: responseData,
          message: responseData?.message || error.message
        });
      } catch (e) {
        console.error('解析错误详情失败', e);
      }
      const userStore = useUserStore();      
    
      // 处理401错误（未授权）
      if (status === 401) {
        // 使用pinia store清除用户状态
        userStore.logoutUser();
        
        // 显示提示
        ElMessage.error('登录已过期，请重新登录');
        
        // 跳转到登录页面，并携带重定向信息
        const currentPath = router.currentRoute.value.path;
        if (currentPath !== '/login') {
          router.push({
            path: '/login',
            query: { redirect: currentPath }
          });
        }
      }
      // 处理403错误（禁止访问）
      else if (status === 403) {
        // 判断用户是否已登录
        if (userStore.isLoggedIn) {
        ElMessage.error('您没有权限访问此资源');
        console.error('403错误详情:', error.response.data);
        router.push('/');
        } else {
          // 未登录用户遇到403错误，应该引导其登录
          ElMessage.error('请先登录后再访问此资源');
          console.error('未登录用户403错误:', error.response.data);
          
          // 跳转到登录页面，并携带重定向信息
          const currentPath = router.currentRoute.value.path;
          if (currentPath !== '/login') {
            router.push({
              path: '/login',
              query: { redirect: currentPath }
            });
          }
        }
      }
      // 处理404错误
      else if (status === 404) {
        ElMessage.error('请求的资源不存在');
      }
      // 处理500错误
      else if (status >= 500) {
        ElMessage.error('服务器错误，请稍后重试');
      }
      // 其他错误
      else {
        const responseData = error.response.data as any;
        ElMessage.error(responseData?.message ?? '请求失败');
      }
    } else if (error.request) {
      // 请求发出但没有收到响应
      console.error('网络错误，无法连接到服务器:', error.request);
      ElMessage.error('网络错误，无法连接到服务器');
    } else {
      // 请求配置出错
      console.error('请求配置错误:', error.message);
      ElMessage.error('请求配置错误');
    }
    
    return Promise.reject(error);
  }
);

export default service; 