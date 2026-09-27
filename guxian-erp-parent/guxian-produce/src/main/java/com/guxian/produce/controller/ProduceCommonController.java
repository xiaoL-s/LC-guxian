package com.guxian.produce.controller;

import com.guxian.produce.mapper.ProductInstockReadMapper;
import com.guxian.produce.mapper.SysUserReadMapper;
import com.guxian.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "公共只读接口")
@RestController
@RequestMapping("/production/common")
public class ProduceCommonController {

    @Resource
    private SysUserReadMapper sysUserReadMapper;
    @Resource
    private ProductInstockReadMapper productInstockReadMapper;

    /** 启用用户列表（报工选工人） */
    @GetMapping("/users")
    public Result<List<Map<String, Object>>> users() {
        return Result.success(sysUserReadMapper.selectEnabledUsers());
    }

    /** 成品入库单列表 */
    @GetMapping("/instock")
    public Result<List<Map<String, Object>>> instock(@RequestParam(required = false) Long workOrderId,
                                                     @RequestParam(required = false) Integer status) {
        return Result.success(productInstockReadMapper.pageInstock(workOrderId, status));
    }
}
