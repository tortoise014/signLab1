package com.signlab1.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生文档表 - 存储学生导出的Word文档信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("student_documents")
public class StudentDocument {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private String courseId;
    
    /**
     * 学生学号
     */
    @TableField("student_code")
    private String studentCode;
    
    /**
     * 文档文件名
     */
    @TableField("document_name")
    private String documentName;
    
    /**
     * 文档存储路径
     */
    @TableField("document_path")
    private String documentPath;
    
    /**
     * 文档大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;
    
    /**
     * 导出时间
     */
    @TableField(value = "export_time", fill = FieldFill.INSERT)
    private LocalDateTime exportTime;
    
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
