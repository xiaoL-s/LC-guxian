package com.guxian.customer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.customer.dto.TCustomerFollowDTO;
import com.guxian.customer.entity.TCustomerFollow;
import com.guxian.customer.service.TCustomerFollowService;
import com.guxian.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/follow")
public class TCustomerFollowController {

    @Resource
    private TCustomerFollowService customerFollowService;

    /**
     * 根据客户ID分页查询跟进记录
     */
    @GetMapping("/page")
    public Result<IPage<TCustomerFollowDTO>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "customerId", required = false) Long customerId
    ){
        IPage<TCustomerFollow> page = new Page<>(pageNum, pageSize);
        IPage<TCustomerFollowDTO> pageData = customerFollowService.page(page, customerId);
        return Result.success(pageData);
    }

    /**
     * 新增/编辑跟进记录
     */
    @PostMapping("/save")
    @OperLog(operModule = "客户跟进记录", operType = "新增/编辑", operContent = "保存客户跟进记录")
    public Result<Void> save(@RequestBody TCustomerFollowDTO dto){
        customerFollowService.saveFollow(dto);
        return Result.success();
    }

    /**
     * 删除跟进记录
     */
    @DeleteMapping("/delete/{id}")
    @OperLog(operModule = "客户跟进记录", operType = "删除", operContent = "删除客户跟进记录")
    public Result<Void> delete(@PathVariable("id") Long id){
        customerFollowService.removeById(id);
        return Result.success();
    }
}
