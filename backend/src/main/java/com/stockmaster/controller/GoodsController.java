package com.stockmaster.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stockmaster.common.Result;
import com.stockmaster.config.ExcelStyleConfig;
import com.stockmaster.dto.GoodsDTO;
import com.stockmaster.dto.GoodsExportDTO;
import com.stockmaster.entity.Goods;
import com.stockmaster.entity.Role;
import com.stockmaster.service.GoodsService;
import com.stockmaster.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 货物信息控制器
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RoleService roleService;

    /**
     * 判断是否为出库员角色
     */
    private boolean isOutboundStaff(HttpServletRequest request) {
        Long roleId = (Long) request.getAttribute("roleId");
        if (roleId != null) {
            Role role = roleService.getById(roleId);
            return role != null && "出库员".equals(role.getRoleName());
        }
        return false;
    }

    /**
     * 将Goods转换为GoodsDTO（不包含单价）
     */
    private GoodsDTO convertToDTO(Goods goods) {
        GoodsDTO dto = new GoodsDTO();
        BeanUtils.copyProperties(goods, dto);
        return dto;
    }

    /**
     * 添加货物
     */
    @PostMapping("/add")
    public Result<Boolean> addGoods(@RequestBody Goods goods) {
        try {
            boolean success = goodsService.addGoods(goods);
            return success ? Result.success("添加成功", true) : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新货物
     */
    @PutMapping("/update")
    public Result<Boolean> updateGoods(@RequestBody Goods goods) {
        try {
            boolean success = goodsService.updateGoods(goods);
            return success ? Result.success("更新成功", true) : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除货物
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteGoods(@PathVariable Long id) {
        try {
            boolean success = goodsService.deleteGoods(id);
            return success ? Result.success("删除成功", true) : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询单个货物
     */
    @GetMapping("/get/{id}")
    public Result<Goods> getGoods(@PathVariable Long id) {
        try {
            Goods goods = goodsService.getGoodsById(id);
            return goods != null ? Result.success(goods) : Result.error("货物不存在");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有货物
     */
    @GetMapping("/list")
    public Result<?> getAllGoods(@RequestParam(required = false) String type, HttpServletRequest request) {
        try {
            List<Goods> list;
            if (type != null && !type.isEmpty()) {
                list = goodsService.getGoodsByType(type);
            } else {
                list = goodsService.getAllGoods();
            }
            
            // 如果是出库员，过滤掉单价字段
//            if (isOutboundStaff(request)) {
//                List<GoodsDTO> dtoList = list.stream()
//                    .map(this::convertToDTO)
//                    .collect(Collectors.toList());
//                return Result.success(dtoList);
//            }
            
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询货物
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getGoodsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            HttpServletRequest request) {
        try {
            Page<Goods> page = new Page<>(current, size);
            IPage<Goods> goodsPage = goodsService.getGoodsPage(page, type, name);
            
            Map<String, Object> result = new HashMap<>();
            
            // 如果是出库员，过滤掉单价字段
            if (isOutboundStaff(request)) {
                List<GoodsDTO> dtoList = goodsPage.getRecords().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
                result.put("records", dtoList);
            } else {
                result.put("records", goodsPage.getRecords());
            }
            
            result.put("total", goodsPage.getTotal());
            result.put("current", goodsPage.getCurrent());
            result.put("size", goodsPage.getSize());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据名称搜索货物
     */
    @GetMapping("/search")
    public Result<?> searchGoods(@RequestParam String name, HttpServletRequest request) {
        try {
            List<Goods> list = goodsService.searchGoodsByName(name);
            
            // 如果是出库员，过滤掉单价字段
            if (isOutboundStaff(request)) {
                List<GoodsDTO> dtoList = list.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
                return Result.success(dtoList);
            }
            
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取指定类型的货物总库存
     */
    @GetMapping("/stock-count")
    public Result<Integer> getStockCount(@RequestParam String type) {
        try {
            Integer count = goodsService.getTotalStock(type);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出货物
     */
    @GetMapping("/export")
    public void exportGoods(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            HttpServletResponse response) throws IOException {
        try {
            // 查询数据
            List<Goods> list = goodsService.getGoodsList(type, name);
            
            // 转换为导出DTO
            List<GoodsExportDTO> exportList = list.stream().map(goods -> {
                GoodsExportDTO dto = new GoodsExportDTO();
                BeanUtils.copyProperties(goods, dto);
                return dto;
            }).collect(Collectors.toList());
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("货物列表_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 写入Excel
            EasyExcel.write(response.getOutputStream(), GoodsExportDTO.class)
                    .registerWriteHandler(ExcelStyleConfig.getStyleStrategy())
                    .sheet("货物列表")
                    .doWrite(exportList);
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println("{\"code\":500,\"message\":\"导出失败：" + e.getMessage() + "\"}");
        }
    }
}
