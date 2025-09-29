package com.signlab1.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 多班级课程关联表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("multi_class_courses")
public class MultiClassCourse {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private String courseId;
    
    /**
     * 班级代码
     */
    @TableField("class_code")
    private String classCode;
    
    /**
     * 教师用户名
     */
    @TableField("teacher_username")
    private String teacherUsername;
    
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
