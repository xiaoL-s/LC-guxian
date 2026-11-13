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
     * 获取当前用户的菜单树：按 userId -> 角色 -> 角色菜单 过滤，admin 全量可见。
     * 只配了子菜单没配父菜单时自动补全父级，避免树挂不上导致菜单整块消失。
     */
    @Override
    public List<SysMenuDTO> getUserMenuTree(Long userId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getDelFlag, 0);
        wrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> allMenuList = baseMapper.selectList(wrapper);
        List<SysMenu> visible = filterVisible(allMenuList, userId);
        return buildTree(convertToDto(visible), 0L);
    }

    /** 按用户角色筛选菜单；超管(id=1 或角色含 admin)返回全量 */
    private List<SysMenu> filterVisible(List<SysMenu> all, Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        if (userId == 1L) {
            return all;
        }
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds).eq(SysRole::getDelFlag, 0));
        boolean isAdmin = roles.stream().anyMatch(r -> "admin".equals(r.getRoleCode()));
        if (isAdmin) {
            return all;
        }
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        Set<Long> granted = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
        if (granted.isEmpty()) {
            return new ArrayList<>();
        }
        // 补全父级链
        Set<Long> finalIds = new HashSet<>(granted);
        for (Long id : new ArrayList<>(granted)) {
            Long parentId = parentIdOf(all, id);
            while (parentId != null && parentId != 0L && finalIds.add(parentId)) {
                parentId = parentIdOf(all, parentId);
            }
        }
        return all.stream().filter(m -> finalIds.contains(m.getId())).collect(Collectors.toList());
    }

    private Long parentIdOf(List<SysMenu> all, Long menuId) {
        for (SysMenu m : all) {
            if (m.getId().equals(menuId)) {
                return m.getParentId();
            }
        }
        return null;
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
