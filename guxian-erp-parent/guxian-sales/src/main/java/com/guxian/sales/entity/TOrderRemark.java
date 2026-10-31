package com.guxian.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单备注记录（工作人员手动备注，含操作人/时间，随订单详情展示）
 */
@Data
@TableName("t_order_remark")
public class TOrderRemark implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 备注内容 */
    private String remark;

    /** 操作人ID（自动填充） */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /** 操作人姓名（冗余存储，登录用户 realName） */
    private String createByName;

    /** 创建时间（自动填充） */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
