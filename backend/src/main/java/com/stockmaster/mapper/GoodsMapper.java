package com.stockmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stockmaster.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 货物信息Mapper
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    @Select("SELECT IFNULL(SUM(total_stock), 0) FROM goods WHERE type = #{type}")
    Integer sumTotalStock(String type);
}
