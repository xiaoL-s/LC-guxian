package com.guxian.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单主表
 */
@Data
@TableName("t_sales_order")
public class TSalesOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 订单状态：0订单未受理 1订单已受理 2生产中 3已完工 4已发货 5已完成 6已驳回 7已取消 */
    private Integer orderStatus;

    /** 订单类型：正常单/加急单/经销商单/样品单 */
    private String orderType;

    /** 客户ID */
    private Long customerId;

    /** 客户名称（冗余） */
    private String customerName;

    /** 联系人 */
    private String contact;

    /** 联系电话 */
    private String phone;

    /** 安装/收货地址 */
    private String address;

    /** 下单日期 */
    private LocalDate orderDate;

    /** 期望交期 */
    private LocalDate expectDate;

    /** 订单总面积（㎡） */
    private BigDecimal totalArea;

    /** 投影面积（㎡） */
    private BigDecimal projectionArea;

    /** 大板数 */
    private Integer bigBoardNum;

    /** 产品金额合计 */
    private BigDecimal productAmount;

    /** 特殊工艺加价 */
    private BigDecimal craftFee;

    /** 加急费 */
    private BigDecimal urgentFee;

    /** 运费 */
    private BigDecimal freight;

    /** 优惠/减价 */
    private BigDecimal discountAmount;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 财务状态：0未收账 1部分已收账 2已收账 3已结清 */
    private Integer financeStatus;

    /** 已付金额 */
    private BigDecimal paidAmount;

    /** 实收金额（累计收款） */
    private BigDecimal receiveAmount;

    /** 未付金额 = 总金额 - 实收金额 */
    private BigDecimal unpaidAmount;

    /** 利润 */
    private BigDecimal profitAmount;

    /** 毛利率 = 利润 / 总金额 */
    private BigDecimal grossProfitRate;

    /** 子单数量 */
    private Integer subOrderCount;

    /** 最近收款时间 */
    private LocalDateTime receiveTime;

    /** 审核人 */
    private Long auditBy;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 驳回原因 */
    private String rejectReason;

    /** 发货时间 */
    private LocalDateTime deliveryTime;

    /** 完成时间 */
    private LocalDateTime finishTime;

    /** 取消原因 */
    private String cancelReason;

    /** 备注/特殊要求 */
    private String remark;

    /** 品牌 */
    private String brand;

    /** 单位 */
    private String unit;

    /** 安装方式 */
    private String installType;

    /** 设计师 */
    private String designer;

    /** 拆单师 */
    private String splitter;

    /** 业务员 */
    private String salesman;

    /** 物流 */
    private String logistics;

    /** 终端地址 */
    private String terminalAddress;

    /** 客户来源 */
    private String customerSource;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
