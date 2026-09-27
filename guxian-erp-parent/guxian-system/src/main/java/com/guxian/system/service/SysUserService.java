package com.guxian.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.system.dto.LoginDTO;
import com.guxian.system.dto.SysUserDTO;
import com.guxian.system.entity.SysUser;
import com.guxian.result.Result;
import com.guxian.system.vo.LoginRespVO;

import java.util.List;

public interface SysUserService extends IService<SysUser> {
    /**
     * 简易登录校验
     */
    Result<LoginRespVO> login(LoginDTO dto);

    void saveUser(SysUserDTO dto);
    void resetPwd(Long userId);
    List<Long> getRoleIdsByUserId(Long userId);

    IPage<SysUserDTO> page(IPage<SysUser> page, LambdaQueryWrapper<SysUser> wrapper);
}