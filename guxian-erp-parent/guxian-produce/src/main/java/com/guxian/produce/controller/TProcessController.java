package com.guxian.produce.controller;

import com.guxian.annotation.OperLog;
import com.guxian.produce.entity.TProcessDict;
import com.guxian.produce.service.TProcessDictService;
import com.guxian.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "工序管理")
@RestController
@RequestMapping("/production/process")
public class TProcessController {

    @Resource
    private TProcessDictService processDictService;

    @GetMapping("/list")
    public Result<List<TProcessDict>> list() {
        return Result.success(processDictService.list());
    }

    @GetMapping("/listEnabled")
    public Result<List<TProcessDict>> listEnabled() {
        return Result.success(processDictService.listEnabled());
    }

    @OperLog(operModule = "生产工序", operContent = "保存工序")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody TProcessDict process) {
        processDictService.saveProcess(process);
        return Result.success();
    }

    @OperLog(operModule = "生产工序", operContent = "删除工序")
    @DeleteMapping("/{processId}")
    public Result<Void> delete(@PathVariable("processId") Long processId) {
        processDictService.removeById(processId);
        return Result.success();
    }
}
