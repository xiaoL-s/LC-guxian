package com.guxian.customer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.customer.dto.TCustomerDTO;
import com.guxian.customer.entity.TCustomer;
import com.guxian.result.Result;
import com.guxian.customer.service.TCustomerService;
import com.guxian.annotation.OperLog;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/customer/list")
public class TCustomerController {

    @Resource
    private TCustomerService customerService;

    /**
     * 客户分页查询（前端页面列表+搜索）
     */
    @GetMapping("/page")
    public Result<IPage<TCustomerDTO>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "phone", required = false) String phone) {

        Page<TCustomer> page = new Page<>(pageNum, pageSize);
        return Result.success(customerService.pageCustomer(page, customerName, phone));
    }

    /**
     * 根据id查询客户详情（编辑弹窗回显）
     */
    @GetMapping("/{customerId}")
    public Result<TCustomer> getInfo(@PathVariable("customerId") Long customerId) {
        return Result.success(customerService.getById(customerId));
    }

    /**
     * 新增/编辑一体接口（和角色/用户保持一致，传customerId就是编辑，不传就是新增）
     */
    @PostMapping("/save")
    @OperLog(operModule = "客户管理", operType = "保存", operContent = "新增/编辑客户信息")
    public Result<Void> save(@RequestBody TCustomerDTO dto) {
        customerService.saveCustomer(dto);
        return Result.success();
    }

    /**
     * 删除（逻辑删除，单个删除）
     */
    @DeleteMapping("/{customerId}")
    @OperLog(operModule = "客户管理", operType = "删除", operContent = "删除客户")
    public Result<Void> remove(@PathVariable("customerId") Long customerId) {
        customerService.removeById(customerId);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    @OperLog(operModule = "客户管理", operType = "批量删除", operContent = "批量删除客户")
    public Result<Void> batchRemove(@RequestBody List<Long> idList) {
        customerService.removeByIds(idList);
        return Result.success();
    }

    /**
     * 获取全部客户下拉（给订单页面选择客户用）
     */
    @GetMapping("/listAll")
    public Result<List<TCustomer>> listAll() {
        LambdaQueryWrapper<TCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TCustomer::getCustomerId);
        return Result.success(customerService.list(wrapper));
    }
}
