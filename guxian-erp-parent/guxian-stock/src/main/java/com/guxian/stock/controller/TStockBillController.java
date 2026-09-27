package com.guxian.stock.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.result.Result;
import com.guxian.stock.dto.StockBillDTO;
import com.guxian.stock.entity.TStockRecord;
import com.guxian.stock.service.TMaterialStockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "出入库单据")
@RestController
@RequestMapping("/stock/bill")
public class TStockBillController {

    @Resource
    private TMaterialStockService materialStockService;

    /** 创建出入库单据并过账（采购入库2/退货出库5），返回单据号 */
    @OperLog(operModule = "出入库单据", operContent = "创建出入库单据")
    @PostMapping("/create")
    public Result<String> create(@RequestBody StockBillDTO dto) {
        return Result.success(materialStockService.createBill(dto));
    }

    /** 单据流水查询（按单号/类型/物料） */
    @GetMapping("/page")
    public Result<IPage<TStockRecord>> page(@RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize,
                                            @RequestParam(required = false) Integer bizType,
                                            @RequestParam(required = false) String relateNo,
                                            @RequestParam(required = false) Long materialId) {
        return Result.success(materialStockService.pageRecord(new Page<>(pageNum, pageSize), materialId, bizType, relateNo));
    }
}
