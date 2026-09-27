package com.guxian.produce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产工序字典 t_process_dict
 */
@Data
@TableName("t_process_dict")
public class TProcessDict {

    @TableId(value = "process_id", type = IdType.AUTO)
    private Long processId;

    /** 工序编码 */
    private String processCode;

    /** 工序名称 */
    private String processName;

    /** 生产排序号 */
    private Integer processSort;

    /** 对应工人岗位类型 */
    private String postType;

    /** 工序计件单价(元/件) */
    private BigDecimal unitPrice;

    /** 0禁用 1启用 */
    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
