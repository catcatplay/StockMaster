package com.stockmaster.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 货物导出DTO
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(25)
public class GoodsExportDTO {

    @ExcelProperty(value = "货物名称", index = 0)
    @ColumnWidth(20)
    private String name;

    @ExcelProperty(value = "品牌", index = 1)
    @ColumnWidth(15)
    private String brand;

    @ExcelProperty(value = "型号", index = 2)
    @ColumnWidth(20)
    private String model;

    @ExcelProperty(value = "总库存", index = 3)
    @ColumnWidth(15)
    private Integer totalStock;

    @ExcelProperty(value = "剩余库存", index = 4)
    @ColumnWidth(15)
    private Integer remainingStock;

    @ExcelProperty(value = "创建时间", index = 5)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime createTime;
}
