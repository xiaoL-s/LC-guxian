package com.guxian.sales.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 物料下拉选项（只读库存模块物料主数据 t_material_stock）
 */
@Data
public class MaterialOptionDTO {
    private Long id;
    private String materialCode;
    private String materialName;
    private String spec;
    private String unit;
    private Integer materialType;
}
