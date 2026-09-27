package com.guxian.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 库存物料微服务启动类（端口8084）
 * scanBasePackages 扫描 com.guxian，加载公共模块的配置/拦截器/自动填充
 */
@SpringBootApplication(scanBasePackages = "com.guxian")
public class StockApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class, args);
    }
}
