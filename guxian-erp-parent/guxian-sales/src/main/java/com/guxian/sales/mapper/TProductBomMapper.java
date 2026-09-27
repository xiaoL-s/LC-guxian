package com.guxian.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.sales.dto.ProductBomDTO;
import com.guxian.sales.entity.TProductBom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TProductBomMapper extends BaseMapper<TProductBom> {

    /**
     * 按产品查询BOM并关联物料主数据（名称/规格/单位）
     */
    @Select("SELECT b.id, b.product_id AS productId, b.material_id AS materialId, " +
            "m.material_code AS materialCode, m.material_name AS materialName, m.spec AS spec, " +
            "m.unit AS unit, m.material_type AS materialType, " +
            "b.use_num AS useNum, b.loss_rate AS lossRate, b.sort AS sort " +
            "FROM t_product_bom b " +
            "LEFT JOIN t_material_stock m ON b.material_id = m.id " +
            "WHERE b.product_id = #{productId} AND b.del_flag = 0 " +
            "ORDER BY b.sort, b.id")
    List<ProductBomDTO> listBomWithMaterial(@Param("productId") Long productId);
}
