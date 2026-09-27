package com.guxian.mybatis.entity;

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

/**
 * 操作日志实体（放在 guxian-common-mybatis 公共模块，全局唯一）
 * 所有微服务统一使用该实体读写 sys_oper_log 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("real_name")
    private String realName;

    @TableField("oper_module")
    private String operModule;

    @TableField("oper_type")
    private String operType;

    @TableField("oper_content")
    private String operContent;

    @TableField("ip")
    private String ip;

    @TableField("oper_time")
    private LocalDateTime operTime;
}
