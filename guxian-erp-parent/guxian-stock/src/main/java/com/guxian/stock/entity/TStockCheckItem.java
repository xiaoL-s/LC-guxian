package com.guxian.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点单明细 t_stock_check_item
 */
@Data
@TableName("t_stock_check_item")
public class TStockCheckItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkId;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String unit;

    /** 账面数 */
    private BigDecimal bookNum;

    /** 实盘数 */
    private BigDecimal realNum;

    /** 差异 = 实盘 - 账面 */
    private BigDecimal diffNum;

    private String remark;
}
