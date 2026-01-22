package com.stockmaster;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 库存管家启动类
 */
@SpringBootApplication
@MapperScan("com.stockmaster.mapper")
public class StockOutboundApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockOutboundApplication.class, args);
    }
}
