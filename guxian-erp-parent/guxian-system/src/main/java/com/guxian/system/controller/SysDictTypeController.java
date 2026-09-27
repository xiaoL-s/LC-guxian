package com.guxian.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.system.entity.SysDictType;
import com.guxian.result.Result;
import com.guxian.system.service.SysDictTypeService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/system/dict/type")
public class SysDictTypeController {

    @Resource
    private SysDictTypeService dictTypeService;

    /**
     * 字典类型分页查询
     */
    @GetMapping("/page")
    public Result<IPage<SysDictType>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "dictName", required = false) String dictName,
            @RequestParam(value = "dictType", required = false) String dictType
    ) {
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysDictType> wrapper
                = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dictName), SysDictType::getDictName, dictName);
        wrapper.like(StringUtils.hasText(dictType), SysDictType::getDictType, dictType);
        IPage<SysDictType> pageData = dictTypeService.page(page, wrapper);
        return Result.success(pageData);
    }

    /**
     * 新增/编辑字典类型
     */
    @PostMapping("/save")
    @OperLog(operModule = "字典管理（类型）", operType = "保存", operContent = "新增或修改字典类型信息")
    public Result<Void> save(@RequestBody SysDictType sysDictType) {
        dictTypeService.saveOrUpdate(sysDictType);
        return Result.success();
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/{id}")
    @OperLog(operModule = "字典管理（类型）", operType = "删除", operContent = "删除字典类型")
    public Result<Void> delete(@PathVariable("id") Long id) {
        dictTypeService.removeById(id);
        return Result.success();
    }

    /**
     * 根据id查询字典类型（编辑回显）
     */
    @GetMapping("/{id}")
    public Result<SysDictType> getInfo(@PathVariable("id") Long id) {
        SysDictType data = dictTypeService.getById(id);
        return Result.success(data);
    }
}
