package com.guxian.produce.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 成品入库单查询（同库）
 */
@Mapper
public interface ProductInstockReadMapper {

    @Select("SELECT i.*, s.shelf_name AS shelfName, w.work_no AS workNo, o.order_no AS orderNo " +
            "FROM t_product_instock i " +
            "LEFT JOIN t_shelf s ON i.shelf_id = s.shelf_id " +
            "LEFT JOIN t_work_order w ON i.work_order_id = w.work_id " +
            "LEFT JOIN t_sales_order o ON i.order_id = o.order_id " +
            "WHERE (#{workOrderId} IS NULL OR i.work_order_id = #{workOrderId}) " +
            "AND (#{status} IS NULL OR i.instock_status = #{status}) " +
            "ORDER BY i.id DESC")
    List<Map<String, Object>> pageInstock(@Param("workOrderId") Long workOrderId, @Param("status") Integer status);
}
