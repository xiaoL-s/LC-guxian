package com.guxian.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 打印模板（系统管理）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_print_template")
public class SysPrintTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    /** 模板名称 */
    @TableField("template_name")
    private String templateName;

    /** 单据类型：销售订单 / 生产派工单 */
    @TableField("template_type")
    private String templateType;

    /** 纸张大小：A4 / A5 / 80mm小票 */
    @TableField("paper_size")
    private String paperSize;

    /** 模板内容（含 ${变量} 占位符） */
    @TableField("template_content")
    private String templateContent;

    /** 是否默认模板：1=是 0=否 */
    @TableField("is_default")
    private Integer isDefault;

    /** 状态：1=启用 0=禁用 */
    @TableField("status")
    private Integer status;

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
