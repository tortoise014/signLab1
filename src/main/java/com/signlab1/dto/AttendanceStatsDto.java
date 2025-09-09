package com.signlab1.dto;

import lombok.Data;

/**
 * 签到统计DTO
 */
@Data
public class AttendanceStatsDto {
    
    /**
     * 应到人数
     */
    private Integer totalCount;
    
    /**
     * 已签到人数
     */
    private Integer attendedCount;
    
    /**
     * 未签到人数
     */
    private Integer absentCount;
    
    /**
     * 签到率
     */
    private Double attendanceRate;
}

