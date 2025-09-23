package com.signlab1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 班级信息DTO
 */
@Data
public class ClassInfoDto {
    
    /**
     * 班级编号
     */
    private String classCode;
    
    /**
     * 班级名称
     */
    private String className;
    
    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;
}

