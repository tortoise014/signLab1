package com.signlab1.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 签到记录表 - 存储签到信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("attendance_records")
public class AttendanceRecord {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private String courseId;
    
    /**
     * 学生用户名
     */
    @TableField("student_username")
    private String studentUsername;
    
    /**
     * 签到时间
     */
    @TableField("attendance_time")
    private LocalDateTime attendanceTime;
    
    /**
     * 签到状态：1-正常签到，2-迟到，3-早退
     */
    @TableField("attendance_status")
    private Integer attendanceStatus;
    
    /**
     * 签到IP地址
     */
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
