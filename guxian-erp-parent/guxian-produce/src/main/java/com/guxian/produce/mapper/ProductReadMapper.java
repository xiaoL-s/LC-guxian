package com.guxian.produce.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 只读访问系统字典里的产品数据（同库共享）
 * 产品 = sys_dict_data 中 dict_type 以 style_ 开头的字典项，dict_label 即产品名称
 */
@Mapper
public interface ProductReadMapper {

    /** 查询全部产品名称（按系列分组排序） */
    @Select("SELECT dict_label FROM sys_dict_data " +
            "WHERE dict_type REGEXP '^style_' AND del_flag = 0 AND status = 1 " +
            "ORDER BY dict_type, sort")
    List<String> selectAllProductNames();
}
