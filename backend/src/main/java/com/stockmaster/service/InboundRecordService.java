package com.stockmaster.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stockmaster.entity.InboundRecord;

import java.util.Date;
import java.util.List;

/**
 * 入库记录服务接口
 */
public interface InboundRecordService extends IService<InboundRecord> {
    
    /**
     * 创建入库记录
     */
    boolean createInboundRecord(InboundRecord record);
    
    /**
     * 查询所有入库记录
     */
    List<InboundRecord> getAllRecords();
    
    /**
     * 根据类型查询入库记录
     */
    List<InboundRecord> getRecordsByType(String type);
    
    /**
     * 根据货物ID查询入库记录
     */
    List<InboundRecord> getRecordsByGoodsId(Long goodsId);
    
    /**
     * 根据时间范围查询入库记录
     */
    List<InboundRecord> getRecordsByTimeRange(Date startTime, Date endTime);
    
    /**
     * 更新结算状态
     */
    boolean updateSettlementStatus(Long id, String settlementStatus, String remark);
    
    /**
     * 分页查询入库记录
     */
    IPage<InboundRecord> getRecordsPage(Page<InboundRecord> page, String type);
}
