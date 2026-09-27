package com.guxian.stock.controller;

import com.guxian.annotation.OperLog;
import com.guxian.result.Result;
import com.guxian.stock.entity.TShelf;
import com.guxian.stock.service.TShelfService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "货架库位管理")
@RestController
@RequestMapping("/stock/shelf")
public class TShelfController {

    @Resource
    private TShelfService shelfService;

    @GetMapping("/list")
    public Result<List<TShelf>> list() {
        return Result.success(shelfService.list());
    }

    @GetMapping("/listEnabled")
    public Result<List<TShelf>> listEnabled() {
        return Result.success(shelfService.listEnabled());
    }

    @OperLog(operModule = "货架管理", operContent = "保存货架")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody TShelf shelf) {
        shelfService.saveShelf(shelf);
        return Result.success();
    }

    @OperLog(operModule = "货架管理", operContent = "删除货架")
    @DeleteMapping("/{shelfId}")
    public Result<Void> delete(@PathVariable("shelfId") Long shelfId) {
        shelfService.removeById(shelfId);
        return Result.success();
    }
}
