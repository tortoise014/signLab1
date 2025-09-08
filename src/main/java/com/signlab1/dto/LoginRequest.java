package com.signlab1.dto;

import lombok.Data;

/**
 * 用户登录请求DTO
 */
@Data
public class LoginRequest {
    
    /**
     * 学号/工号
     */
    private String userCode;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 是否记住账号
     */
    private Boolean rememberMe;
}
