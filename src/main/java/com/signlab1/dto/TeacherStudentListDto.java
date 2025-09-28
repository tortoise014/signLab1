package com.signlab1.dto;

import lombok.Data;
import java.util.List;

/**
 * 老师学生列表响应DTO
 */
@Data
public class TeacherStudentListDto {
    
    /**
     * 学生列表
     */
    private List<StudentInfoDto> students;
    
    /**
     * 统计信息
     */
    private StudentStats stats;
    
    /**
     * 学生统计信息
     */
    @Data
    public static class StudentStats {
        /**
         * 总学生数
         */
        private int totalCount;
        
        /**
         * 本班学生数
         */
        private int classStudentCount;
        
        /**
         * 跨班签到学生数
         */
        private int crossClassAttendeeCount;
        
        /**
         * 已签到学生数
         */
        private int attendedCount;
        
        /**
         * 未签到学生数
         */
        private int notAttendedCount;
        
        /**
         * 签到率（本班学生）
         */
        private double attendanceRate;
    }
}
