package com.guxian.produce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工人扫码报工明细表 t_work_report（计件工资）
 */
@Data
@TableName("t_work_report")
public class TWorkReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联生产工单ID */
    private Long workOrderId;

    /** 工序ID */
    private Long processId;

    /** 报工工人ID */
    private Long workerId;

    /** 本次合格完工数量 */
    private Integer qualifiedNum;

    /** 不良返工数量 */
    private Integer badNum;

    /** 工序计件单价快照 */
    private BigDecimal unitPrice;

    /** 本次计件工资 = 合格数量*单价 */
    private BigDecimal pieceWage;

    /** 报工提交时间 */
    private LocalDateTime reportTime;

    /** 操作端IP */
    private String createIp;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
