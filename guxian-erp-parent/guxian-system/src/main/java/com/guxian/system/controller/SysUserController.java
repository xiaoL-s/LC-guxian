package com.guxian.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.system.dto.SysUserDTO;
import com.guxian.system.entity.SysUser;
import com.guxian.result.Result;
import com.guxian.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Resource
    private SysUserService userService;

    @GetMapping("/page")
    public Result<IPage<SysUserDTO>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "realName", required = false) String realName
    )
    {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(realName), SysUser::getRealName, realName);
        Page<SysUser> page = new Page<>(pageNum,pageSize);
        IPage<SysUserDTO> pageData = userService.page(page, wrapper);
        return Result.success(pageData);
    }


    @PostMapping("/save")
    @OperLog(operModule = "员工管理", operType = "保存", operContent = "新增/修改员工信息")
    public Result<Void> save(@RequestBody SysUserDTO dto) {
        userService.saveUser(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperLog(operModule = "员工管理", operType = "删除", operContent = "删除员工")
    public Result<Void> delete(@PathVariable("id") Long id) {
        userService.removeById(id);
        System.out.println(id);
        return Result.success();
    }

    @GetMapping("/getRoleIds/{userId}")
    public Result<List<Long>> getRoleIds(@PathVariable("userId") Long userId) {
        List<Long> roleIds = userService.getRoleIdsByUserId(userId);
        return Result.success(roleIds);
    }

    @PutMapping("/resetPwd/{userId}")
    public Result<Void> resetPwd(@PathVariable("userId") Long userId) {
        userService.resetPwd(userId);
        return Result.success();
    }

}
