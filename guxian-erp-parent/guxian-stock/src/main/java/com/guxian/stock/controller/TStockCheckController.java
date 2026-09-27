package com.guxian.stock.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.result.Result;
import com.guxian.stock.entity.TStockCheck;
import com.guxian.stock.service.TStockCheckService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "库存盘点")
@RestController
@RequestMapping("/stock/check")
public class TStockCheckController {

    @Resource
    private TStockCheckService checkService;

    /** 盘点单分页 */
    @GetMapping("/page")
    public Result<IPage<TStockCheck>> page(@RequestParam(defaultValue = "1") long pageNum,
                                           @RequestParam(defaultValue = "10") long pageSize,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String keyword) {
        return Result.success(checkService.pageCheck(new Page<>(pageNum, pageSize), status, keyword));
    }

    /** 创建盘点单（带出启用物料+账面库存） */
    @OperLog(operModule = "库存盘点", operContent = "创建盘点单")
    @PostMapping("/create")
    public Result<Long> create(@RequestParam(required = false) String remark) {
        return Result.success(checkService.createCheck(remark));
    }

    /** 录入实盘数 */
    @OperLog(operModule = "库存盘点", operContent = "录入实盘数")
    @PostMapping("/items/save")
    public Result<Void> saveItems(@RequestParam Long checkId, @RequestBody List<Map<String, Object>> items) {
        checkService.saveItems(checkId, items);
        return Result.success();
    }

    /** 过账 */
    @OperLog(operModule = "库存盘点", operContent = "盘点过账")
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestParam Long checkId) {
        checkService.confirm(checkId);
        return Result.success();
    }

    /** 盘点单详情 */
    @GetMapping("/{checkId}")
    public Result<Map<String, Object>> detail(@PathVariable("checkId") Long checkId) {
        return Result.success(checkService.detail(checkId));
    }
}
