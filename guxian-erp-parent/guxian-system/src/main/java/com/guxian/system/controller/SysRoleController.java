package com.guxian.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.system.dto.SysRoleDTO;
import com.guxian.system.entity.SysMenu;
import com.guxian.system.entity.SysRole;
import com.guxian.result.Result;
import com.guxian.system.service.SysRoleService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Resource
    private SysRoleService roleService;

    /**
     * 角色分页查询
     */
    @GetMapping("/page")
    public Result<IPage<SysRole>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "roleName", required = false) String roleName
    ) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRole> wrapper
                = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName);
        IPage<SysRole> pageData = roleService.page(page, wrapper);
        return Result.success(pageData);
    }

    /**
     * 新增/编辑角色（保存角色+菜单权限）
     */
    @PostMapping("/save")
    @OperLog(operModule = "角色管理", operType = "保存", operContent = "新增/修改角色信息")
    public Result<Void> save(@RequestBody SysRoleDTO dto) {
        roleService.saveRole(dto);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @OperLog(operModule = "角色管理", operType = "删除", operContent = "删除角色")
    public Result<Void> delete(@PathVariable("id") Long id) {
        roleService.removeById(id);
        return Result.success();
    }

    /**
     * 根据角色ID获取绑定的菜单ID数组（编辑弹窗回显勾选）
     */
    @GetMapping("/getMenuIds/{roleId}")
    public Result<List<Long>> getMenuIds(@PathVariable("roleId") Long roleId) {
        List<Long> menuIdList = roleService.getMenuIdsByRoleId(roleId);
        return Result.success(menuIdList);
    }

    @GetMapping("/listAll")
    public Result<List<SysRole>> listAll() {
        List<SysRole> list = roleService.list();
        return Result.success(list);
    }

    /**
     * 获取全部菜单树（前端渲染菜单选择树）
     */
    @GetMapping("/menuTree")
    public Result<List<SysMenu>> menuTree() {
        List<SysMenu> tree = roleService.getMenuTree();
        return Result.success(tree);
    }

}
