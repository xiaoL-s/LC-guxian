package com.guxian.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.stock.entity.TMaterialStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface TMaterialStockMapper extends BaseMapper<TMaterialStock> {

    /** 盘点/单据过账：原子增减库存，防止库存扣为负 */
    @Update("UPDATE t_material_stock SET stock_num = stock_num + #{diff}, update_time = NOW() " +
            "WHERE id = #{id} AND stock_num + #{diff} >= 0")
    int adjustStock(@Param("id") Long id, @Param("diff") BigDecimal diff);
}
