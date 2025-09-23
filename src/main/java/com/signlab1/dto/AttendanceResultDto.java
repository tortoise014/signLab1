package com.signlab1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 签到结果DTO
 */
@Data
public class AttendanceResultDto {
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 老师姓名
     */
    private String teacherName;
    
    /**
     * 签到时间
     */
    private LocalDateTime attendanceTime;
    
    /**
     * 上课地点
     */
    private String location;
}

