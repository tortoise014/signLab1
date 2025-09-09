package com.signlab1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.signlab1.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

