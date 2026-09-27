package com.guxian.produce.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.produce.service.TWorkReportService;
import com.guxian.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "报工记录与计件工资")
@RestController
@RequestMapping("/production/report")
public class TWorkReportController {

    @Resource
    private TWorkReportService workReportService;

    @GetMapping("/page")
    public Result<IPage<Map<String, Object>>> page(@RequestParam(defaultValue = "1") long pageNum,
                                                   @RequestParam(defaultValue = "10") long pageSize,
                                                   @RequestParam(required = false) Long workOrderId,
                                                   @RequestParam(required = false) Long workerId,
                                                   @RequestParam(required = false) Long processId,
                                                   @RequestParam(required = false) String keyword) {
        return Result.success(workReportService.pageReport(new Page<>(pageNum, pageSize), workOrderId, workerId, processId, keyword));
    }

    /** 计件工资汇总 */
    @GetMapping("/wage/summary")
    public Result<Map<String, Object>> wageSummary(@RequestParam(required = false) Long workerId,
                                                   @RequestParam(required = false) String dateFrom,
                                                   @RequestParam(required = false) String dateTo) {
        return Result.success(workReportService.wageSummary(workerId, dateFrom, dateTo));
    }

    /** 按工人汇总计件工资（计件工资核算） */
    @GetMapping("/wage/byWorker")
    public Result<java.util.List<Map<String, Object>>> wageByWorker(@RequestParam(required = false) String dateFrom,
                                                                    @RequestParam(required = false) String dateTo) {
        return Result.success(workReportService.wageByWorker(dateFrom, dateTo));
    }
}
