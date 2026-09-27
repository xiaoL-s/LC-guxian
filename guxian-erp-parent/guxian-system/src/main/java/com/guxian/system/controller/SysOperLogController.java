package com.guxian.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.system.dto.SysOperLogDTO;
import com.guxian.mybatis.entity.SysOperLog;
import com.guxian.result.Result;
import com.guxian.system.service.SysOperLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/operLog")
public class SysOperLogController {

    @Resource
    private SysOperLogService operLogService;

    @GetMapping("/page")
    public Result<IPage<SysOperLogDTO>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "operModule", required = false) String operModule,
            @RequestParam(value = "realName", required = false) String realName,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime
    ){
        IPage<SysOperLog> page = new Page<>(pageNum, pageSize);
        IPage<SysOperLogDTO> data = operLogService.page(page, operModule, realName, startTime, endTime);
        return Result.success(data);
    }
}
