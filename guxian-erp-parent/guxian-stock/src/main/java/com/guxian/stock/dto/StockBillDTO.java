package com.guxian.stock.dto;

import lombok.Data;

import java.util.List;

/**
 * 出入库单据：采购入库(2)/退货出库(5)，一次可含多个物料
 */
@Data
public class StockBillDTO {

    /** 单据类型：2采购入库 5退货出库 */
    private Integer bizType;

    /** 单据备注 */
    private String remark;

    /** 明细行 */
    private List<BillItemDTO> items;
}
