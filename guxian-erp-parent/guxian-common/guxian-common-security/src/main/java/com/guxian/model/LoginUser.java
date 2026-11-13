package com.guxian.model;

import lombok.Data;

/**
 * JWT解析后的登录用户模型
 */
@Data
public class LoginUser {
    /** 用户ID */
    private Long userId;
    /** 账号 */
    private String username;
    /** 真实姓名 */
    private String realName;
    /** 岗位类型：cut开料、assemble组装、inspect质检、package打包 */
    private String postType;
    /** 角色编码列表（admin/clerk/worker/prod_manager/finance/boss/hezuofang） */
    private java.util.List<String> roleCodes;
}