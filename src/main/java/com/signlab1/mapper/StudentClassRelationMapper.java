package com.signlab1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.signlab1.entity.StudentClassRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生班级关联Mapper接口
 */
@Mapper
public interface StudentClassRelationMapper extends BaseMapper<StudentClassRelation> {
}
