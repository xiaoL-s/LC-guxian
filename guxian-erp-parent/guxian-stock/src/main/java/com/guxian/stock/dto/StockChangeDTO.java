package com.guxian.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存变动请求（入库/出库/盘点调整）
 */
@Data
public class StockChangeDTO {

    /** 物料ID */
    private Long materialId;

    /** 变动数量（入库为正、出库为负） */
    private BigDecimal changeNum;

    /** 业务类型：1期初入库 2采购入库 3生产领料 4盘点调整 5退货出库 */
    private Integer bizType;

    /** 关联单类型 */
    private String relateType;

    /** 关联单号 */
    private String relateNo;

    /** 备注 */
    private String remark;
}
