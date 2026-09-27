package com.guxian.sales.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 产品BOM行DTO（含物料主数据展示字段）
 */
@Data
public class ProductBomDTO {
    private Long id;
    private Long productId;
    private Long materialId;

    /** 物料编码（来自 t_material_stock） */
    private String materialCode;
    /** 物料名称 */
    private String materialName;
    /** 规格型号 */
    private String spec;
    /** 物料单位 */
    private String unit;
    /** 物料分类 */
    private Integer materialType;

    /** 单位用量 */
    private BigDecimal useNum;
    /** 损耗率 */
    private BigDecimal lossRate;
    /** 排序 */
    private Integer sort;
}
