package com.guxian.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.system.dto.SysRoleDTO;
import com.guxian.system.entity.SysMenu;
import com.guxian.system.entity.SysRole;
import java.util.List;

public interface SysRoleService extends IService<SysRole> {
    void saveRole(SysRoleDTO dto);
    List<Long> getMenuIdsByRoleId(Long roleId);

    List<SysMenu> getMenuTree();

}
