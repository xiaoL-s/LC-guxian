package com.guxian.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysUserDTO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String postType;
    private Integer status;
    private LocalDateTime createTime;
    // 前端多选角色id数组, 角色id集合，新增/编辑回显用
    private List<Long> roleIdList;
    // SysUserDTO.java
    // 角色名称集合（多角色）
    private List<String> roleNameList;
    // 拼接好的角色名称字符串，用于表格直接展示，例："管理员,文员"
    private String roleNames;
    private String menuNames;


}