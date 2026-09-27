package com.guxian.system.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 打印模板 新增/编辑 入参
 */
@Data
public class SysPrintTemplateDTO {

    private Long templateId;

    /** 模板名称 */
    private String templateName;

    /** 单据类型 */
    private String templateType;

    /** 纸张大小 */
    private String paperSize;

    /** 模板内容 */
    private String templateContent;

    /** 是否默认：1=是 0=否 */
    private Integer isDefault;

    /** 状态：1=启用 0=禁用 */
    private Integer status;

    private LocalDateTime createTime;
}
