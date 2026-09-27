package com.guxian.produce.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 工单详情：工单头 + 订单信息 + 物料需求 + 工序进度 + 报工记录
 */
@Data
public class WorkOrderDetailVO {

    private Long workId;
    private String workNo;
    private Long salesOrderId;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    /** 订单类型（正常单/加急单…） */
    private String orderType;
    /** 下单日期 */
    private java.time.LocalDate orderDate;
    /** 计划交货日期（生产单打印"交货日期"） */
    private java.time.LocalDate expectDate;
    private Long shelfId;
    private String shelfName;
    private BigDecimal totalArea;
    private String workStatus;
    private String workStatusDesc;
    private Integer isRework;
    private String remark;
    private java.time.LocalDateTime finishTime;
    private java.time.LocalDateTime createTime;

    /** 订单明细行（只读） */
    private List<Map<String, Object>> orderItems;

    /** 物料需求清单 */
    private List<Map<String, Object>> materialList;

    /** 工序进度（含是否已完成） */
    private List<Map<String, Object>> processList;

    /** 报工记录 */
    private List<Map<String, Object>> reportList;
}
