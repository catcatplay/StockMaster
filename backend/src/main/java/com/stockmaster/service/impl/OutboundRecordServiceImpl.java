package com.stockmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stockmaster.entity.Goods;
import com.stockmaster.entity.OutboundRecord;
import com.stockmaster.mapper.OutboundRecordMapper;
import com.stockmaster.service.GoodsService;
import com.stockmaster.service.OutboundRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 出库记录服务实现类
 */
@Service
public class OutboundRecordServiceImpl extends ServiceImpl<OutboundRecordMapper, OutboundRecord> implements OutboundRecordService {

    @Autowired
    private GoodsService goodsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOutboundRecord(OutboundRecord record) {
        // 查询货物信息
        Goods goods = goodsService.getGoodsById(record.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("货物不存在");
        }
        
        // 检查库存是否充足
        if (goods.getStockQuantity() < record.getQuantity()) {
            throw new RuntimeException("库存不足，当前库存：" + goods.getStockQuantity());
        }
        
        // 填充货物信息
        record.setGoodsName(goods.getName());
        record.setBrand(goods.getBrand());
        record.setModel(goods.getModel());
        record.setType(goods.getType());
        
        // 保存出库记录
        boolean saved = this.save(record);
        
        // 更新库存（减少）
        if (saved) {
            goodsService.updateStockQuantity(record.getGoodsId(), -record.getQuantity());
        }
        
        return saved;
    }

    @Override
    public List<OutboundRecord> getAllRecords() {
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OutboundRecord::getOutboundTime);
        return this.list(wrapper);
    }

    @Override
    public List<OutboundRecord> getRecordsByType(String type) {
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundRecord::getType, type)
               .orderByDesc(OutboundRecord::getOutboundTime);
        return this.list(wrapper);
    }

    @Override
    public List<OutboundRecord> getRecordsByGoodsId(Long goodsId) {
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundRecord::getGoodsId, goodsId)
               .orderByDesc(OutboundRecord::getOutboundTime);
        return this.list(wrapper);
    }

    @Override
    public List<OutboundRecord> getRecordsByTimeRange(Date startTime, Date endTime) {
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(OutboundRecord::getOutboundTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OutboundRecord::getOutboundTime, endTime);
        }
        wrapper.orderByDesc(OutboundRecord::getOutboundTime);
        return this.list(wrapper);
    }

    @Override
    public Integer getTotalOutboundQuantityByGoodsId(Long goodsId) {
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundRecord::getGoodsId, goodsId);
        List<OutboundRecord> records = this.list(wrapper);
        
        return records.stream()
                .mapToInt(OutboundRecord::getQuantity)
                .sum();
    }
    
    @Override
    public IPage<OutboundRecord> getRecordsPage(Page<OutboundRecord> page, String type) {
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(OutboundRecord::getType, type);
        }
        wrapper.orderByDesc(OutboundRecord::getOutboundTime);
        return this.page(page, wrapper);
    }
}
