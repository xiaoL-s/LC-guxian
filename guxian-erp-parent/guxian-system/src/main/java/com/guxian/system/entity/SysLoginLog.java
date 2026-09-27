package com.guxian.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("login_ip")
    private String loginIp;

    @TableField("login_address")
    private String loginAddress;

    @TableField("browser")
    private String browser;

    @TableField("os")
    private String os;

    @TableField("login_status")
    private Integer loginStatus;

    @TableField("msg")
    private String msg;

    @TableField("login_time")
    private LocalDateTime loginTime;
}