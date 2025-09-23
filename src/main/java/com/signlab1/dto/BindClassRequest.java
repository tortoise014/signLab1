package com.signlab1.dto;

import lombok.Data;

/**
 * 绑定班级请求DTO
 */
@Data
public class BindClassRequest {
    
    /**
     * 学生用户名
     */
    private String studentCode;
    
    /**
     * 班级验证码
     */
    private String verificationCode;
}


