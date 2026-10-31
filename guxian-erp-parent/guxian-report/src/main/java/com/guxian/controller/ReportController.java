package com.guxian.controller;

import com.guxian.result.Result;
import com.guxian.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 报表统计中心：销售/生产/库存/利润 四大看板（端口 8088）
 * 风格参考速制ERP：指标卡 + 趋势折线 + 排名柱状
 */
@Tag(name = "报表统计-看板")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    /** 销售看板：今日新增/月销售额/月面积/应收款 + 下单趋势 + 产品销量排名 + 客户销量排名 */
    @GetMapping("/sales/overview")
    public Result<Map<String, Object>> salesOverview() {
        return Result.success(reportService.salesOverview());
    }

    /** 生产看板：工单统计 + 生产趋势 + 工序产量排名 + 工人产量排名 */
    @GetMapping("/production/overview")
    public Result<Map<String, Object>> productionOverview() {
        return Result.success(reportService.productionOverview());
    }

    /** 库存看板：入库统计 + 入库趋势 + 库位入库排名 */
    @GetMapping("/stock/overview")
    public Result<Map<String, Object>> stockOverview() {
        return Result.success(reportService.stockOverview());
    }

    /** 利润看板：月销售/月利润/毛利率/应收 + 12月利润趋势 + 产品利润排名 */
    @GetMapping("/profit/overview")
    public Result<Map<String, Object>> profitOverview() {
        return Result.success(reportService.profitOverview());
    }
}
