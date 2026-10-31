package com.guxian.produce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工序产品计件价 t_process_product_price
 * 同一个工序，不同产品的计件单价不同（如：下料工序，护童款3元/件、三节款2元/件）
 * 报工时按"工单产品 + 当前工序"查本表计价，查不到再回退工序字典统一单价
 */
@Data
@TableName("t_process_product_price")
public class TProcessProductPrice {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 工序ID（关联 t_process_dict.process_id） */
    private Long processId;

    /** 产品名称（与订单明细 productName 一致，通常来自字典管理） */
    private String productName;

    /** 计件单价（元/件） */
    private BigDecimal price;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
