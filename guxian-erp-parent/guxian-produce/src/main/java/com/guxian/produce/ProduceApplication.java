package com.guxian.produce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 生产管理微服务启动类（端口8085）
 * scanBasePackages 扫描 com.guxian，加载公共模块的配置/拦截器/自动填充
 */
@SpringBootApplication(scanBasePackages = "com.guxian")
public class ProduceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProduceApplication.class, args);
    }
}
