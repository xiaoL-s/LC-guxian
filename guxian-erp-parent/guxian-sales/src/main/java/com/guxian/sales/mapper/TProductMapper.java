package com.guxian.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.sales.entity.TProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TProductMapper extends BaseMapper<TProduct> {

    /**
     * 查询已被逻辑删除的产品（忽略 del_flag），用于产品编码复用时避免唯一索引冲突
     */
    @Select("SELECT * FROM t_product WHERE product_code = #{productCode} AND del_flag = 1 LIMIT 1")
    TProduct selectDeletedByProductCode(@Param("productCode") String productCode);

    /**
     * 恢复逻辑删除标记
     */
    @Update("UPDATE t_product SET del_flag = 0 WHERE product_id = #{productId}")
    int restoreById(@Param("productId") Long productId);
}
