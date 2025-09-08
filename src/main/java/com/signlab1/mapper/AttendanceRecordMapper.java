package com.signlab1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.signlab1.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 签到记录Mapper接口
 */
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {
}
