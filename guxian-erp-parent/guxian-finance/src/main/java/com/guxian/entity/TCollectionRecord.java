package com.guxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收款明细
 */
@Data
@TableName("t_collection_record")
public class TCollectionRecord {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    /** 收款账户ID */
    private Long accountId;

    /** 收款账户名称 */
    private String accountName;

    /** 收款金额 */
    private BigDecimal amount;

    /** 订单ID */
    private Long salesOrderId;

    /** 订单号 */
    private String orderNo;

    /** 客户ID */
    private Long customerId;

    /** 客户 */
    private String customerName;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人 */
    private String operatorName;

    /** 备注 */
    private String remark;

    /** 收款时间 */
    private LocalDateTime receiveTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer delFlag;
}
