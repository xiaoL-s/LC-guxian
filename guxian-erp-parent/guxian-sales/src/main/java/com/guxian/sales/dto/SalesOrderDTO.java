package com.guxian.sales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单主从一体DTO（订单录入/编辑/详情）
 */
@Data
public class SalesOrderDTO {
    private Long orderId;
    private String orderNo;
    private Integer orderStatus;
    /** 状态文字（后端回填，仅展示） */
    private String orderStatusDesc;

    /** 订单类型：正常单/加急单/经销商单/样品单 */
    private String orderType;

    private Long customerId;
    private String customerName;
    private String contact;
    private String phone;
    private String address;

    /** 终端地址 */
    private String terminalAddress;
    /** 物流 */
    private String logistics;
    /** 单位 */
    private String unit;
    /** 品牌 */
    private String brand;
    /** 安装方式 */
    private String installType;
    /** 设计师 */
    private String designer;
    /** 拆单师 */
    private String splitter;
    /** 业务员 */
    private String salesman;
    /** 客户来源 */
    private String customerSource;

    private LocalDate orderDate;
    private LocalDate expectDate;

    /** 订单总面积（后端汇总） */
    private BigDecimal totalArea;
    /** 投影面积 */
    private BigDecimal projectionArea;
    /** 大板数 */
    private Integer bigBoardNum;

    /** 产品金额合计（后端汇总） */
    private BigDecimal productAmount;
    /** 特殊工艺加价 */
    private BigDecimal craftFee;
    /** 加急费 */
    private BigDecimal urgentFee;
    /** 运费 */
    private BigDecimal freight;
    /** 优惠/减价 */
    private BigDecimal discountAmount;
    /** 订单总金额（后端计算） */
    private BigDecimal totalAmount;

    /** 财务状态：0未收账 1部分已收账 2已收账 3已结清 */
    private Integer financeStatus;
    /** 财务状态文字（后端回填） */
    private String financeStatusDesc;
    /** 已付金额 */
    private BigDecimal paidAmount;
    /** 实收金额（累计） */
    private BigDecimal receiveAmount;
    /** 未付金额 */
    private BigDecimal unpaidAmount;
    /** 利润 */
    private BigDecimal profitAmount;
    /** 毛利率 */
    private BigDecimal grossProfitRate;
    /** 子单数量 */
    private Integer subOrderCount;
    /** 最近收款时间 */
    private LocalDateTime receiveTime;

    private String remark;

    /** 审核/流转信息（回显） */
    private Long auditBy;
    private LocalDateTime auditTime;
    private String rejectReason;
    private LocalDateTime deliveryTime;
    private LocalDateTime finishTime;
    private String cancelReason;
    private LocalDateTime createTime;
    /** 制单人ID */
    private Long createBy;

    /** 订单明细 */
    private List<SalesOrderItemDTO> itemList;
}
