package com.stockmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stockmaster.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 货物信息Mapper
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
