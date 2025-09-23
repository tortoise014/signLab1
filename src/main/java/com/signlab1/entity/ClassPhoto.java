package com.signlab1.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课堂照片表 - 存储学生拍摄的课堂照片
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("class_photos")
public class ClassPhoto {
    
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
     * 照片文件名
     */
    @TableField("photo_name")
    private String photoName;
    
    /**
     * 照片存储路径
     */
    @TableField("photo_path")
    private String photoPath;
    
    /**
     * 照片备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 照片大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;
    
    /**
     * 上传时间
     */
    @TableField(value = "upload_time", fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
    
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

