package com.guxian.context;

import com.guxian.model.LoginUser;

/**
 * 用户上下文，存放当前线程登录用户信息，全局可获取登录人ID、岗位
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> USER_THREAD_LOCAL = new ThreadLocal<>();

    /** 设置登录用户 */
    public static void setLoginUser(LoginUser loginUser) {
        USER_THREAD_LOCAL.set(loginUser);
    }

    /** 获取完整登录用户 */
    public static LoginUser getLoginUser() {
        return USER_THREAD_LOCAL.get();
    }

    /** 获取登录用户ID */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    /** 获取当前用户岗位类型 cut/assemble/inspect/package */
    public static String getPostType() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getPostType();
    }

    /** 清除上下文（拦截器结束必须调用，防止内存泄漏） */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}