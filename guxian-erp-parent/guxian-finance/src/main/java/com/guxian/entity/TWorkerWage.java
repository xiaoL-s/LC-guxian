package com.guxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_worker_wage")
public class TWorkerWage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workerId;

    private String wageMonth;

    private BigDecimal totalWage;

    private BigDecimal deductWage;

    private BigDecimal addWage;

    private BigDecimal realWage;

    private Integer status;

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