package com.guxian.sales.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细列表底部"加载统计"
 */
@Data
public class SalesOrderStatsVO {
    /** 总数量（件/扇） */
    private BigDecimal totalNum = BigDecimal.ZERO;
    /** 总面积（㎡） */
    private BigDecimal totalArea = BigDecimal.ZERO;
    /** 总销售额 */
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /** 总未付金额 */
    private BigDecimal totalUnpaid = BigDecimal.ZERO;
    /** 总实收金额 */
    private BigDecimal totalReceive = BigDecimal.ZERO;
    /** 总款数（明细行数） */
    private Long totalRows = 0L;
}
