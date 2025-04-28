/**
 * API响应的通用格式
 * 与后端ApiResponse.java保持一致
 */
export interface ApiResponse<T> {
  /**
   * 状态码
   * 200: 成功
   * 400: 参数错误
   * 401: 未授权
   * 403: 禁止访问
   * 404: 资源不存在
   * 500: 服务器错误
   */
  code: number;
  
  /**
   * 响应消息
   */
  message: string;
  
  /**
   * 响应数据
   */
  data: T;
  
  /**
   * 时间戳（毫秒）
   */
  timestamp: number;
}

/**
 * API响应工具类
 */
export const ApiResponseUtil = {
  /**
   * 判断响应是否成功
   * @param response API响应
   * @returns 是否成功
   */
  isSuccess: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 200;
  },

  /**
   * 判断是否未授权
   * @param response API响应
   * @returns 是否未授权
   */
  isUnauthorized: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 401;
  },

  /**
   * 判断是否禁止访问
   * @param response API响应
   * @returns 是否禁止访问
   */
  isForbidden: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 403;
  },

  /**
   * 判断资源是否不存在
   * @param response API响应
   * @returns 资源是否不存在
   */
  isNotFound: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 404;
  },

  /**
   * 判断是否服务器错误
   * @param response API响应
   * @returns 是否服务器错误
   */
  isServerError: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 500;
  }
}; 
