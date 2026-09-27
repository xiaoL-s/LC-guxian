package com.guxian.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.system.dto.SysRoleDTO;
import com.guxian.system.entity.SysMenu;
import com.guxian.system.entity.SysRole;
import com.guxian.system.entity.SysRoleMenu;
import com.guxian.system.mapper.SysMenuMapper;
import com.guxian.system.mapper.SysRoleMenuMapper;
import com.guxian.system.mapper.SysRoleMapper;
import com.guxian.system.service.SysRoleMenuService;
import com.guxian.system.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Resource
    private SysRoleMenuService sysRoleMenuService;

    @Resource
    private SysMenuMapper sysMenuMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(SysRoleDTO dto) {
        SysRole role = new SysRole();
        // 新增时，若同编码角色已被逻辑删除（unique(uk_role_code) 仍占用），先复活该行，
        // 避免"删除角色后用同一编码重新新增"直接抛 Duplicate entry 500
        Long reusedId = dto.getId();
        if (reusedId == null && dto.getRoleCode() != null) {
            SysRole deleted = baseMapper.selectDeletedByRoleCode(dto.getRoleCode());
            if (deleted != null) {
                baseMapper.restoreById(deleted.getId());
                reusedId = deleted.getId();
            }
        }
        role.setId(reusedId);
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setSort(dto.getSort());
        //保存/更新角色基础信息
        saveOrUpdate(role);
        Long roleId = role.getId();

        // 删除该角色旧的菜单关联
        LambdaQueryWrapper<SysRoleMenu> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuMapper.delete(delWrapper);

        // 批量新增选中菜单
        List<Long> menuIdList = dto.getMenuIdList();
        if(menuIdList != null && !menuIdList.isEmpty()){
            List<SysRoleMenu> list = menuIdList.stream()
                    .map(menuId -> {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(menuId);
                        return rm;
                    }).collect(Collectors.toList());
            sysRoleMenuService.saveBatch(list);

        }
    }

    // 根据角色id，查询绑定的所有菜单id（编辑弹窗回显树）
    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> menuList = sysRoleMenuMapper.selectList(wrapper);
        return menuList.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    /**
     * 获取菜单树，给前端el-tree渲染
     */
    @Override
    public List<SysMenu> getMenuTree() {
        // 查询全部菜单
        List<SysMenu> allMenu = sysMenuMapper.selectList(new LambdaQueryWrapper<>());
        // 构建树，parentId=0 为一级菜单
        return buildTree(allMenu, 0L);
    }

    // 递归构建菜单树
    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        return all.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .map(m -> {
                    m.setChildren(buildTree(all, m.getId()));
                    return m;
                }).collect(Collectors.toList());
    }
}
