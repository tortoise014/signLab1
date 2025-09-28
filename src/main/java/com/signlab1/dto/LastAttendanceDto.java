package com.signlab1.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 最近签到记录DTO
 */
@Data
public class LastAttendanceDto {
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 授课老师用户名
     */
    private String teacherUsername;
    
    /**
     * 授课老师姓名
     */
    private String teacherName;
    
    /**
     * 班级代码
     */
    private String classCode;
    
    /**
     * 班级名称
     */
    private String className;
    
    /**
     * 上课地点
     */
    private String location;
    
    /**
     * 课程日期
     */
    private String courseDate;
    
    /**
     * 上课时间段
     */
    private String timeSlot;
    
    /**
     * 签到时间
     */
    private LocalDateTime attendanceTime;
    
    /**
     * 签到状态：1-正常签到，2-迟到，3-早退
     */
    private Integer attendanceStatus;
    
    /**
     * 签到状态描述
     */
    private String statusDescription;
    
    /**
     * 签到IP地址
     */
    private String ipAddress;
    
    /**
     * 距离签到的时间描述
     */
    private String timeDescription;
}
