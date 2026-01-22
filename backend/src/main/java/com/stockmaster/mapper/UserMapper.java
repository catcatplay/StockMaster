package com.stockmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stockmaster.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
