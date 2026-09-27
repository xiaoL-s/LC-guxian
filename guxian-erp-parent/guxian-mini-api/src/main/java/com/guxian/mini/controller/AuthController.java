package com.guxian.mini.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guxian.mini.config.JwtTokenService;
import com.guxian.mini.entity.SysUser;
import com.guxian.mini.mapper.SysUserMapper;
import com.guxian.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录：开发阶段用手机号直接登录；正式上线走 wxLogin（微信code换openid）
 */
@RestController
@RequestMapping("/mini/auth")
public class AuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    /** 手机号登录（车间工人直接输手机号） */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        SysUser u = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone).eq(SysUser::getDelFlag, 0));
        if (u == null) return Result.fail("手机号未绑定系统账号：" + phone);
        if (u.getStatus() != null && u.getStatus() != 1) return Result.fail("账号已禁用");
        String token = jwtTokenService.generate(u.getId(), u.getUsername(), u.getRealName(), u.getPostType());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", u.getId());
        data.put("realName", u.getRealName());
        data.put("postType", u.getPostType());
        return Result.success(data);
    }

    /**
     * 微信code登录（预留：正式上线需配置小程序appid/secret）
     * 当前先按code后面带的phone登录，联调通过后再接微信code2session
     */
    @PostMapping("/wxLogin")
    public Result<Map<String, Object>> wxLogin(@RequestBody Map<String, String> body) {
        // TODO 上线时：用 code 调 https://api.weixin.qq.com/sns/jscode2session 换 openid
        String phone = body.get("phone");
        if (phone == null || phone.isEmpty()) return Result.fail("开发阶段请传 phone");
        return login(body);
    }

    /** 当前登录人信息 */
    @GetMapping("/me")
    public Result<Map<String, Object>> me(@RequestHeader("Authorization") String token) {
        String realName = jwtTokenService.parse(token.substring(7)).get("realName", String.class);
        Map<String, Object> m = new HashMap<>();
        m.put("realName", realName);
        return Result.success(m);
    }
}

