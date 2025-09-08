package com.signlab1.dto;

import com.signlab1.enums.ResponseCode;
import lombok.Data;

/**
 * 通用响应DTO
 */
@Data
public class ApiResponse<T> {
    
    /**
     * 响应码
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
    
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResponseCode.SUCCESS.getCode());
        response.setMessage(ResponseCode.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResponseCode.SUCCESS.getCode());
        response.setMessage(ResponseCode.SUCCESS.getMessage());
        return response;
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResponseCode.SUCCESS.getCode());
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResponseCode.INTERNAL_SERVER_ERROR.getCode());
        response.setMessage(message);
        return response;
    }
    
    /**
     * 失败响应（自定义状态码）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
    
    /**
     * 失败响应（使用枚举）
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(responseCode.getCode());
        response.setMessage(responseCode.getMessage());
        return response;
    }
    
    /**
     * 失败响应（使用枚举，自定义消息）
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(responseCode.getCode());
        response.setMessage(message);
        return response;
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == ResponseCode.SUCCESS.getCode();
    }
}
