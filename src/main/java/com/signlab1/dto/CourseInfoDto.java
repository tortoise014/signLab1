package com.signlab1.dto;

import lombok.Data;

/**
 * 课程信息DTO
 */
@Data
public class CourseInfoDto {
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 上课班级
     */
    private String className;
    
    /**
     * 班级代码
     */
    private String classCode;
    
    /**
     * 教师用户名
     */
    private String teacherUsername;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 上课时间段
     */
    private String timeSlot;
    
    /**
     * 上课地点
     */
    private String location;
    
    /**
     * 课程日期
     */
    private String courseDate;
    
    /**
     * 是否可发起签到
     */
    private Boolean canStartAttendance;
    
    /**
     * 学生文档数量
     */
    private Integer documentCount;
    
    /**
     * 是否可查看文档
     */
    private Boolean canViewDocuments;
}

