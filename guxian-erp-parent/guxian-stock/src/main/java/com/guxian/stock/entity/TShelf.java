package com.guxian.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 货架库位表 t_shelf
 */
@Data
@TableName("t_shelf")
public class TShelf {

    @TableId(type = IdType.AUTO)
    private Long shelfId;

    /** 货架编码 */
    private String shelfCode;

    /** 货架名称 */
    private String shelfName;

    /** 状态 1启用 0停用 */
    private Integer status;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
