package com.guxian.produce.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 工序报工请求
 */
@Data
public class WorkReportDTO {

    /** 工单ID */
    private Long workId;

    /** 工序ID */
    private Long processId;

    /** 订单明细行ID（一个工单多产品时，按产品分别报工计价） */
    private Long itemId;

    /** 报工工人ID */
    private Long workerId;

    /** 合格数量 */
    private Integer qualifiedNum;

    /** 不良数量 */
    private Integer badNum;

    /** 备注 */
    private String remark;
}
