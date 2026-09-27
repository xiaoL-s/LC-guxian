package com.guxian.stock.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 盘点单创建/录入请求
 */
@Data
public class StockCheckDTO {

    /** 盘点单ID（录入实盘时必填） */
    private Long checkId;

    /** 盘点单备注 */
    private String remark;

    /** 录入实盘：明细 [{materialId, realNum}]，materialId 对应盘点明细中的物料 */
    private List<CheckItemInput> items;

    @Data
    public static class CheckItemInput {
        private Long materialId;
        private BigDecimal realNum;
    }
}
