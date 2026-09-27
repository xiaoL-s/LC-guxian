package com.guxian.sales.vo;

import lombok.Data;

/**
 * 字典选项（产品、颜色、网子、把手、锁具等均可由字典管理维护）
 */
@Data
public class DictOptionVO {
    /** 字典类型，如 style_8k_sanjie / color / net */
    private String dictType;
    /** 字典类型名称，如 8K三节系列 */
    private String typeName;
    /** 字典值 */
    private String value;
    /** 字典标签（下拉展示） */
    private String label;
    /** 排序 */
    private Integer sort;

    public DictOptionVO() {
    }

    public DictOptionVO(String dictType, String typeName, String value, String label, Integer sort) {
        this.dictType = dictType;
        this.typeName = typeName;
        this.value = value;
        this.label = label;
        this.sort = sort;
    }
}
