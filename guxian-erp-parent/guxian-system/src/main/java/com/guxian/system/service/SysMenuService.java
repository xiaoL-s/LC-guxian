package com.guxian.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.system.dto.SysMenuDTO;
import com.guxian.system.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据用户id获取该用户拥有的菜单树，返回给前端导航栏
     * @param userId 当前登录用户id
     * @return 树形菜单
     */
    List<SysMenuDTO> getUserMenuTree(Long userId);

    // 获取菜单树（角色分配菜单el-tree使用）
    List<SysMenuDTO> getMenuTree();
    // 根据用户id获取菜单树，登录后侧边栏渲染
    List<SysMenuDTO> getMenuTreeByUserId(Long userId);

    void saveMenu(SysMenuDTO dto);
}
