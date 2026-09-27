package com.guxian.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品BOM（物料清单）
 * 物料主数据来自库存模块 t_material_stock（同库）
 */
@Data
@TableName("t_product_bom")
public class TProductBom implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 产品ID */
    private Long productId;

    /** 物料ID（t_material_stock.id） */
    private Long materialId;

    /** 单位用量（每㎡或每件用量，按物料单位） */
    private BigDecimal useNum;

    /** 损耗率（默认0.05） */
    private BigDecimal lossRate;

    /** 排序 */
    private Integer sort;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
