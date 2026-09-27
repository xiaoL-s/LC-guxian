package com.guxian.produce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 成品入库单据 t_product_instock
 */
@Data
@TableName("t_product_instock")
public class TProductInstock {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联工单ID */
    private Long workOrderId;

    /** 关联销售订单ID */
    private Long orderId;

    /** 入库货架库位ID */
    private Long shelfId;

    /** 入库成品总数量 */
    private Integer inNum;

    /** 1待入库 2已入库完成 */
    private Integer instockStatus;

    /** 操作仓管员ID */
    private Long stockUserId;

    /** 实际入库时间 */
    private LocalDateTime instockTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
