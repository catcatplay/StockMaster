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

@RestController
@RequestMapping("/inbound")
public class InboundRecordController {

    @Autowired
    private InboundRecordService inboundRecordService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/create")
    public Result<Boolean> createInboundRecord(@RequestBody InboundRecord record, HttpServletRequest request) {
        String username = jwtUtils.getUsernameFromAuthorizationHeader(request.getHeader("Authorization"));
        if (username != null) {
            record.setInboundUser(username);
        }
        record.setSettlementStatus("未结算");

        boolean success = inboundRecordService.createInboundRecord(record);
        return success ? Result.success("入库成功", true) : Result.error("入库失败");
    }

    @GetMapping("/list")
    public Result<List<InboundRecord>> getAllRecords(@RequestParam(required = false) String type) {
        List<InboundRecord> list;
        if (type != null && !type.isEmpty()) {
            list = inboundRecordService.getRecordsByType(type);
        } else {
            list = inboundRecordService.getAllRecords();
        }
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result<Map<String, Object>> getRecordsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        Page<InboundRecord> page = new Page<>(current, size);
        IPage<InboundRecord> recordPage = inboundRecordService.getRecordsPage(page, type, startTime, endTime);

        Map<String, Object> result = new HashMap<>();
        result.put("records", recordPage.getRecords());
        result.put("total", recordPage.getTotal());
        result.put("current", recordPage.getCurrent());
        result.put("size", recordPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/listByGoods/{goodsId}")
    public Result<List<InboundRecord>> getRecordsByGoodsId(@PathVariable Long goodsId) {
        List<InboundRecord> list = inboundRecordService.getRecordsByGoodsId(goodsId);
        return Result.success(list);
    }

    @GetMapping("/listByTime")
    public Result<List<InboundRecord>> getRecordsByTimeRange(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        List<InboundRecord> list = inboundRecordService.getRecordsByTimeRange(startTime, endTime);
        return Result.success(list);
    }

    @GetMapping("/export")
    public void exportRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            HttpServletResponse response) throws IOException {
        List<InboundRecord> records = inboundRecordService.getRecordsList(type, startTime, endTime);
        List<InboundRecordExportDTO> exportList = records.stream().map(record -> {
            InboundRecordExportDTO dto = new InboundRecordExportDTO();
            BeanUtils.copyProperties(record, dto);
            return dto;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("入库记录_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), InboundRecordExportDTO.class)
                .registerWriteHandler(ExcelStyleConfig.getStyleStrategy())
                .sheet("入库记录")
                .doWrite(exportList);
    }

    @PutMapping("/updateSettlement")
    public Result<Boolean> updateSettlementStatus(@RequestBody InboundRecord record) {
        boolean success = inboundRecordService.updateSettlementStatus(
                record.getId(),
                record.getSettlementStatus(),
                record.getRemark()
        );
        return success ? Result.success("更新成功", true) : Result.error("更新失败");
    }
}
