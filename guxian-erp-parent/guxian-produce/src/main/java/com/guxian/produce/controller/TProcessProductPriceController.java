package com.guxian.produce.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.exception.BusinessException;
import com.guxian.produce.entity.TProcessProductPrice;
import com.guxian.produce.service.TProcessProductPriceService;
import com.guxian.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 工序产品计件价管理
 * 维护"每个工序下各产品的计件单价"，批量导入导出 xlsx
 */
@Tag(name = "产品计件价")
@RestController
@RequestMapping("/production/price")
public class  TProcessProductPriceController {

    @Resource
    private TProcessProductPriceService priceService;

    /** 分页列表 */
    @GetMapping("/page")
    public Result<Page<TProcessProductPrice>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) Long processId,
            @RequestParam(required = false) String productName) {
        return Result.success(priceService.page(pageNum, pageSize, processId, productName));
    }

    /** 新增/修改单条 */
    @OperLog(operModule = "产品计件价", operContent = "保存计件价")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody TProcessProductPrice price) {
        if (price == null || price.getProcessId() == null) {
            throw new BusinessException("请选择所属工序");
        }
        if (!StringUtils.hasText(price.getProductName())) {
            throw new BusinessException("产品名称不能为空");
        }
        if (price.getPrice() != null && price.getPrice().signum() < 0) {
            throw new BusinessException("计件单价不能为负数");
        }
        priceService.savePrice(price);
        return Result.success();
    }

    /** 删除单条 */
    @OperLog(operModule = "产品计件价", operContent = "删除计件价")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        priceService.deletePrice(id);
        return Result.success();
    }

    /** Excel 批量导入（三列通用格式，带工序名） */
    @OperLog(operModule = "产品计件价", operContent = "Excel导入")
    @PostMapping("/import")
    public Result<String> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(priceService.importExcel(file));
    }

    /** 按当前工序导入：文件只需两列 产品名称|计件单价 */
    @OperLog(operModule = "产品计件价", operContent = "按工序Excel导入")
    @PostMapping("/importByProcess/{processId}")
    public Result<String> importByProcess(@PathVariable Long processId, @RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(priceService.importByProcess(processId, file));
    }

    /** 模板下载 */
    @GetMapping("/template")
    public ResponseEntity<byte[]> template() throws IOException {
        byte[] data = priceService.template();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" + URLEncoder.encode("产品计件价模板.xlsx", StandardCharsets.UTF_8))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    /** 按筛选导出 Excel */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long processId,
                                        @RequestParam(required = false) String productName) throws IOException {
        byte[] data = priceService.exportExcel(processId, productName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=" + URLEncoder.encode("产品计件价.xlsx", StandardCharsets.UTF_8))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
