package com.stockmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stockmaster.entity.Goods;
import com.stockmaster.entity.OutboundRecord;
import com.stockmaster.exception.BusinessException;
import com.stockmaster.mapper.OutboundRecordMapper;
import com.stockmaster.service.GoodsService;
import com.stockmaster.service.OutboundRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 出库记录服务实现类
 */
@Service
public class OutboundRecordServiceImpl extends ServiceImpl<OutboundRecordMapper, OutboundRecord> implements OutboundRecordService {

    @Resource
    private GoodsService goodsService;

    private LambdaQueryWrapper<OutboundRecord> buildQueryWrapper(String type, Date startTime, Date endTime, Integer status) {
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(OutboundRecord::getType, type);
        }
        if (startTime != null) {
            wrapper.ge(OutboundRecord::getOutboundTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OutboundRecord::getOutboundTime, endTime);
        }
        wrapper.eq(status != null, OutboundRecord::getStatus, status)
                .orderByDesc(OutboundRecord::getOutboundTime);
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOutboundRecord(OutboundRecord record) {
        // 查询货物信息
        Goods goods = goodsService.getGoodsById(record.getGoodsId());
        if (goods == null) {
            throw new BusinessException("货物不存在");
        }
        
        // 检查库存是否充足
        if (goods.getRemainingStock() < record.getQuantity()) {
            throw new BusinessException("库存不足，当前剩余库存：" + goods.getRemainingStock());
        }
        
        // 填充货物信息
        record.setGoodsName(goods.getName());
        record.setBrand(goods.getBrand());
        record.setModel(goods.getModel());
        record.setType(goods.getType());
        
        record.setStatus(OutboundRecord.STATUS_NORMAL);
        record.setCancelTime(null);

        // 保存出库记录
        boolean saved = this.save(record);
        
        // 更新库存（减少）
        if (saved) {
            goodsService.updateStockQuantity(record.getGoodsId(), -record.getQuantity());
        }
        
        return saved;
    }

    @Override
    public List<OutboundRecord> getAllRecords(Integer status) {
        return this.list(buildQueryWrapper(null, null, null, status));
    }

    @Override
    public List<OutboundRecord> getRecordsByType(String type, Integer status) {
        return this.list(buildQueryWrapper(type, null, null, status));
    }

    @Override
    public List<OutboundRecord> getRecordsByGoodsId(Long goodsId) {
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundRecord::getGoodsId, goodsId)
               .orderByDesc(OutboundRecord::getOutboundTime);
        return this.list(wrapper);
    }

    @Override
    public List<OutboundRecord> getRecordsByTimeRange(Date startTime, Date endTime, Integer status) {
        return this.list(buildQueryWrapper(null, startTime, endTime, status));
    }

    @Override
    public List<OutboundRecord> getRecordsList(String type, Date startTime, Date endTime, Integer status) {
        return this.list(buildQueryWrapper(type, startTime, endTime, status));
    }

    @Override
    public Integer getTotalOutboundQuantityByGoodsId(Long goodsId) {
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundRecord::getGoodsId, goodsId)
                .eq(OutboundRecord::getStatus, OutboundRecord.STATUS_NORMAL);
        List<OutboundRecord> records = this.list(wrapper);
        
        return records.stream()
                .mapToInt(OutboundRecord::getQuantity)
                .sum();
    }
    
    @Override
    public IPage<OutboundRecord> getRecordsPage(Page<OutboundRecord> page, String type, Date startTime, Date endTime, Integer status) {
        return this.page(page, buildQueryWrapper(type, startTime, endTime, status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOutboundRecord(Long id) {
        // 查询出库记录
        OutboundRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("出库记录不存在");
        }

        if (Integer.valueOf(OutboundRecord.STATUS_CANCELED).equals(record.getStatus())) {
            throw new BusinessException("该出库记录已取消，请勿重复操作");
        }

        record.setStatus(OutboundRecord.STATUS_CANCELED);
        record.setCancelTime(java.time.LocalDateTime.now());
        boolean updated = this.lambdaUpdate()
                .eq(OutboundRecord::getId, id)
                .eq(OutboundRecord::getStatus, OutboundRecord.STATUS_NORMAL)
                .set(OutboundRecord::getStatus, OutboundRecord.STATUS_CANCELED)
                .set(OutboundRecord::getCancelTime, record.getCancelTime())
                .update();

        if (!updated) {
            throw new BusinessException("该出库记录已取消，请勿重复操作");
        }

        // 恢复库存（仅恢复剩余库存，不影响总库存）
        goodsService.updateRemainingStockOnly(record.getGoodsId(), record.getQuantity());
        return true;
    }
}
