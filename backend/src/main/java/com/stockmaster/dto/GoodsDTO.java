package com.stockmaster.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 货物信息DTO - 用于出库员等角色，不包含单价信息
 */
@Data
public class GoodsDTO implements Serializable {
    
    private Long id;
    
    private String name;
    
    private String brand;
    
    private String model;
    
    private String batch;
    
    private String type;
    
    private Integer remainingStock;  // 剩余库存
    
    private Integer totalStock;  // 总库存

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
