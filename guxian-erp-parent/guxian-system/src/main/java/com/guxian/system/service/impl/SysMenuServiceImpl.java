package com.guxian.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.system.dto.SysMenuDTO;
import com.guxian.system.entity.SysMenu;
import com.guxian.system.mapper.SysMenuMapper;
import com.guxian.system.service.SysMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    /**
     * 获取当前用户的菜单树【你原来写的，保留】
     */
    @Override
    public List<SysMenuDTO> getUserMenuTree(Long userId) {
        // todo 这里后续需要联表 sys_user_role、sys_role_menu 查询该用户拥有的菜单ID
        //【现阶段调试】先直接查询全部有效菜单（方便前端先把导航栏跑通，后面再做权限过滤）
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getDelFlag, 0);
        wrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> allMenuList = baseMapper.selectList(wrapper);

        // entity转dto
        List<SysMenuDTO> dtoList = convertToDto(allMenuList);
        // 构建树形：parentId=0为根节点
        return buildTree(dtoList, 0L);
    }

    /**
     * 角色分配菜单：获取全部菜单树（给el-tree）【新增实现】
     */
    @Override
    public List<SysMenuDTO> getMenuTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getDelFlag,0);
        wrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> allMenuList = baseMapper.selectList(wrapper);
        List<SysMenuDTO> dtoList = convertToDto(allMenuList);
        return buildTree(dtoList,0L);
    }

    @Override
    public List<SysMenuDTO> getMenuTreeByUserId(Long userId) {
        // 复用用户菜单方法，后续可单独改造，当前直接复用
        return getUserMenuTree(userId);
    }


    /**
     * 递归构建菜单树【完全保留你写好的代码！】
     */
    private List<SysMenuDTO> buildTree(List<SysMenuDTO> allDto, Long parentId){
        List<SysMenuDTO> tree = new ArrayList<>();
        for(SysMenuDTO dto : allDto){
            if(parentId.equals(dto.getParentId())){
                //递归找子节点
                dto.setChildren(buildTree(allDto, dto.getId()));
                tree.add(dto);
            }
        }
        return tree;
    }

    /**
     * entity转DTO【保留你的代码，补齐完整】
     */
    private List<SysMenuDTO> convertToDto(List<SysMenu> menuList){
        return menuList.stream().map( menu ->{
            SysMenuDTO dto = new SysMenuDTO();
            dto.setId(menu.getId());
            dto.setParentId(menu.getParentId());
            dto.setMenuName(menu.getMenuName());
            dto.setPath(menu.getPath());
            dto.setPerms(menu.getPerms());
            dto.setMenuType(menu.getMenuType());
            dto.setSort(menu.getSort());
            dto.setIcon(menu.getIcon());
            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void saveMenu(SysMenuDTO dto) {
        SysMenu menu = BeanUtil.copyProperties(dto, SysMenu.class);
        if(dto.getId() == null){
            // 新增
            save(menu);
        }else{
            // 编辑
            updateById(menu);
        }
    }
}
