package com.guxian.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.context.UserContext;
import com.guxian.system.entity.SysMenu;
import com.guxian.result.Result;
import com.guxian.system.dto.SysMenuDTO;
import com.guxian.system.service.SysMenuService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Resource
    private SysMenuService sysMenuService;

    /**
     * 获取当前登录用户导航菜单树
     */
    @GetMapping("/getUserMenus")
    public Result<List<SysMenuDTO>> getUserMenus(){
        Long userId = UserContext.getUserId();
        if (userId  == null)  {
            return Result.fail("未登录，请先登录");
        }
        List<SysMenuDTO> userMenuTree = sysMenuService.getUserMenuTree(userId);
        return Result.success(userMenuTree);
    }

    /**
     * 分页列表
     */
    @GetMapping("/page")
    public Result<IPage<SysMenu>> page(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "menuName", required = false) String menuName
    ){
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(menuName), SysMenu::getMenuName, menuName);
        wrapper.orderByAsc(SysMenu::getId);
        Page<SysMenu> page = new Page<>(pageNum, pageSize);
        IPage<SysMenu> pageData = sysMenuService.page(page, wrapper);
        return Result.success(pageData);
    }

    /**
     * 新增/编辑角色（保存角色+菜单权限）
     */
    @PostMapping("/save")
    @OperLog(operModule = "菜单管理", operType = "保存", operContent = "新增/修改菜单信息")
    public Result<Void> save(@RequestBody SysMenuDTO dto) {
        sysMenuService.saveMenu(dto);
        return Result.success();
    }

    /**
     * 删除菜单（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @OperLog(operModule = "菜单管理", operType = "删除", operContent = "删除菜单")
    public Result<Void> delete(@PathVariable("id") Long id){
        sysMenuService.removeById(id);
        System.out.println(id);
        return Result.success();
    }

    /**
     * 修改菜单
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysMenu menu){
        sysMenuService.updateById(menu);
        return Result.success();
    }

    /**
     * 获取单条详情
     */
    @GetMapping("/{id}")
    public Result<SysMenu> getInfo(@PathVariable("id") Long id){
        SysMenu menu = sysMenuService.getById(id);
        return Result.success(menu);
    }

    /**
     * 获取菜单树（角色管理分配菜单使用）
     */
    @GetMapping("/tree")
    public Result<List<SysMenuDTO>> tree(){
        List<SysMenuDTO> tree = sysMenuService.getMenuTree();
        return Result.success(tree);
    }
}
