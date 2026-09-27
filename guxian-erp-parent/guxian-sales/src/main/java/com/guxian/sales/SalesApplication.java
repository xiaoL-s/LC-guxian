package com.guxian.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 销售订单微服务启动类（端口8083）
 * scanBasePackages 扫描 com.guxian，加载公共模块的配置/拦截器/自动填充
 */
@SpringBootApplication(scanBasePackages = "com.guxian")
public class SalesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalesApplication.class, args);
    }
}
