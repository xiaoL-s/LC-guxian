package com.guxian.produce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产工单主表 t_work_order
 */
@Data
@TableName("t_work_order")
public class TWorkOrder {

    @TableId(value = "work_id", type = IdType.AUTO)
    private Long workId;

    /** 工单号(二维码内容) */
    private String workNo;

    /** 销售订单ID */
    private Long salesOrderId;

    /** 客户ID */
    private Long customerId;

    /** 预分配入库货架ID */
    private Long shelfId;

    /** 工单总面积 */
    private BigDecimal totalArea;

    /** 工单状态：WAIT_PROCESS待生产 PROCESSING生产中 FINISHED已完工 CANCELED已取消 */
    private String workStatus;

    /** 是否返工 0否1是 */
    private Integer isRework;

    /** 完工时间 */
    private LocalDateTime finishTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
