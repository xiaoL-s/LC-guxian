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
}