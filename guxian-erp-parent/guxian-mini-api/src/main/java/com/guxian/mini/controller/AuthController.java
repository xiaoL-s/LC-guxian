package com.guxian.mini.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guxian.mini.config.JwtTokenService;
import com.guxian.mini.entity.SysUser;
import com.guxian.mini.mapper.SysUserMapper;
import com.guxian.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工人端登录：员工姓名 + 员工密码（BCrypt 校验）
 * 姓名支持输入：登录账号(username) 或 真实姓名(realName)，两者均可登录
 */
@RestController
@RequestMapping("/mini/auth")
public class AuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** 姓名/账号 + 密码登录 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String password = body.get("password");
        if (!StringUtils.hasText(name) || !StringUtils.hasText(password)) {
            return Result.fail("请输入员工姓名和密码");
        }
        name = name.trim();
        // 1) 优先按登录账号精确匹配
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, name).eq(SysUser::getDelFlag, 0).last("LIMIT 1"));
        // 2) 未命中再按真实姓名匹配
        if (user == null) {
            List<SysUser> byReal = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getRealName, name).eq(SysUser::getDelFlag, 0));
            if (byReal.size() > 1) {
                return Result.fail("姓名【" + name + "】存在多人，请联系管理员用登录账号登录");
            }
            if (!byReal.isEmpty()) {
                user = byReal.get(0);
            }
        }
        if (user == null) {
            return Result.fail("员工不存在：" + name);
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            return Result.fail("账号已禁用");
        }
        // 密码校验：BCrypt 优先；历史明文账号（未加密）直接比对兜底，保证老账号可登录
        String stored = user.getPassword() == null ? "" : user.getPassword();
        boolean ok;
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            ok = encoder.matches(password, stored);
        } else {
            ok = stored.equals(password);
        }
        if (!ok) {
            return Result.fail("密码错误");
        }
        String token = jwtTokenService.generate(user.getId(), user.getUsername(), user.getRealName(), user.getPostType());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("realName", user.getRealName());
        data.put("username", user.getUsername());
        data.put("postType", user.getPostType());
        return Result.success(data);
    }

    /**
     * 微信code登录（预留：正式上线需配置小程序appid/secret）
     * 当前按 code 后带姓名+密码方式登录，联调通过后再接微信 code2session
     */
    @PostMapping("/wxLogin")
    public Result<Map<String, Object>> wxLogin(@RequestBody Map<String, String> body) {
        // TODO 上线时：用 code 调 https://api.weixin.qq.com/sns/jscode2session 换 openid
        String name = body.get("name");
        if (!StringUtils.hasText(name)) {
            return Result.fail("开发阶段请传 name 与 password");
        }
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
