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

@RestController
@RequestMapping("/outbound")
public class OutboundRecordController {

    @Autowired
    private OutboundRecordService outboundRecordService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/create")
    public Result<Boolean> createOutboundRecord(@RequestBody OutboundRecord record, HttpServletRequest request) {
        String username = jwtUtils.getUsernameFromAuthorizationHeader(request.getHeader("Authorization"));
        if (username != null) {
            record.setOutboundUser(username);
        }

        boolean success = outboundRecordService.createOutboundRecord(record);
        return success ? Result.success("出库成功", true) : Result.error("出库失败");
    }

    @GetMapping("/list")
    public Result<List<OutboundRecord>> getAllRecords(@RequestParam(required = false) String type,
                                                       @RequestParam(required = false) Integer status) {
        List<OutboundRecord> list;
        if (type != null && !type.isEmpty()) {
            list = outboundRecordService.getRecordsByType(type, status);
        } else {
            list = outboundRecordService.getAllRecords(status);
        }
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result<Map<String, Object>> getRecordsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        Page<OutboundRecord> page = new Page<>(current, size);
        IPage<OutboundRecord> recordPage = outboundRecordService.getRecordsPage(page, type, startTime, endTime, status);

        Map<String, Object> result = new HashMap<>();
        result.put("records", recordPage.getRecords());
        result.put("total", recordPage.getTotal());
        result.put("current", recordPage.getCurrent());
        result.put("size", recordPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/listByGoods/{goodsId}")
    public Result<List<OutboundRecord>> getRecordsByGoodsId(@PathVariable Long goodsId) {
        List<OutboundRecord> list = outboundRecordService.getRecordsByGoodsId(goodsId);
        return Result.success(list);
    }

    @GetMapping("/listByTime")
    public Result<List<OutboundRecord>> getRecordsByTimeRange(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(required = false) Integer status) {
        List<OutboundRecord> list = outboundRecordService.getRecordsByTimeRange(startTime, endTime, status);
        return Result.success(list);
    }

    @GetMapping("/totalQuantity/{goodsId}")
    public Result<Integer> getTotalOutboundQuantity(@PathVariable Long goodsId) {
        Integer total = outboundRecordService.getTotalOutboundQuantityByGoodsId(goodsId);
        return Result.success(total);
    }

    @GetMapping("/export")
    public void exportRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            HttpServletResponse response) throws IOException {
        List<OutboundRecord> records = outboundRecordService.getRecordsList(type, startTime, endTime, status);
        List<OutboundRecordExportDTO> exportList = records.stream().map(record -> {
            OutboundRecordExportDTO dto = new OutboundRecordExportDTO();
            BeanUtils.copyProperties(record, dto);
            dto.setStatusText(Integer.valueOf(OutboundRecord.STATUS_CANCELED).equals(record.getStatus()) ? "已取消" : "未取消");
            return dto;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("出库记录_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), OutboundRecordExportDTO.class)
                .registerWriteHandler(ExcelStyleConfig.getStyleStrategy())
                .sheet("出库记录")
                .doWrite(exportList);
    }

    @DeleteMapping("/cancel/{id}")
    public Result<Boolean> cancelOutboundRecord(@PathVariable Long id, HttpServletRequest request) {
        Long roleId = (Long) request.getAttribute("roleId");
        if (roleId == null || roleId != 1) {
            return Result.error("无权限操作");
        }

        boolean success = outboundRecordService.cancelOutboundRecord(id);
        return success ? Result.success("取消出库成功", true) : Result.error("取消出库失败");
    }
}
