package com.signlab1.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生班级关联表 - 存储学生与班级的绑定关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("student_class_relations")
public class StudentClassRelation {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生学号
     */
    @TableField("student_code")
    private String studentCode;
    
    /**
     * 班级编号
     */
    @TableField("class_code")
    private String classCode;
    
    /**
     * 绑定时间
     */
    @TableField(value = "bind_time", fill = FieldFill.INSERT)
    private LocalDateTime bindTime;
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}

