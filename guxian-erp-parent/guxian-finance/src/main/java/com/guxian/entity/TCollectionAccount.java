package com.guxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收款账户
 */
@Data
@TableName("t_collection_account")
public class TCollectionAccount {

    @TableId(type = IdType.AUTO)
    private Long accountId;

    /** 账户名称 */
    private String accountName;

    /** 户名 */
    private String holderName;

    /** 银行卡号 */
    private String bankCard;

    /** 账户金额 */
    private BigDecimal balance;

    /** 是否系统账号(1=是,切勿删除) */
    private Integer isSystem;

    /** 备注 */
    private String remark;

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
