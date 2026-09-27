package com.guxian.sales.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售订单明细DTO（录入/回显）
 * 宽高单位毫米(mm)
 */
@Data
public class SalesOrderItemDTO {
    private Long itemId;
    private Long orderId;
    /** 子单号（新增时后端生成） */
    private String subOrderNo;

    private Long productId;
    /** 产品名称（字典标签） */
    private String productName;
    private String productType;
    /** 品目（字典系列名称） */
    private String itemCategory;
    /** 产品字典类型 */
    private String dictType;

    private String color;
    private String material;
    private String openDirection;
    /** 网子 */
    private String netMaterial;
    /** 把手 */
    private String handle;
    /** 锁具 */
    private String lockSet;
    /** 把手方向 */
    private String handleDirection;
    /** 加杆 */
    private String addRod;
    /** 下固定 */
    private String fixedBottom;
    /** 方板规格 */
    private String squareBoard;

    /** 单价（元/㎡ 或 元/件） */
    private BigDecimal unitPrice;

    /** 总宽 mm */
    private BigDecimal width;
    /** 总高 mm */
    private BigDecimal height;
    /** 扣宽 mm */
    private BigDecimal deductWidth;
    /** 净宽 mm（=总宽-扣宽，后端计算） */
    private BigDecimal netWidth;

    /** 单扇面积（后端计算，前端传也会被重算覆盖） */
    private BigDecimal singleArea;
    /** 最小起算方（取自产品，可被明细覆盖） */
    private BigDecimal minArea;
    /** 单扇计费面积（后端计算） */
    private BigDecimal chargeArea;
    /** 计算方式：1按面积 2按件 */
    private Integer calcType;

    /** 数量 */
    private Integer num;
    /** 单位 */
    private String unit;
    /** 行总面积（后端计算） */
    private BigDecimal itemTotalArea;
    /** 行金额（后端计算） */
    private BigDecimal lineAmount;

    /** 行状态 */
    private Integer itemStatus;
    /** 行状态文字（后端回填） */
    private String itemStatusDesc;
    /** 销售计价方式 */
    private Integer salePriceType;
    /** 行运费 */
    private BigDecimal freight;
    /** 行实收金额 */
    private BigDecimal receiveAmount;
    /** 行利润 */
    private BigDecimal profitAmount;
    /** 销售商/下单员 */
    private String salesOwner;

    /** 当前工序（生产预留） */
    private String processCode;
    private String processName;
    /** 生产流程（生产预留） */
    private String flowStatus;
    /** 生产进度%（生产预留） */
    private Integer produceProgress;
    /** 关联工单ID（生产预留） */
    private Long workOrderId;

    private String remark;
}
