package com.guxian.interceptor;

import com.guxian.exception.BusinessException;
import com.guxian.exception.ResultCodeEnum;
import com.guxian.context.UserContext;
import com.guxian.model.LoginUser;
import com.guxian.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Token拦截器：校验token、解析用户存入上下文
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
        loginUser.setPostType(claims.get("postType").toString());
        UserContext.setLoginUser(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
