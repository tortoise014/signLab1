package com.signlab1.dto;

import lombok.Data;

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
}







