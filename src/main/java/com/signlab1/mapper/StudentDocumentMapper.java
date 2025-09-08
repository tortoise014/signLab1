package com.signlab1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.signlab1.entity.StudentDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生文档Mapper接口
 */
@Mapper
public interface StudentDocumentMapper extends BaseMapper<StudentDocument> {
}
