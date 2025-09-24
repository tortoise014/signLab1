package com.signlab1.dto;

import lombok.Data;

/**
 * 更新学生信息请求DTO
 */
@Data
public class UpdateStudentRequest {
    
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
}






