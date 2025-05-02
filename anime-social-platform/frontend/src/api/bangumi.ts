import axios from 'axios';
import type { AxiosInstance, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

// 创建专门用于Bangumi API的axios实例
const bangumiService: AxiosInstance = axios.create({
  baseURL: 'https://api.bgm.tv',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer nNB6OelhWnn5ywGmxBH5KLwSk9lV9fBfaLH3klJO' // 使用提供的token
  }
});

// 响应拦截器
bangumiService.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error) => {
    console.error('Bangumi API请求错误:', error);
    ElMessage.error('获取动漫数据失败，请稍后重试');
    return Promise.reject(error);
  }
);

// 获取每日放送
export const getCalendar = async () => {
  return bangumiService.get('/calendar');
};

// 搜索动漫条目
export const searchAnime = async (keyword: string) => {
  return bangumiService.post('/v0/search/subjects', {
    keyword,
    sort: 'rank',
    filter: {
      type: [2], // 2表示动画
      nsfw: false
    },
    limit: 8
  });
};

// 获取热门动画
export const getHotAnime = async () => {
  return bangumiService.get('/v0/subjects', {
    params: {
      type: 2, // 2表示动画
      limit: 12,
      order: 'rank', // 按排名排序
      year: new Date().getFullYear() - 1,
    }
  });
};

// 获取单个条目详情
export const getAnimeDetail = async (subjectId: number) => {
  return bangumiService.get(`/v0/subjects/${subjectId}`);
};

export default {
  getCalendar,
  searchAnime,
  getHotAnime,
  getAnimeDetail
}; 