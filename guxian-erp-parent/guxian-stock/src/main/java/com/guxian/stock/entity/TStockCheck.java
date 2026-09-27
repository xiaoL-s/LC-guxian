package com.guxian.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存盘点单 t_stock_check
 */
@Data
@TableName("t_stock_check")
public class TStockCheck {

    @TableId(value = "check_id", type = IdType.AUTO)
    private Long checkId;

    /** 盘点单号 */
    private String checkNo;

    /** 0草稿 1已过账 */
    private Integer checkStatus;

    /** 差异汇总 */
    private BigDecimal totalDiff;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
