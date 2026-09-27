package com.guxian.sales.mapper;

import com.guxian.sales.dto.MaterialOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 物料主数据只读Mapper（数据归属库存模块 t_material_stock，销售模块仅读取用于BOM配置）
 */
@Mapper
public interface TMaterialMapper {

    /**
     * 查询全部启用物料（BOM配置下拉）
     */
    @Select("SELECT id, material_code, material_name, spec, unit, material_type " +
            "FROM t_material_stock WHERE enable = 1 ORDER BY material_code")
    List<MaterialOptionDTO> listAllEnabled();
}
