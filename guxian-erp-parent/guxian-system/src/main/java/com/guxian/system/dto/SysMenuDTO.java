package com.guxian.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class SysMenuDTO {
    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String perms;
    private Integer menuType;
    private Integer sort;
    /** 子菜单，前端渲染导航树 */
    private List<SysMenuDTO> children;
    private String icon;

    
}