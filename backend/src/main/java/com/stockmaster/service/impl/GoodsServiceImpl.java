package com.stockmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stockmaster.entity.Goods;
import com.stockmaster.entity.InboundRecord;
import com.stockmaster.entity.OutboundRecord;
import com.stockmaster.mapper.GoodsMapper;
import com.stockmaster.mapper.InboundRecordMapper;
import com.stockmaster.mapper.OutboundRecordMapper;
import com.stockmaster.service.GoodsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 货物信息服务实现类
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Resource
    private InboundRecordMapper inboundRecordMapper;

    @Resource
    private OutboundRecordMapper outboundRecordMapper;
    @Resource
    private GoodsMapper goodsMapper;

    @Override
    public boolean addGoods(Goods goods) {
        if (goods.getRemainingStock() == null) {
            goods.setRemainingStock(0);
        }
        if (goods.getTotalStock() == null) {
            goods.setTotalStock(0);
        }
        return this.save(goods);
    }

    @Override
    public boolean updateGoods(Goods goods) {
        return this.updateById(goods);
    }

    @Override
    public boolean deleteGoods(Long id) {
        // 校验是否存在入库记录
        LambdaQueryWrapper<InboundRecord> inboundWrapper = new LambdaQueryWrapper<>();
        inboundWrapper.eq(InboundRecord::getGoodsId, id);
        Integer inboundCount = inboundRecordMapper.selectCount(inboundWrapper);
        if (inboundCount > 0) {
            throw new RuntimeException("该货物存在入库记录，不允许删除");
        }

        // 校验是否存在出库记录
        LambdaQueryWrapper<OutboundRecord> outboundWrapper = new LambdaQueryWrapper<>();
        outboundWrapper.eq(OutboundRecord::getGoodsId, id);
        Integer outboundCount = outboundRecordMapper.selectCount(outboundWrapper);
        if (outboundCount > 0) {
            throw new RuntimeException("该货物存在出库记录，不允许删除");
        }

        return this.removeById(id);
    }

    @Override
    public Goods getGoodsById(Long id) {
        return this.getById(id);
    }

    @Override
    public List<Goods> getAllGoods() {
        return this.list();
    }

    @Override
    public List<Goods> getGoodsByType(String type) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getType, type);
        return this.list(wrapper);
    }

    @Override
    public List<Goods> searchGoodsByName(String name) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Goods::getName, name);
        return this.list(wrapper);
    }

    @Override
    public boolean updateStockQuantity(Long id, Integer quantity) {
        final int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            Goods goods = getById(id);
            if (goods == null) {
                return false;
            }

            int remaining = goods.getRemainingStock() != null ? goods.getRemainingStock() : 0;
            int newRemaining = remaining + quantity;

            // 出库校验
            if (quantity < 0 && newRemaining < 0) {
                throw new RuntimeException("库存不足，无法出库");
            }

            LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Goods::getId, id)
                    .eq(Goods::getRemainingStock, goods.getRemainingStock())
                    .set(Goods::getRemainingStock, newRemaining);

            // 入库才增加总库存
            if (quantity > 0) {
                int total = goods.getTotalStock() != null ? goods.getTotalStock() : 0;
                wrapper.set(Goods::getTotalStock, total + quantity);
            }

            if (this.update(wrapper)) {
                return true;
            }
        }

        throw new RuntimeException("系统繁忙，库存更新失败，请重试");
    }


    @Override
    public IPage<Goods> getGoodsPage(Page<Goods> page, String type, String name) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Goods::getType, type);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(Goods::getName, name);
        }
        wrapper.orderByDesc(Goods::getTotalStock);
        return this.page(page, wrapper);
    }

    @Override
    public Integer getTotalStock(String type) {
        return goodsMapper.sumTotalStock(type);
    }

    @Override
    public List<Goods> getGoodsList(String type, String name) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Goods::getType, type);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(Goods::getName, name);
        }
        wrapper.orderByDesc(Goods::getTotalStock);
        return this.list(wrapper);
    }
}
