package com.guxian.produce.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 成品入库单查询（同库）
 * 除入库单自身字段外，一并带出：入库操作人（用户名/姓名/电话）、客户与联系电话、
 * 该订单的产品名称/产品类型/产品数量，便于"库存里直接看到入库信息和录入用户信息"。
 */
@Mapper
public interface ProductInstockReadMapper {

    @Select("SELECT i.*, s.shelf_name AS shelfName, w.work_no AS workNo, o.order_no AS orderNo, " +
            "o.customer_name AS customerName, o.phone AS customerPhone, " +
            "u.username AS userName, u.real_name AS realName, u.phone AS userPhone, " +
            "it.productNames AS productName, it.productTypes AS productType, it.productNum AS productNum " +
            "FROM t_product_instock i " +
            "LEFT JOIN t_shelf s ON i.shelf_id = s.shelf_id " +
            "LEFT JOIN t_work_order w ON i.work_order_id = w.work_id " +
            "LEFT JOIN t_sales_order o ON i.order_id = o.order_id " +
            "LEFT JOIN sys_user u ON i.stock_user_id = u.id " +
            "LEFT JOIN (SELECT order_id, " +
            "  GROUP_CONCAT(DISTINCT product_name SEPARATOR '、') AS productNames, " +
            "  GROUP_CONCAT(DISTINCT item_category SEPARATOR '、') AS productTypes, " +
            "  SUM(num) AS productNum " +
            "  FROM t_sales_order_item GROUP BY order_id) it ON it.order_id = i.order_id " +
            "WHERE (#{workOrderId} IS NULL OR i.work_order_id = #{workOrderId}) " +
            "AND (#{status} IS NULL OR i.instock_status = #{status}) " +
            "ORDER BY i.id DESC")
    List<Map<String, Object>> pageInstock(@Param("workOrderId") Long workOrderId, @Param("status") Integer status);
}
