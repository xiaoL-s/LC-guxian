package com.guxian.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.result.Result;
import com.guxian.sales.dto.TProductDTO;
import com.guxian.sales.entity.TProduct;
import com.guxian.sales.service.TProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品档案
 */
@RestController
@RequestMapping("/sales/product")
public class TProductController {

    @Resource
    private TProductService productService;

    /** 产品分页 */
    @GetMapping("/page")
    public Result<IPage<TProductDTO>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "productType", required = false) String productType) {
        Page<TProduct> page = new Page<>(pageNum, pageSize);
        return Result.success(productService.pageProduct(page, productName, productType));
    }

    /** 产品详情 */
    @GetMapping("/{productId}")
    public Result<TProduct> info(@PathVariable("productId") Long productId) {
        return Result.success(productService.getById(productId));
    }

    /** 新增/编辑产品 */
    @PostMapping("/save")
    @OperLog(operModule = "产品档案", operType = "保存", operContent = "新增/编辑产品")
    public Result<Void> save(@RequestBody TProductDTO dto) {
        productService.saveProduct(dto);
        return Result.success();
    }

    /** 删除产品 */
    @DeleteMapping("/{productId}")
    @OperLog(operModule = "产品档案", operType = "删除", operContent = "删除产品")
    public Result<Void> remove(@PathVariable("productId") Long productId) {
        productService.removeById(productId);
        return Result.success();
    }

    /** 启用产品下拉（订单录入用） */
    @GetMapping("/listEnabled")
    public Result<List<TProduct>> listEnabled() {
        return Result.success(productService.listEnabled());
    }
}
