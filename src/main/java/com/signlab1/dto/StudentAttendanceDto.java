package com.signlab1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生签到状态DTO（老师端使用）
 */
@Data
public class StudentAttendanceDto {
    
    /**
     * 学生学号
     */
    private String studentCode;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 签到状态：0-未签到，1-已签到
     */
    private Integer attendanceStatus;
    
    /**
     * 签到时间
     */
    private LocalDateTime attendanceTime;
}



