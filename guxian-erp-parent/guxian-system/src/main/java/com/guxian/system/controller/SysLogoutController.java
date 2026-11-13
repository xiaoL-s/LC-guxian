package com.guxian.system.controller;

import com.guxian.result.Result;
import com.guxian.security.TokenBlacklistStore;
import com.guxian.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登出接口：把当前 token 拉黑，使其立即失效（不再等自然过期）。
 *
 * <p>POST /system/logout，需携带 Authorization: Bearer &lt;token&gt;。
 * 该路径在 {@code ApiPermissionGuard} 白名单内，任何角色都能登出；但 token 本身仍需通过拦截器校验。</p>
 */
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
@RestController
@RequestMapping("/system/logout")
public class SysLogoutController {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private TokenBlacklistStore tokenBlacklistStore;

    @PostMapping
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // 没带 token 说明本来就没登录态，直接按成功处理，前端照常清本地缓存
            return Result.success();
        }
        String token = authorization.substring(7);
        if (token.isBlank()) {
            return Result.success();
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            // TTL 取剩余有效期，token 过期后黑名单条目由 Redis 自动回收
            tokenBlacklistStore.revoke(token, jwtUtil.remainingSeconds(claims));
        } catch (Exception e) {
            // token 已过期或签名不合法：本来就用不了，无需再拉黑，登出仍算成功
        }
        return Result.success();
    }
}
