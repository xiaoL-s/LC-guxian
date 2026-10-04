package com.guxian.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.system.dto.FormulaTestRequest;
import com.guxian.system.entity.SysDictData;
import com.guxian.result.Result;
import com.guxian.system.service.SysDictDataService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController {

    @Resource
    private SysDictDataService dictDataService;

    /**
     * 字典数据分页查询
     */
    @GetMapping("/page")
    public Result<IPage<SysDictData>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "dictType", required = false) String dictType,
            @RequestParam(value = "dictLabel",required = false) String dictLabel
    ) {
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictData> wrapper
                = new LambdaQueryWrapper<>();
        // dictType 可选：不传时返回全部字典数据
        wrapper.eq(StringUtils.hasText(dictType), SysDictData::getDictType, dictType);
        // 新增：标签名称模糊搜索
        wrapper.like(StringUtils.hasText(dictLabel), SysDictData::getDictLabel, dictLabel);
        wrapper.orderByAsc(SysDictData::getSort);
        IPage<SysDictData> pageData = dictDataService.page(page, wrapper);
        return Result.success(pageData);
    }


    /**
     * 新增/编辑字典数据
     */
    @PostMapping("/save")
    @OperLog(operModule = "字典管理（数据）", operType = "保存", operContent = "新增或修改字典数据信息")
    public Result<Void> save(@RequestBody SysDictData sysDictData) {
        dictDataService.saveOrUpdate(sysDictData);
        return Result.success();
    }

    /**
     * 删除字典数据
     */
    @DeleteMapping("/{id}")
    @OperLog(operModule = "字典管理（数据）", operType = "删除", operContent = "删除字典数据")
    public Result<Void> delete(@PathVariable("id") Long id) {
        dictDataService.removeById(id);
        return Result.success();
    }

    /**
     * 根据id查询字典数据（编辑回显）
     */
    @GetMapping("/{id}")
    public Result<SysDictData> getInfo(@PathVariable("id") Long id) {
        SysDictData data = dictDataService.getById(id);
        return Result.success(data);
    }

    /**
     * 产品公式测试：根据产品字典项公式配置 + 输入参数，计算下料/剪网/面积/金额
     */
    @PostMapping("/formula/test")
    public Result<Map<String, Object>> formulaTest(@RequestBody FormulaTestRequest request) {
        return dictDataService.formulaTest(request);
    }
}
