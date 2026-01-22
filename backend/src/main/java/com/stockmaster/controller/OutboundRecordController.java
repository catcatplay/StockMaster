package com.stockmaster.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stockmaster.common.Result;
import com.stockmaster.config.ExcelStyleConfig;
import com.stockmaster.config.JwtUtils;
import com.stockmaster.dto.OutboundRecordExportDTO;
import com.stockmaster.entity.OutboundRecord;
import com.stockmaster.service.OutboundRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 出库记录控制器
 */
@RestController
@RequestMapping("/outbound")
public class OutboundRecordController {

    @Autowired
    private OutboundRecordService outboundRecordService;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 创建出库记录
     */
    @PostMapping("/create")
    public Result<Boolean> createOutboundRecord(@RequestBody OutboundRecord record, HttpServletRequest request) {
        try {
            // 获取当前用户名
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // 移除 "Bearer " 前缀
                String username = jwtUtils.getUsernameFromToken(token);
                record.setOutboundUser(username);
            }
            
            boolean success = outboundRecordService.createOutboundRecord(record);
            return success ? Result.success("出库成功", true) : Result.error("出库失败");
        } catch (Exception e) {
            return Result.error("出库失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有出库记录
     */
    @GetMapping("/list")
    public Result<List<OutboundRecord>> getAllRecords(@RequestParam(required = false) String type) {
        try {
            List<OutboundRecord> list;
            if (type != null && !type.isEmpty()) {
                list = outboundRecordService.getRecordsByType(type);
            } else {
                list = outboundRecordService.getAllRecords();
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询出库记录
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getRecordsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type) {
        try {
            Page<OutboundRecord> page = new Page<>(current, size);
            IPage<OutboundRecord> recordPage = outboundRecordService.getRecordsPage(page, type);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", recordPage.getRecords());
            result.put("total", recordPage.getTotal());
            result.put("current", recordPage.getCurrent());
            result.put("size", recordPage.getSize());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据货物ID查询出库记录
     */
    @GetMapping("/listByGoods/{goodsId}")
    public Result<List<OutboundRecord>> getRecordsByGoodsId(@PathVariable Long goodsId) {
        try {
            List<OutboundRecord> list = outboundRecordService.getRecordsByGoodsId(goodsId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据时间范围查询出库记录
     */
    @GetMapping("/listByTime")
    public Result<List<OutboundRecord>> getRecordsByTimeRange(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        try {
            List<OutboundRecord> list = outboundRecordService.getRecordsByTimeRange(startTime, endTime);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 统计货物总出库量
     */
    @GetMapping("/totalQuantity/{goodsId}")
    public Result<Integer> getTotalOutboundQuantity(@PathVariable Long goodsId) {
        try {
            Integer total = outboundRecordService.getTotalOutboundQuantityByGoodsId(goodsId);
            return Result.success(total);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出出库记录
     */
    @GetMapping("/export")
    public void exportRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            HttpServletResponse response) throws IOException {
        try {
            // 查询数据
            List<OutboundRecord> records;
            if (type != null && !type.isEmpty()) {
                // 根据type过滤
                records = outboundRecordService.getRecordsByType(type);
                // 如果有时间范围，进一步过滤
                if (startTime != null || endTime != null) {
                    final Date finalStartTime = startTime;
                    final Date finalEndTime = endTime;
                    records = records.stream()
                        .filter(record -> {
                            if (record.getOutboundTime() == null) return false;
                            Date recordTime = java.sql.Timestamp.valueOf(record.getOutboundTime());
                            if (finalStartTime != null && recordTime.before(finalStartTime)) return false;
                            if (finalEndTime != null && recordTime.after(finalEndTime)) return false;
                            return true;
                        })
                        .collect(Collectors.toList());
                }
            } else {
                records = outboundRecordService.getRecordsByTimeRange(startTime, endTime);
            }
            
            // 转换为导出DTO
            List<OutboundRecordExportDTO> exportList = records.stream().map(record -> {
                OutboundRecordExportDTO dto = new OutboundRecordExportDTO();
                BeanUtils.copyProperties(record, dto);
                return dto;
            }).collect(Collectors.toList());
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("出库记录_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 写入Excel
            EasyExcel.write(response.getOutputStream(), OutboundRecordExportDTO.class)
                    .registerWriteHandler(ExcelStyleConfig.getStyleStrategy())
                    .sheet("出库记录")
                    .doWrite(exportList);
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println("{\"code\":500,\"message\":\"导出失败：" + e.getMessage() + "\"}");
        }
    }
}
