package com.signlab1.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 照片上传请求DTO
 */
@Data
public class PhotoUploadRequest {
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 照片备注
     */
    private String remark;
}

