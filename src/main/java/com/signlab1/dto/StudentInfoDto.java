package com.signlab1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生信息DTO
 */
@Data
public class StudentInfoDto {
    
    /**
     * 学生学号
     */
    private String studentCode;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 班级编号
     */
    private String classCode;
    
    /**
     * 班级名称
     */
    private String className;
    
    /**
     * 学生类型：CLASS_STUDENT(本班学生), CROSS_CLASS_ATTENDEE(跨班签到学生)
     */
    private String studentType;
    
    /**
     * 最近签到时间
     */
    private LocalDateTime lastAttendanceTime;
    
    /**
     * 签到状态：1-已签到，0-未签到
     */
    private Integer attendanceStatus;
    
    /**
     * 签到课程ID
     */
    private String courseId;
    
    /**
     * 签到课程名称
     */
    private String courseName;
}







