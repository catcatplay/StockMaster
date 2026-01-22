package com.stockmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stockmaster.entity.Goods;
import com.stockmaster.mapper.GoodsMapper;
import com.stockmaster.service.GoodsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 货物信息服务实现类
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Override
    public boolean addGoods(Goods goods) {
        if (goods.getStockQuantity() == null) {
            goods.setStockQuantity(0);
        }
        return this.save(goods);
    }

    @Override
    public boolean updateGoods(Goods goods) {
        return this.updateById(goods);
    }

    @Override
    public boolean deleteGoods(Long id) {
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
        Goods goods = this.getById(id);
        if (goods == null) {
            return false;
        }
        goods.setStockQuantity(goods.getStockQuantity() + quantity);
        return this.updateById(goods);
    }
    
    @Override
    public IPage<Goods> getGoodsPage(Page<Goods> page, String type) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Goods::getType, type);
        }
        wrapper.orderByDesc(Goods::getCreateTime);
        return this.page(page, wrapper);
    }
}
