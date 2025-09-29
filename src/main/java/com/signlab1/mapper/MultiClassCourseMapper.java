package com.signlab1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.signlab1.entity.MultiClassCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 多班级课程关联Mapper
 */
@Mapper
public interface MultiClassCourseMapper extends BaseMapper<MultiClassCourse> {
}
