package com.stockmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stockmaster.entity.Goods;
import com.stockmaster.entity.InboundRecord;
import com.stockmaster.mapper.InboundRecordMapper;
import com.stockmaster.service.GoodsService;
import com.stockmaster.service.InboundRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 入库记录服务实现类
 */
@Service
public class InboundRecordServiceImpl extends ServiceImpl<InboundRecordMapper, InboundRecord> implements InboundRecordService {

    @Resource
    private GoodsService goodsService;

    @Override
    public List<InboundRecord> getRecordsList(String type, Date startTime, Date endTime) {
        LambdaQueryWrapper<InboundRecord> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(InboundRecord::getType, type);
        }
        if (startTime != null) {
            wrapper.ge(InboundRecord::getInboundTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(InboundRecord::getInboundTime, endTime);
        }
        wrapper.orderByDesc(InboundRecord::getInboundTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createInboundRecord(InboundRecord record) {
        // 查询货物信息
        Goods goods = goodsService.getGoodsById(record.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("货物不存在");
        }
        
        // 填充货物信息
        record.setGoodsName(goods.getName());
        record.setBrand(goods.getBrand());
        record.setModel(goods.getModel());
        record.setType(goods.getType());
        
        // 保存入库记录
        boolean saved = this.save(record);
        
        // 更新库存（增加）
        if (saved) {
            goodsService.updateStockQuantity(record.getGoodsId(), record.getQuantity());
        }
        
        return saved;
    }

    @Override
    public List<InboundRecord> getAllRecords() {
        LambdaQueryWrapper<InboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InboundRecord::getInboundTime);
        return this.list(wrapper);
    }

    @Override
    public List<InboundRecord> getRecordsByType(String type) {
        LambdaQueryWrapper<InboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InboundRecord::getType, type)
               .orderByDesc(InboundRecord::getInboundTime);
        return this.list(wrapper);
    }

    @Override
    public List<InboundRecord> getRecordsByGoodsId(Long goodsId) {
        LambdaQueryWrapper<InboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InboundRecord::getGoodsId, goodsId)
               .orderByDesc(InboundRecord::getInboundTime);
        return this.list(wrapper);
    }

    @Override
    public List<InboundRecord> getRecordsByTimeRange(Date startTime, Date endTime) {
        LambdaQueryWrapper<InboundRecord> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(InboundRecord::getInboundTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(InboundRecord::getInboundTime, endTime);
        }
        wrapper.orderByDesc(InboundRecord::getInboundTime);
        return this.list(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSettlementStatus(Long id, String settlementStatus, String remark) {
        InboundRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("入库记录不存在");
        }
        record.setSettlementStatus(settlementStatus);
        if (remark != null && !remark.isEmpty()) {
            record.setRemark(remark);
        }
        return this.updateById(record);
    }
    
    @Override
    public IPage<InboundRecord> getRecordsPage(Page<InboundRecord> page, String type, Date startTime, Date endTime) {
        LambdaQueryWrapper<InboundRecord> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(InboundRecord::getType, type);
        }
        if (startTime != null) {
            wrapper.ge(InboundRecord::getInboundTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(InboundRecord::getInboundTime, endTime);
        }
        wrapper.orderByDesc(InboundRecord::getInboundTime);
        return this.page(page, wrapper);
    }
}
