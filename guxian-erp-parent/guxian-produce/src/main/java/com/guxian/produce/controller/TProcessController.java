package com.guxian.produce.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guxian.annotation.OperLog;
import com.guxian.produce.entity.TProcessDict;
import com.guxian.produce.entity.TProcessProductPrice;
import com.guxian.produce.mapper.TProcessProductPriceMapper;
import com.guxian.produce.service.TProcessDictService;
import com.guxian.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "工序管理")
@RestController
@RequestMapping("/production/process")
public class TProcessController {

    @Resource
    private TProcessDictService processDictService;

    @Resource
    private TProcessProductPriceMapper priceMapper;

    @GetMapping("/list")
    public Result<List<TProcessDict>> list() {
        List<TProcessDict> list = processDictService.list();
        // 查每个工序下产品计件单价的最近更新时间，填充到列表
        List<TProcessProductPrice> allPrices = priceMapper.selectList(
                new LambdaQueryWrapper<TProcessProductPrice>()
                        .select(TProcessProductPrice::getProcessId, TProcessProductPrice::getUpdateTime));
        Map<Long, java.time.LocalDateTime> latestMap = new HashMap<>();
        for (TProcessProductPrice p : allPrices) {
            if (p.getUpdateTime() == null) continue;
            java.time.LocalDateTime t = p.getUpdateTime();
            java.time.LocalDateTime old = latestMap.get(p.getProcessId());
            if (old == null || t.isAfter(old)) latestMap.put(p.getProcessId(), t);
        }
        for (TProcessDict d : list) {
            d.setLatestPriceUpdateTime(
                    latestMap.get(d.getProcessId()) == null ? "" :
                    latestMap.get(d.getProcessId()).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return Result.success(list);
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
