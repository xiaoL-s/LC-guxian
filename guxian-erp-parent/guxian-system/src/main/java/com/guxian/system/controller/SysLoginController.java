package com.guxian.system.controller;

import com.guxian.result.Result;
import com.guxian.system.dto.LoginDTO;
import com.guxian.system.service.SysUserService;
import com.guxian.system.vo.LoginRespVO;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;


@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
@RestController
@RequestMapping("/system/login")
public class SysLoginController {

    @Resource
    private SysUserService sysUserService;

    /**
     * 简易登录接口
     * POST /system/login/doLogin
     */
    @PostMapping("/doLogin")
    public Result<LoginRespVO> doLogin(@RequestBody LoginDTO loginDTO) {
        return sysUserService.login(loginDTO);
    }
}