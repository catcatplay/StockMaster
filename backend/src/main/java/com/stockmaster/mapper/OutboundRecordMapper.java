package com.stockmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stockmaster.entity.OutboundRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库记录Mapper
 */
@Mapper
public interface OutboundRecordMapper extends BaseMapper<OutboundRecord> {
}
