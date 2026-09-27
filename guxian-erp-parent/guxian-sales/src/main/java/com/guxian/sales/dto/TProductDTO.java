package com.guxian.sales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品档案DTO
 */
@Data
public class TProductDTO {
    private Long productId;
    private String productCode;
    private String productName;
    private String productType;
    private String spec;
    private String unit;
    private BigDecimal unitPrice;
    private Integer priceType;
    private BigDecimal minArea;
    private String defaultColor;
    private String defaultMaterial;
    private String openDirection;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
