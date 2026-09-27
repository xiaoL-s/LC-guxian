package com.guxian.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水表 t_stock_record
 */
@Data
@TableName("t_stock_record")
public class TStockRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;

    private String materialCode;

    private String materialName;

    /** 1期初入库 2采购入库 3生产领料 4盘点调整 5退货出库 */
    private Integer bizType;

    /** 入库数量 */
    private BigDecimal inNum;

    /** 出库数量 */
    private BigDecimal outNum;

    /** 变动后库存 */
    private BigDecimal afterNum;

    /** 关联单类型 WORK_ORDER/PURCHASE */
    private String relateType;

    /** 关联单号 */
    private String relateNo;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
