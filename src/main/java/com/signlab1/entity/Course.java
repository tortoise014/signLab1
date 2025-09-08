package com.signlab1.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程表 - 存储课程信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("courses")
public class Course {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID - 格式：KC + 年份后2位 + 6位自增数
     */
    @TableField("course_id")
    private String courseId;
    
    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;
    
    /**
     * 授课老师工号
     */
    @TableField("teacher_code")
    private String teacherCode;
    
    /**
     * 上课班级编号
     */
    @TableField("class_code")
    private String classCode;
    
    /**
     * 上课地点
     */
    @TableField("location")
    private String location;
    
    /**
     * 课程日期
     */
    @TableField("course_date")
    private String courseDate;
    
    /**
     * 上课时间段 - 如：08:00-09:40
     */
    @TableField("time_slot")
    private String timeSlot;
    
    /**
     * 课程周次
     */
    @TableField("week_number")
    private Integer weekNumber;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}