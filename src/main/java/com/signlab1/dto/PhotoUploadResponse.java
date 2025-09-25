package com.signlab1.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 照片上传响应DTO
 */
@Data
public class PhotoUploadResponse {
    
    /**
     * 照片ID
     */
    private Long photoId;
    
    /**
     * 照片文件名
     */
    private String photoName;
    
    /**
     * 照片访问URL
     */
    private String photoUrl;
    
    /**
     * 照片大小（字节）
     */
    private Long fileSize;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 照片备注
     */
    private String remark;
}

