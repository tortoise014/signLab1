package com.signlab1.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户表 - 存储学生和老师的基本信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名 - 唯一标识
     */
    @TableField("username")
    private String username;
    
    /**
     * 姓名
     */
    @TableField("name")
    private String name;
    
    /**
     * 密码 - 加密存储
     */
    @TableField("password")
    private String password;
    
    /**
     * 角色：student-学生，teacher-老师，admin-管理员
     */
    @TableField("role")
    private String role;
    
    /**
     * 是否已设置密码：0-未设置，1-已设置
     */
    @TableField("password_set")
    private Integer passwordSet;
    
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