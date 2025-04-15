package com.animesocial.platform.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一API响应结果封装
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 构造函数
     * @param code 状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功返回结果
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null);
    }
    
    /**
     * 成功返回结果
     * @param data 返回数据
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }
    
    /**
     * 成功返回结果
     * @param message 提示信息
     * @param data 返回数据
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
    
    /**
     * 失败返回结果
     * @param message 提示信息
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> failed(String message) {
        return new ApiResponse<>(400, message, null);
    }
    
    /**
     * 失败返回结果
     * @param code 状态码
     * @param message 提示信息
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> failed(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    /**
     * 参数验证失败返回结果
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> validateFailed() {
        return new ApiResponse<>(400, "参数检验失败", null);
    }
    
    /**
     * 参数验证失败返回结果
     * @param message 提示信息
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> validateFailed(String message) {
        return new ApiResponse<>(400, message, null);
    }
    
    /**
     * 未登录返回结果
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(401, "暂未登录或token已经过期", null);
    }
    
    /**
     * 无权限返回结果
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> forbidden() {
        return new ApiResponse<>(403, "没有相关权限", null);
    }
    
    /**
     * 资源不存在返回结果
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> notFound() {
        return new ApiResponse<>(404, "资源不存在", null);
    }
    
    /**
     * 服务器错误返回结果
     * @param <T> 数据类型
     * @return ApiResponse对象
     */
    public static <T> ApiResponse<T> serverError() {
        return new ApiResponse<>(500, "服务器内部错误", null);
    }
} 