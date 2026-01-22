package com.stockmaster.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 入库记录导出DTO
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(25)
public class InboundRecordExportDTO {
    
    @ExcelProperty(value = "货物名称", index = 0)
    @ColumnWidth(20)
    private String goodsName;
    
    @ExcelProperty(value = "品牌", index = 1)
    @ColumnWidth(15)
    private String brand;
    
    @ExcelProperty(value = "型号", index = 2)
    @ColumnWidth(15)
    private String model;
    
    @ExcelProperty(value = "入库数量", index = 3)
    @ColumnWidth(12)
    private Integer quantity;
    
    @ExcelProperty(value = "入库时间", index = 4)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime inboundTime;
    
    @ExcelProperty(value = "备注", index = 5)
    @ColumnWidth(30)
    private String remark;
}
