package com.guxian.system.dto;

import lombok.Data;

import java.util.Map;

/**
 * 产品公式测试请求
 */
@Data
public class FormulaTestRequest {
    /** 字典类型（产品系列，如 style_sanjie） */
    private String dictType;
    /** 字典值（产品编码，如 1） */
    private String dictValue;
    /** 输入属性参数：总高/总宽/数量/下固定/加杆/单价/把手… */
    private Map<String, Object> params;
}
