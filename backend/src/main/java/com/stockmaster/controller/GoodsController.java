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

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RoleService roleService;

    private boolean isOutboundStaff(HttpServletRequest request) {
        Long roleId = (Long) request.getAttribute("roleId");
        if (roleId != null) {
            Role role = roleService.getById(roleId);
            return role != null && "出库员".equals(role.getRoleName());
        }
        return false;
    }

    private GoodsDTO convertToDTO(Goods goods) {
        GoodsDTO dto = new GoodsDTO();
        BeanUtils.copyProperties(goods, dto);
        return dto;
    }

    @PostMapping("/add")
    public Result<Boolean> addGoods(@RequestBody Goods goods) {
        boolean success = goodsService.addGoods(goods);
        return success ? Result.success("添加成功", true) : Result.error("添加失败");
    }

    @PutMapping("/update")
    public Result<Boolean> updateGoods(@RequestBody Goods goods) {
        boolean success = goodsService.updateGoods(goods);
        return success ? Result.success("更新成功", true) : Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteGoods(@PathVariable Long id) {
        boolean success = goodsService.deleteGoods(id);
        return success ? Result.success("删除成功", true) : Result.error("删除失败");
    }

    @GetMapping("/get/{id}")
    public Result<Goods> getGoods(@PathVariable Long id) {
        Goods goods = goodsService.getGoodsById(id);
        return goods != null ? Result.success(goods) : Result.error("货物不存在");
    }

    @GetMapping("/list")
    public Result<?> getAllGoods(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model) {
        List<Goods> list;
        if ((name != null && !name.isEmpty())
                || (brand != null && !brand.isEmpty())
                || (model != null && !model.isEmpty())) {
            list = goodsService.getGoodsList(type, name, brand, model);
        } else if (type != null && !type.isEmpty()) {
            list = goodsService.getGoodsByType(type);
        } else {
            list = goodsService.getAllGoods();
        }
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result<Map<String, Object>> getGoodsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            HttpServletRequest request) {
        Page<Goods> page = new Page<>(current, size);
        IPage<Goods> goodsPage = goodsService.getGoodsPage(page, type, name, brand, model);

        Map<String, Object> result = new HashMap<>();
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
    }

    @GetMapping("/search")
    public Result<?> searchGoods(@RequestParam String name, HttpServletRequest request) {
        List<Goods> list = goodsService.searchGoodsByName(name);
        if (isOutboundStaff(request)) {
            List<GoodsDTO> dtoList = list.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return Result.success(dtoList);
        }
        return Result.success(list);
    }

    @GetMapping("/stock-count")
    public Result<Integer> getStockCount(@RequestParam String type) {
        Integer count = goodsService.getTotalStock(type);
        return Result.success(count);
    }

    @GetMapping("/export")
    public void exportGoods(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            HttpServletResponse response) throws IOException {
        List<Goods> list = goodsService.getGoodsList(type, name, brand, model);
        List<GoodsExportDTO> exportList = list.stream().map(goods -> {
            GoodsExportDTO dto = new GoodsExportDTO();
            BeanUtils.copyProperties(goods, dto);
            return dto;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("货物列表_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), GoodsExportDTO.class)
                .registerWriteHandler(ExcelStyleConfig.getStyleStrategy())
                .sheet("货物列表")
                .doWrite(exportList);
    }
}
