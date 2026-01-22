package com.stockmaster.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出库记录导出DTO
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(25)
public class OutboundRecordExportDTO {
    
    @ExcelProperty(value = "货物名称", index = 0)
    @ColumnWidth(20)
    private String goodsName;
    
    @ExcelProperty(value = "品牌", index = 1)
    @ColumnWidth(15)
    private String brand;
    
    @ExcelProperty(value = "型号", index = 2)
    @ColumnWidth(15)
    private String model;
    
    @ExcelProperty(value = "出库数量", index = 3)
    @ColumnWidth(12)
    private Integer quantity;
    
    @ExcelProperty(value = "使用部门", index = 4)
    @ColumnWidth(15)
    private String department;
    
    @ExcelProperty(value = "出库时间", index = 5)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime outboundTime;
    
    @ExcelProperty(value = "备注", index = 6)
    @ColumnWidth(30)
    private String remark;
}
