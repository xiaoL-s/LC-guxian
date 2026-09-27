package com.guxian.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品档案
 */
@Data
@TableName("t_product")
public class TProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "product_id", type = IdType.AUTO)
    private Long productId;

    /** 产品编码 */
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 产品分类：平开纱窗/推拉纱窗/金刚网纱窗/折叠纱窗 */
    private String productType;

    /** 规格型号 */
    private String spec;

    /** 计价单位，默认㎡ */
    private String unit;

    /** 基础单价（元/㎡或元/件） */
    private BigDecimal unitPrice;

    /** 计价方式：1按面积 2按件 */
    private Integer priceType;

    /** 最小起算方（㎡） */
    private BigDecimal minArea;

    /** 默认颜色 */
    private String defaultColor;

    /** 默认材质 */
    private String defaultMaterial;

    /** 默认开启方向 */
    private String openDirection;

    /** 状态：1启用 0停用 */
    private Integer status;

    /** 备注 */
    private String remark;

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
