package com.stockmaster.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stockmaster.entity.Goods;

import java.util.List;

/**
 * 货物信息服务接口
 */
public interface GoodsService extends IService<Goods> {
    
    /**
     * 添加货物
     */
    boolean addGoods(Goods goods);
    
    /**
     * 更新货物信息
     */
    boolean updateGoods(Goods goods);
    
    /**
     * 删除货物
     */
    boolean deleteGoods(Long id);
    
    /**
     * 根据ID查询货物
     */
    Goods getGoodsById(Long id);
    
    /**
     * 查询所有货物列表
     */
    List<Goods> getAllGoods();
    
    /**
     * 根据类型查询货物列表
     */
    List<Goods> getGoodsByType(String type);
    
    /**
     * 根据名称模糊查询
     */
    List<Goods> searchGoodsByName(String name);
    
    /**
     * 更新库存数量
     */
    boolean updateStockQuantity(Long id, Integer quantity);
    
    /**
     * 分页查询货物
     */
    IPage<Goods> getGoodsPage(Page<Goods> page, String type, String name);
}
