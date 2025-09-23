package com.signlab1.dto;

import lombok.Data;

/**
 * 更新签到请求DTO
 */
@Data
public class UpdateAttendanceRequest {
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 学生学号
     */
    private String studentCode;
    
    /**
     * 签到状态：0-未签到，1-已签到
     */
    private Integer status;
}

