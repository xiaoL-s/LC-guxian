package com.guxian.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 原料物料表 t_material_stock
 */
@Data
@TableName("t_material_stock")
public class TMaterialStock {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 物料唯一编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 1型材 2纱网 3五金配件 4辅材 */
    private Integer materialType;

    /** 规格型号 */
    private String spec;

    /** 单位：米/㎡/个/套 */
    private String unit;

    /** 当前库存数量 */
    private BigDecimal stockNum;

    /** 安全库存预警值 */
    private BigDecimal warnNum;

    /** 存放货架库位ID */
    private Long shelfId;

    /** 0禁用 1启用 */
    private Integer enable;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
