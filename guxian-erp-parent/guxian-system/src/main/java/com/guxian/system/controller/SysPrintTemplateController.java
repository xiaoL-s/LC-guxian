package com.guxian.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.system.dto.SysPrintTemplateDTO;
import com.guxian.system.entity.SysPrintTemplate;
import com.guxian.result.Result;
import com.guxian.system.service.SysPrintTemplateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 打印模板管理
 */
@RestController
@RequestMapping("/system/printTemplate")
public class SysPrintTemplateController {

    @Resource
    private SysPrintTemplateService printTemplateService;

    /**
     * 所有去重后的单据类型（下拉渲染用）
     */
    @GetMapping("/types")
    public Result<List<String>> types() {
        return Result.success(printTemplateService.listTypes());
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<IPage<SysPrintTemplate>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "templateName", required = false) String templateName,
            @RequestParam(value = "templateType", required = false) String templateType,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        Page<SysPrintTemplate> page = new Page<>(pageNum, pageSize);
        IPage<SysPrintTemplate> pageData = printTemplateService.getPage(page, templateName, templateType, status);
        return Result.success(pageData);
    }

    /**
     * 新增+编辑一体接口
     */
    @PostMapping("/save")
    @OperLog(operModule = "打印模板管理", operType = "保存", operContent = "新增/修改打印模板信息")
    public Result<Void> save(@RequestBody SysPrintTemplateDTO dto) {
        printTemplateService.saveTemplate(dto);
        return Result.success();
    }

    /**
     * 根据 id 查询（编辑回显）
     */
    @GetMapping("/getById/{templateId}")
    public Result<SysPrintTemplate> getById(@PathVariable("templateId") Long templateId) {
        return Result.success(printTemplateService.getById(templateId));
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/delete/{templateId}")
    @OperLog(operModule = "打印模板管理", operType = "删除", operContent = "删除打印模板")
    public Result<Void> delete(@PathVariable("templateId") Long templateId) {
        printTemplateService.removeById(templateId);
        return Result.success();
    }

    /**
     * 启用 / 禁用切换
     */
    @PutMapping("/toggleStatus/{templateId}")
    @OperLog(operModule = "打印模板管理", operType = "保存", operContent = "启用/禁用打印模板信息")
    public Result<Void> toggleStatus(@PathVariable("templateId") Long templateId,
                                     @RequestParam("status") Integer status) {
        printTemplateService.toggleStatus(templateId, status);
        return Result.success();
    }

    /**
     * 模板预览，返回 templateContent（前端负责用模拟订单数据渲染）
     */
    @GetMapping("/preview/{templateId}")
    public Result<String> preview(@PathVariable("templateId") Long templateId) {
        String html = printTemplateService.getPreviewHtml(templateId);
        return Result.success(html);
    }
}
