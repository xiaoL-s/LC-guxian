package com.guxian.interceptor;

import com.guxian.exception.BusinessException;
import com.guxian.exception.ResultCodeEnum;
import com.guxian.context.UserContext;
import com.guxian.model.LoginUser;
import com.guxian.security.ApiPermissionGuard;
import com.guxian.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Token拦截器：校验token、解析用户（含角色）存入上下文，并按角色做接口级鉴权。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS预检请求（OPTIONS）直接放行，不做token校验，否则刷新/跨域场景会401
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(ResultCodeEnum.TOKEN_EXPIRE.getCode(), ResultCodeEnum.TOKEN_EXPIRE.getMsg());
        }
        token = token.substring(7);

        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            throw new BusinessException(ResultCodeEnum.TOKEN_EXPIRE.getCode(), ResultCodeEnum.TOKEN_EXPIRE.getMsg());
        }

        if (jwtUtil.isExpire(claims)) {
            throw new BusinessException(ResultCodeEnum.TOKEN_EXPIRE.getCode(), ResultCodeEnum.TOKEN_EXPIRE.getMsg());
        }

        // 封装登录用户存入上下文
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(Long.valueOf(claims.get("userId").toString()));
        // token中带username、realName，操作日志等场景需要使用
        loginUser.setUsername((String) claims.get("username"));
        loginUser.setRealName((String) claims.get("realName"));
        Object postType = claims.get("postType");
        loginUser.setPostType(postType == null ? "" : postType.toString());
        // 角色编码（登录时签发，接口级鉴权用）
        Object rolesClaim = claims.get("roles");
        List<String> roles = new ArrayList<>();
        if (rolesClaim != null && !rolesClaim.toString().isBlank()) {
            roles = new ArrayList<>(Arrays.asList(rolesClaim.toString().split(",")));
        }
        loginUser.setRoleCodes(roles);
        UserContext.setLoginUser(loginUser);

        // 接口级鉴权：只对真实接口（HandlerMethod）生效，静态资源等直接放行
        if (handler instanceof HandlerMethod) {
            String reason = ApiPermissionGuard.denyReason(request.getMethod(), request.getRequestURI(), roles);
            if (reason != null) {
                throw new BusinessException(ResultCodeEnum.NO_PERMISSION.getCode(), reason);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
