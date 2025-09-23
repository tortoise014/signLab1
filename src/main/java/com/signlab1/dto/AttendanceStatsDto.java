package com.signlab1.dto;

import lombok.Data;

/**
 * 签到统计DTO
 */
@Data
public class AttendanceStatsDto {
    
    /**
     * 总签到次数
     */
    private Integer totalAttendance;
    
    /**
     * 签到率（百分比）
     */
    private Double attendanceRate;
}