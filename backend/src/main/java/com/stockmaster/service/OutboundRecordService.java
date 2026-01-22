package com.stockmaster.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stockmaster.entity.OutboundRecord;

import java.util.Date;
import java.util.List;

/**
 * 出库记录服务接口
 */
public interface OutboundRecordService extends IService<OutboundRecord> {
    
    /**
     * 创建出库记录
     */
    boolean createOutboundRecord(OutboundRecord record);
    
    /**
     * 查询所有出库记录
     */
    List<OutboundRecord> getAllRecords();
    
    /**
     * 根据类型查询出库记录
     */
    List<OutboundRecord> getRecordsByType(String type);
    
    /**
     * 根据货物ID查询出库记录
     */
    List<OutboundRecord> getRecordsByGoodsId(Long goodsId);
    
    /**
     * 根据时间范围查询出库记录
     */
    List<OutboundRecord> getRecordsByTimeRange(Date startTime, Date endTime);
    
    /**
     * 统计货物总出库量
     */
    Integer getTotalOutboundQuantityByGoodsId(Long goodsId);
    
    /**
     * 分页查询出库记录
     */
    IPage<OutboundRecord> getRecordsPage(Page<OutboundRecord> page, String type);
}
