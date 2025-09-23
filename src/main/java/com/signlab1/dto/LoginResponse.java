package com.signlab1.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 学号/工号
     */
    private String username;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 是否首次登录
     */
    private Boolean isFirstLogin;
}

