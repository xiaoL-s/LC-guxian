package com.guxian.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 出入库单据明细行
 */
@Data
public class BillItemDTO {

    /** 物料ID */
    private Long materialId;

    /** 数量（正数） */
    private BigDecimal num;

    /** 行备注 */
    private String remark;
}
