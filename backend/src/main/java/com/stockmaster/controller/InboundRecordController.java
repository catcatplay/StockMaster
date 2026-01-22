package com.stockmaster.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stockmaster.common.Result;
import com.stockmaster.config.ExcelStyleConfig;
import com.stockmaster.config.JwtUtils;
import com.stockmaster.dto.InboundRecordExportDTO;
import com.stockmaster.entity.InboundRecord;
import com.stockmaster.service.InboundRecordService;
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
 * 入库记录控制器
 */
@RestController
@RequestMapping("/inbound")
public class InboundRecordController {

    @Autowired
    private InboundRecordService inboundRecordService;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 创建入库记录
     */
    @PostMapping("/create")
    public Result<Boolean> createInboundRecord(@RequestBody InboundRecord record, HttpServletRequest request) {
        try {
            // 获取当前用户名
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // 移除 "Bearer " 前缀
                String username = jwtUtils.getUsernameFromToken(token);
                record.setInboundUser(username);
            }
            // 设置默认结算状态
            record.setSettlementStatus("未结算");
            
            boolean success = inboundRecordService.createInboundRecord(record);
            return success ? Result.success("入库成功", true) : Result.error("入库失败");
        } catch (Exception e) {
            return Result.error("入库失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有入库记录
     */
    @GetMapping("/list")
    public Result<List<InboundRecord>> getAllRecords(@RequestParam(required = false) String type) {
        try {
            List<InboundRecord> list;
            if (type != null && !type.isEmpty()) {
                list = inboundRecordService.getRecordsByType(type);
            } else {
                list = inboundRecordService.getAllRecords();
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询入库记录
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getRecordsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type) {
        try {
            Page<InboundRecord> page = new Page<>(current, size);
            IPage<InboundRecord> recordPage = inboundRecordService.getRecordsPage(page, type);
            
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
     * 根据货物ID查询入库记录
     */
    @GetMapping("/listByGoods/{goodsId}")
    public Result<List<InboundRecord>> getRecordsByGoodsId(@PathVariable Long goodsId) {
        try {
            List<InboundRecord> list = inboundRecordService.getRecordsByGoodsId(goodsId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据时间范围查询入库记录
     */
    @GetMapping("/listByTime")
    public Result<List<InboundRecord>> getRecordsByTimeRange(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        try {
            List<InboundRecord> list = inboundRecordService.getRecordsByTimeRange(startTime, endTime);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出入库记录
     */
    @GetMapping("/export")
    public void exportRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            HttpServletResponse response) throws IOException {
        try {
            // 查询数据
            List<InboundRecord> records;
            if (type != null && !type.isEmpty()) {
                // 根据type过滤
                records = inboundRecordService.getRecordsByType(type);
                // 如果有时间范围，进一步过滤
                if (startTime != null || endTime != null) {
                    final Date finalStartTime = startTime;
                    final Date finalEndTime = endTime;
                    records = records.stream()
                        .filter(record -> {
                            if (record.getInboundTime() == null) return false;
                            Date recordTime = java.sql.Timestamp.valueOf(record.getInboundTime());
                            if (finalStartTime != null && recordTime.before(finalStartTime)) return false;
                            if (finalEndTime != null && recordTime.after(finalEndTime)) return false;
                            return true;
                        })
                        .collect(Collectors.toList());
                }
            } else {
                records = inboundRecordService.getRecordsByTimeRange(startTime, endTime);
            }
            
            // 转换为导出DTO
            List<InboundRecordExportDTO> exportList = records.stream().map(record -> {
                InboundRecordExportDTO dto = new InboundRecordExportDTO();
                BeanUtils.copyProperties(record, dto);
                return dto;
            }).collect(Collectors.toList());
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("入库记录_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 写入Excel
            EasyExcel.write(response.getOutputStream(), InboundRecordExportDTO.class)
                    .registerWriteHandler(ExcelStyleConfig.getStyleStrategy())
                    .sheet("入库记录")
                    .doWrite(exportList);
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println("{\"code\":500,\"message\":\"导出失败：" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 更新结算状态
     */
    @PutMapping("/updateSettlement")
    public Result<Boolean> updateSettlementStatus(@RequestBody InboundRecord record) {
        try {
            boolean success = inboundRecordService.updateSettlementStatus(
                record.getId(), 
                record.getSettlementStatus(), 
                record.getRemark()
            );
            return success ? Result.success("更新成功", true) : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }
}
