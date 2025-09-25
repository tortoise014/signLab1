package com.signlab1.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课堂照片信息DTO
 */
@Data
public class ClassPhotoDto {
    
    /**
     * 照片ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 学生用户名
     */
    private String studentUsername;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 照片文件名
     */
    private String photoName;
    
    /**
     * 照片访问URL
     */
    private String photoUrl;
    
    /**
     * 照片备注
     */
    private String remark;
    
    /**
     * 照片大小（字节）
     */
    private Long fileSize;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
}

