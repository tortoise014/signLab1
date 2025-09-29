package com.signlab1.dto;

import lombok.Data;
import java.util.List;

/**
 * 多班级课程关联请求DTO
 */
@Data
public class MultiClassCourseRequest {
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 班级代码列表
     */
    private List<String> classCodes;
    
    /**
     * 教师用户名
     */
    private String teacherUsername;
}
