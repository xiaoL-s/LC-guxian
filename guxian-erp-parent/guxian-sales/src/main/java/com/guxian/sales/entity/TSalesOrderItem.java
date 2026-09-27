package com.guxian.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售订单明细
 * 宽高单位毫米(mm)，面积单位㎡
 */
@Data
@TableName("t_sales_order_item")
public class TSalesOrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    /** 订单ID */
    private Long orderId;

    /** 子单号（如 SO20260919001-01） */
    private String subOrderNo;

    /** 产品ID */
    private Long productId;

    /** 产品名称（快照） */
    private String productName;

    /** 产品类型/分类（快照） */
    private String productType;

    /** 品目（字典系列名称，如 8K三节系列） */
    private String itemCategory;

    /** 产品来源字典类型（如 style_8k_sanjie） */
    private String dictType;

    /** 颜色 */
    private String color;

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

    /** 方板规格（5号方板等） */
    private String squareBoard;

    /** 材质 */
    private String material;

    /** 开启方向 */
    private String openDirection;

    /** 单价（元/㎡，快照） */
    private BigDecimal unitPrice;

    /** 宽度（mm） */
    private BigDecimal width;

    /** 高度（mm） */
    private BigDecimal height;

    /** 扣宽（mm），净宽 = 总宽 - 扣宽 */
    private BigDecimal deductWidth;

    /** 扣宽后净宽（mm），参与面积计算的宽度 */
    private BigDecimal netWidth;

    /** 单扇面积（㎡）= 宽×高/1e6 */
    private BigDecimal singleArea;

    /** 最小起算方（㎡，快照） */
    private BigDecimal minArea;

    /** 计算方式：1按面积 2按件 */
    private Integer calcType;

    /** 单扇计费面积 = max(单扇面积, 最小起算方) */
    private BigDecimal chargeArea;

    /** 数量（扇/件） */
    private Integer num;

    /** 单位 */
    private String unit;

    /** 行总面积 = 计费面积×数量 */
    private BigDecimal itemTotalArea;

    /** 行金额 = 行总面积×单价 */
    private BigDecimal lineAmount;

    /** 行状态：0订单未受理 1订单已受理 2生产中 3已完工 4已发货 5已完成 9已取消 */
    private Integer itemStatus;

    /** 销售计价方式：1按面积 2按件 3按公式 */
    private Integer salePriceType;

    /** 行运费 */
    private BigDecimal freight;

    /** 行实收金额 */
    private BigDecimal receiveAmount;

    /** 行利润 */
    private BigDecimal profitAmount;

    /** 销售商/下单员 */
    private String salesOwner;

    /** 当前工序编码（生产预留） */
    private String processCode;

    /** 当前工序名称（生产预留） */
    private String processName;

    /** 生产流程（生产预留） */
    private String flowStatus;

    /** 生产进度%（生产预留） */
    private Integer produceProgress;

    /** 关联生产工单ID（生产预留） */
    private Long workOrderId;

    /** 明细备注 */
    private String remark;
}
