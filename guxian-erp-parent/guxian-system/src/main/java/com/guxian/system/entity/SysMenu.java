package com.guxian.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单ID，0=顶级菜单
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 菜单名称
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 前端路由path
     */
    @TableField("path")
    private String path;

    /**
     * 权限标识 system:log:list
     */
    @TableField("perms")
    private String perms;

    /**
     * 菜单类型：1目录 2菜单页面 3按钮
     */
    @TableField("menu_type")
    private Integer menuType;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 逻辑删除 0正常，1删除
     */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

    /**
    *菜单小图标
    */
    @TableField("icon")
    private String icon;

    @TableField(exist = false)
    private List<SysMenu> children;

}