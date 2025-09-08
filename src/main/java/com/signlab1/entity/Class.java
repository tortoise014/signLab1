package com.signlab1.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 班级表 - 存储班级信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("classes")
public class Class {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 班级编号 - 6位数字
     */
    @TableField("class_code")
    private String classCode;
    
    /**
     * 班级名称
     */
    @TableField("class_name")
    private String className;
    
    /**
     * 班级验证码 - 用于学生绑定班级
     */
    @TableField("verification_code")
    private String verificationCode;
    
    /**
     * 班级人数
     */
    @TableField("student_count")
    private Integer studentCount;
    
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