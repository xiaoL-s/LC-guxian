package com.guxian.stock.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.result.Result;
import com.guxian.stock.dto.StockChangeDTO;
import com.guxian.stock.entity.TMaterialStock;
import com.guxian.stock.entity.TStockRecord;
import com.guxian.stock.service.TMaterialStockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "物料库存管理")
@RestController
@RequestMapping("/stock/material")
public class TMaterialController {

    @Resource
    private TMaterialStockService materialStockService;

    /** 物料分页 */
    @GetMapping("/page")
    public Result<IPage<TMaterialStock>> page(@RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "10") long pageSize,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer materialType,
                                              @RequestParam(required = false) Integer enable) {
        return Result.success(materialStockService.pageMaterial(new Page<>(pageNum, pageSize), keyword, materialType, enable));
    }

    /** 启用物料列表（下拉） */
    @GetMapping("/listEnabled")
    public Result<List<TMaterialStock>> listEnabled() {
        return Result.success(materialStockService.listEnabled());
    }

    /** 新增/编辑物料 */
    @OperLog(operModule = "物料档案", operContent = "保存物料档案")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody TMaterialStock material) {
        materialStockService.saveMaterial(material);
        return Result.success();
    }

    /** 删除物料（逻辑删除） */
    @OperLog(operModule = "物料档案", operContent = "删除物料档案")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        materialStockService.removeById(id);
        return Result.success();
    }

    /** 库存变动（入库/出库/盘点） */
    @OperLog(operModule = "物料库存", operContent = "库存变动")
    @PostMapping("/change")
    public Result<Void> change(@RequestBody StockChangeDTO dto) {
        materialStockService.changeStock(dto);
        return Result.success();
    }

    /** 批量出库（生产领料） */
    @OperLog(operModule = "物料库存", operContent = "批量领料出库")
    @PostMapping("/batchOut")
    public Result<Void> batchOut(@RequestBody List<StockChangeDTO> items) {
        materialStockService.batchOutStock(items);
        return Result.success();
    }

    /** 库存流水分页 */
    @GetMapping("/record/page")
    public Result<IPage<TStockRecord>> recordPage(@RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize,
                                                  @RequestParam(required = false) Long materialId,
                                                  @RequestParam(required = false) Integer bizType,
                                                  @RequestParam(required = false) String relateNo) {
        return Result.success(materialStockService.pageRecord(new Page<>(pageNum, pageSize), materialId, bizType, relateNo));
    }

    /** 库存预警列表：当前库存低于预警值 */
    @GetMapping("/warn/list")
    public Result<List<TMaterialStock>> warnList() {
        return Result.success(materialStockService.listWarn());
    }
}
