package com.stockmaster.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出库记录实体类
 */
@Data
@TableName("outbound_record")
public class OutboundRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long goodsId;
    
    private String goodsName;
    
    private String brand;
    
    private String model;
    
    private String type;
    
    private Integer quantity;
    
    private String department;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime outboundTime;
    
    private String remark;
    
    private String outboundUser;
}
