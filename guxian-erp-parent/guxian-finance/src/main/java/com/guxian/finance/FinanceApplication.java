package com.guxian.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 财务管理微服务启动类（端口8087）
 * 集中承载计件工资统计、月度工资确认等金额相关能力
 * scanBasePackages 扫描 com.guxian，加载公共模块的配置/拦截器/自动填充
 */
@SpringBootApplication(scanBasePackages = "com.guxian")
@MapperScan("com.guxian.mapper")
public class FinanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }
}
