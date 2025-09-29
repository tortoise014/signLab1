package com.signlab1.dto;

import lombok.Data;
import java.util.List;

/**
 * 多班级课程信息DTO
 */
@Data
public class MultiClassCourseInfoDto {
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 关联的班级列表
     */
    private List<ClassInfoDto> classes;
    
    /**
     * 是否为多班级课程
     */
    private Boolean isMultiClass;
}
