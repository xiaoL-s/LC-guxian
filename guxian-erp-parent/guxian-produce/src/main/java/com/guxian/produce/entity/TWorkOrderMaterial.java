package com.guxian.produce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工单物料需求清单 t_work_order_material（拆单算料结果）
 */
@Data
@TableName("t_work_order_material")
public class TWorkOrderMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单ID */
    private Long workId;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String unit;

    /** 需求数量(含损耗) */
    private BigDecimal requireNum;

    /** 已领数量 */
    private BigDecimal pickedNum;

    /** 1按面积㎡ 2按件 */
    private Integer calcType;

    /** 损耗率 */
    private BigDecimal lossRate;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
