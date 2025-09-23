package com.signlab1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 签到记录DTO
 */
@Data
public class AttendanceRecordDto {
    
    /**
     * 课程ID
     */
    private String courseId;
    
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
    
    /**
     * 签到状态：0-未签到，1-已签到
     */
    private Integer status;
}



