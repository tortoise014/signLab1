package com.signlab1.dto;

import lombok.Data;

/**
 * 扫码签到请求DTO
 */
@Data
public class ScanAttendanceRequest {
    
    /**
     * 二维码数据（Base64编码）
     */
    private String qrData;
}
