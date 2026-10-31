package com.guxian.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 报表统计微服务启动类（端口 8088）
 * 集中承载销售报表、生产报表、库存报表、利润报表等看板统计能力
 * scanBasePackages 扫描 com.guxian，加载公共模块的配置/拦截器自动装配
 */
@SpringBootApplication(scanBasePackages = "com.guxian")
@MapperScan("com.guxian.mapper")
public class ReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReportApplication.class, args);
    }
}
