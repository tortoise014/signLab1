package com.signlab1.dto;

import lombok.Data;

/**
 * 设置密码请求DTO
 */
@Data
public class SetPasswordRequest {
    
    /**
     * 学号/工号
     */
    private String username;
    
    /**
     * 新密码
     */
    private String password;
    
    /**
     * 确认密码
     */
    private String confirmPassword;
}

