package com.guxian.controller;

import com.guxian.service.WageService;
import com.guxian.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 财务管理：计件工资统计与月度工资确认（端口8087）
 */
@Tag(name = "财务管理-计件工资")
@RestController
@RequestMapping("/finance/wage")
public class WageController {

    @Resource
    private WageService wageService;

    /** 计件工资汇总 */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(@RequestParam(required = false) Long workerId,
                                               @RequestParam(required = false) String dateFrom,
                                               @RequestParam(required = false) String dateTo,
                                               @RequestParam(required = false) String workerName) {
        return Result.success(wageService.summary(workerId, dateFrom, dateTo, workerName));
    }

    /** 按工人汇总（计件工资核算） */
    @GetMapping("/byWorker")
    public Result<List<Map<String, Object>>> byWorker(@RequestParam(required = false) String dateFrom,
                                                      @RequestParam(required = false) String dateTo,
                                                      @RequestParam(required = false) String workerName) {
        return Result.success(wageService.byWorker(dateFrom, dateTo, workerName));
    }

    /** 计件工资明细（分页，按 员工×工单×工序 聚合；关键字匹配 订单号/工序名/客户/地址） */
    @GetMapping("/detail")
    public Result<IPage<Map<String, Object>>> detail(@RequestParam(defaultValue = "1") long pageNum,
                                                     @RequestParam(defaultValue = "30") long pageSize,
                                                     @RequestParam(required = false) String dateFrom,
                                                     @RequestParam(required = false) String dateTo,
                                                     @RequestParam(required = false) String workerName,
                                                     @RequestParam(required = false) String keyword) {
        return Result.success(wageService.wageDetail(new Page<>(pageNum, pageSize), dateFrom, dateTo, workerName, keyword));
    }

    /** 月度工资确认列表 */
    @GetMapping("/month")
    public Result<List<Map<String, Object>>> monthList(@RequestParam(required = false) String wageMonth,
                                                       @RequestParam(required = false) Long workerId) {
        return Result.success(wageService.monthList(wageMonth, workerId));
    }

    /** 生成/保存月度工资（totalWage 自动按该月报工合计，可调整扣款/加款） */
    @PostMapping("/month")
    public Result<Map<String, Object>> saveMonth(@RequestBody Map<String, Object> body) {
        Long workerId = ((Number) body.get("workerId")).longValue();
        String wageMonth = (String) body.get("wageMonth");
        BigDecimal deduct = body.get("deductWage") == null ? null : new BigDecimal(body.get("deductWage").toString());
        BigDecimal add = body.get("addWage") == null ? null : new BigDecimal(body.get("addWage").toString());
        String remark = (String) body.get("remark");
        return Result.success(wageService.saveMonth(workerId, wageMonth, deduct, add, remark));
    }

    /** 删除月度工资记录 */
    @DeleteMapping("/month/{id}")
    public Result<Boolean> deleteMonth(@PathVariable Long id) {
        return Result.success(wageService.deleteMonth(id));
    }
}
