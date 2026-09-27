package com.guxian.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class SysRoleDTO {
    private Long id;
    private String roleName;
    private String roleCode;
    private Integer sort;
    // 菜单id集合，前端树勾选后传过来
    private List<Long> menuIdList;
}
