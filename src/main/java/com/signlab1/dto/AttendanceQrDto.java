package com.signlab1.dto;

import lombok.Data;

/**
 * 签到二维码DTO
 */
@Data
public class AttendanceQrDto {
    
    /**
     * 二维码内容
     */
    private String qrContent;
    
    /**
     * 二维码图片Base64
     */
    private String qrImage;
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 生成时间戳
     */
    private Long timestamp;
    
    /**
     * 剩余有效时间（秒）
     */
    private Integer remainingTime;
}
