package com.guxian.sales.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单明细行列表VO（"下单"/"订单明细"页面用，明细字段 + 订单头冗余字段）
 */
@Data
public class SalesOrderItemRowVO {
    /** 明细ID */
    private Long itemId;
    /** 订单ID */
    private Long orderId;

    // ---------------- 订单头 ----------------
    private String orderNo;
    private String subOrderNo;
    private String orderType;
    private Integer orderStatus;
    private String orderStatusDesc;
    private String orderStatusTag;
    private Integer financeStatus;
    private String financeStatusDesc;
    private String financeStatusTag;

    private Long customerId;
    private String customerName;
    private String contact;
    private String phone;
    private String terminalAddress;
    private String logistics;
    private String unit;
    private String brand;
    private String installType;
    private String designer;
    private String splitter;
    private String salesman;
    private String customerSource;

    private LocalDate orderDate;
    private LocalDate expectDate;
    private LocalDateTime orderCreateTime;

    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 订单未付金额 */
    private BigDecimal unpaidAmount;
    /** 订单运费 */
    private BigDecimal orderFreight;

    // ---------------- 明细行 ----------------
    private Long productId;
    private String productName;
    private String productType;
    private String itemCategory;
    private String dictType;
    private String color;
    private String material;
    private String netMaterial;
    private String handle;
    private String lockSet;
    private String handleDirection;
    private String openDirection;
    private String addRod;
    private String fixedBottom;
    private String squareBoard;

    /** 总宽 mm */
    private BigDecimal width;
    /** 总高 mm */
    private BigDecimal height;
    private BigDecimal deductWidth;
    private BigDecimal netWidth;

    private Integer num;
    private BigDecimal unitPrice;
    private Integer calcType;
    private Integer salePriceType;
    /** 单扇面积 */
    private BigDecimal singleArea;
    /** 计费面积 */
    private BigDecimal chargeArea;
    /** 行面积 */
    private BigDecimal itemTotalArea;
    /** 销售金额（行金额） */
    private BigDecimal lineAmount;
    /** 行运费 */
    private BigDecimal freight;
    /** 行实收金额 */
    private BigDecimal receiveAmount;
    /** 行利润 */
    private BigDecimal profitAmount;
    /** 毛利率 = 行利润/行金额 */
    private BigDecimal grossProfitRate;
    private String salesOwner;

    private Integer itemStatus;
    private String itemStatusDesc;

    /** 当前工序（生产预留） */
    private String processCode;
    private String processName;
    /** 生产流程（生产预留） */
    private String flowStatus;
    /** 生产进度%（生产预留） */
    private Integer produceProgress;
    private Long workOrderId;

    private String remark;
    private LocalDateTime createTime;
}
