package com.signlab1.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前课程信息DTO
 */
@Data
public class CurrentCourseDto {
    
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
     * 课程状态：1-即将开始，2-正在进行，3-已结束
     */
    private Integer courseStatus;
    
    /**
     * 状态描述
     */
    private String statusDescription;
    
    /**
     * 距离开始/结束的分钟数
     */
    private Integer minutesRemaining;
    
    /**
     * 签到人数
     */
    private Integer attendanceCount;
    
    /**
     * 班级总人数
     */
    private Integer totalStudents;
    
    /**
     * 签到率
     */
    private Double attendanceRate;
}
