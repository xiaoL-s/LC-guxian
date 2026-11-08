package com.guxian.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.system.dto.SysMenuDTO;
import com.guxian.system.entity.SysMenu;
import com.guxian.system.entity.SysRole;
import com.guxian.system.entity.SysRoleMenu;
import com.guxian.system.entity.SysUserRole;
import com.guxian.system.mapper.SysMenuMapper;
import com.guxian.system.mapper.SysRoleMapper;
import com.guxian.system.mapper.SysRoleMenuMapper;
import com.guxian.system.mapper.SysUserRoleMapper;
import com.guxian.system.service.SysMenuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Resource
    private SysRoleMapper sysRoleMapper;

    /**
     * 获取当前用户的菜单树（所有角色都能看到全部菜单，权限控制在按钮级别）
     */
    @Override
    public List<SysMenuDTO> getUserMenuTree(Long userId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getDelFlag, 0);
        wrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> allMenuList = baseMapper.selectList(wrapper);
        List<SysMenuDTO> dtoList = convertToDto(allMenuList);
        return buildTree(dtoList, 0L);
    }

    /**
     * 角色分配菜单：获取全部菜单树（给el-tree）
     */
    @Override
    public List<SysMenuDTO> getMenuTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getDelFlag, 0);
        wrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> allMenuList = baseMapper.selectList(wrapper);
        List<SysMenuDTO> dtoList = convertToDto(allMenuList);
        return buildTree(dtoList, 0L);
    }

    @Override
    public List<SysMenuDTO> getMenuTreeByUserId(Long userId) {
        return getUserMenuTree(userId);
    }


    /**
     * 递归构建菜单树
     */
    private List<SysMenuDTO> buildTree(List<SysMenuDTO> allDto, Long parentId){
        List<SysMenuDTO> tree = new ArrayList<>();
        for(SysMenuDTO dto : allDto){
            if(parentId.equals(dto.getParentId())){
                dto.setChildren(buildTree(allDto, dto.getId()));
                tree.add(dto);
            }
        }
        return tree;
    }

    /**
     * entity转DTO
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
            save(menu);
        }else{
            updateById(menu);
        }
    }
}
