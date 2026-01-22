package com.stockmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stockmaster.entity.InboundRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库记录Mapper
 */
@Mapper
public interface InboundRecordMapper extends BaseMapper<InboundRecord> {
}
