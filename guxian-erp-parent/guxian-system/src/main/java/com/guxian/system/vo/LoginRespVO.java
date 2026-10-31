package com.guxian.system.vo;
import lombok.Data;

@Data
public class LoginRespVO {
    private Long id;
    private String username;
    private String realName;
    private String token;
    /** 是否超级管理员：生产工单撤回等高风险操作仅超管可用 */
    private Boolean isAdmin;
}
