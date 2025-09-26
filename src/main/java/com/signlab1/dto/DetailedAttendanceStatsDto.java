package com.signlab1.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 详细签到统计DTO - 包含本班级完整签到情况
 */
@Data
public class DetailedAttendanceStatsDto {
    
    /**
     * 课程信息
     */
    private CourseInfo courseInfo;
    
    /**
     * 班级信息
     */
    private ClassInfo classInfo;
    
    /**
     * 本班级学生签到情况
     */
    private List<StudentAttendanceInfo> classStudents;
    
    /**
     * 非本班级但已签到的学生
     */
    private List<StudentAttendanceInfo> otherClassStudents;
    
    /**
     * 统计汇总
     */
    private AttendanceSummary summary;
    
    /**
     * 课程信息
     */
    @Data
    public static class CourseInfo {
        private String courseId;
        private String courseName;
        private String teacherName;
        private String location;
        private String courseDate;
        private String timeSlot;
    }
    
    /**
     * 班级信息
     */
    @Data
    public static class ClassInfo {
        private String classCode;
        private String className;
        private Integer totalStudentCount; // 班级总人数
        private Integer attendedCount; // 本班级已签到人数
        private Integer absentCount; // 本班级未签到人数
    }
    
    /**
     * 学生签到信息
     */
    @Data
    public static class StudentAttendanceInfo {
        private String studentCode;
        private String studentName;
        private String classCode;
        private String className;
        private Boolean isAttended; // 是否已签到
        private LocalDateTime attendanceTime; // 签到时间
        private Integer attendanceStatus; // 签到状态：1-正常，2-迟到，3-早退
        private String statusText; // 状态文本
        private Boolean isFromThisClass; // 是否来自本班级
    }
    
    /**
     * 统计汇总
     */
    @Data
    public static class AttendanceSummary {
        private Integer totalAttended; // 总签到人数
        private Integer classAttended; // 本班级签到人数
        private Integer otherClassAttended; // 其他班级签到人数
        private Integer classAbsent; // 本班级未签到人数
        private Double attendanceRate; // 本班级签到率
        private Integer normalCount; // 正常签到人数
        private Integer lateCount; // 迟到人数
        private Integer earlyLeaveCount; // 早退人数
    }
}
