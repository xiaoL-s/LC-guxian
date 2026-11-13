package com.guxian.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 接口级权限守卫：按「请求方法 + 路径前缀」判定当前登录用户是否越权。
 * <p>
 * 设计原则：
 * 1. 超级管理员（admin）全通；未命中任何规则的一律放行（只要求已登录）。
 * 2. 敏感模块（系统管理、财务、小程序配置）读操作也校验，避免"菜单看不到但接口能扒"。
 * 3. 业务模块（订单/客户/采购/库存/生产）只校验写操作，查询类对所有岗位开放。
 * 4. 与菜单权限保持一致：某个角色菜单里能看到的功能，接口不会被拦。
 * <p>
 * 注意：这里是"角色粒度的粗控制"，比逐接口注解更省维护，也能覆盖全部现有接口；
 * 需要更细的字段级控制时再补 @RequireRole 注解。
 */
public final class ApiPermissionGuard {

    /** 读操作也要校验的敏感前缀 → 允许角色 */
    private static final String[][] READ_AND_WRITE = {
            {"/system/user", "admin,clerk"},
            {"/system/role", "admin"},
            {"/system/menu", "admin"},
            {"/system/dict", "admin,clerk"},
            {"/system/printTemplate", "admin"},
            {"/system/operlog", "admin"},
            {"/finance", "admin,finance,boss"},
            {"/mini", "admin"},
    };

    /** 仅写操作校验（POST/PUT/DELETE/PATCH）前缀 → 允许角色 */
    private static final String[][] WRITE_ONLY = {
            {"/sales", "admin,clerk"},
            {"/customer", "admin,clerk"},
            {"/supplier", "admin,clerk"},
            {"/purchase", "admin,clerk"},
            {"/stock", "admin,clerk,prod_manager"},
            {"/production/workorder/report", "admin,worker,prod_manager"},
            {"/production/workorder/finish", "admin,prod_manager"},
            {"/production", "admin,prod_manager"},
    };

    /** 无需权限校验的路径（登录、退出、取自己的菜单） */
    private static final String[] WHITE_LIST = {
            "/system/login",
            "/system/logout",
            "/system/menu/getUserMenus",
            "/mini/auth",
    };

    private ApiPermissionGuard() {
    }

    /**
     * @return null 表示允许；否则返回拒绝原因（仅供日志/提示使用，不含内部细节）
     */
    public static String denyReason(String method, String uri, List<String> roleCodes) {
        if (uri == null) {
            return null;
        }
        String path = uri;
        int q = path.indexOf('?');
        if (q > -1) {
            path = path.substring(0, q);
        }
        if (isWhite(path)) {
            return null;
        }
        List<String> roles = roleCodes == null ? new ArrayList<>() : roleCodes;
        if (roles.contains("admin")) {
            return null;
        }
        boolean isWrite = !isReadMethod(method);

        for (String[] rule : READ_AND_WRITE) {
            if (match(path, rule[0])) {
                // 命中即判定：允许就放行，避免被后面更宽的规则再次拦下
                return allowed(roles, rule[1]) ? null : "当前账号没有该功能的查看权限，请联系管理员" + ("（" + rule[0] + "）");
            }
        }
        if (!isWrite) {
            return null;
        }
        for (String[] rule : WRITE_ONLY) {
            if (match(path, rule[0])) {
                return allowed(roles, rule[1]) ? null : "当前账号没有该功能的操作权限，请联系管理员" + ("（" + rule[0] + "）");
            }
        }
        return null;
    }

    private static boolean isWhite(String path) {
        for (String w : WHITE_LIST) {
            if (path.startsWith(w)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isReadMethod(String method) {
        return method == null
                || "GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method);
    }

    private static boolean match(String path, String prefix) {
        return path.startsWith(prefix + "/") || path.equals(prefix);
    }

    private static boolean allowed(List<String> roles, String allowedCsv) {
        List<String> allow = Arrays.asList(allowedCsv.split(","));
        for (String r : roles) {
            if (r != null && allow.contains(r.trim())) {
                return true;
            }
        }
        return false;
    }
}
