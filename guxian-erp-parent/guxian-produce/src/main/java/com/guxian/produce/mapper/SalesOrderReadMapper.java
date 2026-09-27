package com.guxian.produce.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 只读访问销售模块订单数据（同库共享）
 * 拆单算料需要读订单头、订单明细、产品BOM、物料信息
 */
@Mapper
public interface SalesOrderReadMapper {

    /** 订单头：状态 + 客户 + 联系信息 + 总面积 + 生产单打印所需日期/类型 */
    @Select("SELECT order_id AS orderId, order_no AS orderNo, order_status AS orderStatus, " +
            "customer_id AS customerId, customer_name AS customerName, " +
            "phone AS customerPhone, terminal_address AS customerAddress, " +
            "total_area AS totalArea, order_type AS orderType, " +
            "order_date AS orderDate, expect_date AS expectDate " +
            "FROM t_sales_order WHERE order_id = #{orderId} AND del_flag = 0")
    Map<String, Object> selectOrderHeader(@Param("orderId") Long orderId);

    /** 待拆单的已审核订单（1=已审核待排产；2=生产中但无工单的历史卡单） */
    @Select("SELECT order_id AS orderId, order_no AS orderNo, customer_name AS customerName, " +
            "total_area AS totalArea, total_amount AS totalAmount, order_date AS orderDate, create_time AS createTime " +
            "FROM t_sales_order WHERE order_status IN (1,2) AND del_flag = 0 " +
            "AND NOT EXISTS (SELECT 1 FROM t_work_order w WHERE w.sales_order_id = t_sales_order.order_id AND w.del_flag = 0) " +
            "ORDER BY order_id DESC")
    List<Map<String, Object>> selectAuditedOrders();

    /** 待拆单的已审核订单（关键字：订单号/客户名） */
    @Select("SELECT order_id AS orderId, order_no AS orderNo, customer_name AS customerName, " +
            "total_area AS totalArea, total_amount AS totalAmount, order_date AS orderDate, create_time AS createTime " +
            "FROM t_sales_order WHERE order_status IN (1,2) AND del_flag = 0 " +
            "AND (order_no LIKE #{kw} OR customer_name LIKE #{kw}) " +
            "AND NOT EXISTS (SELECT 1 FROM t_work_order w WHERE w.sales_order_id = t_sales_order.order_id AND w.del_flag = 0) " +
            "ORDER BY order_id DESC")
    List<Map<String, Object>> selectAuditedOrdersWithKeyword(@Param("kw") String kw);

    /** 订单明细：产品 + 尺寸 + 颜色/纱网/把手等下料属性（打印生产下料单用） */
    @Select("SELECT item_id AS itemId, product_id AS productId, product_name AS productName, " +
            "dict_type AS dictType, item_total_area AS itemTotalArea, num AS num, width, height, " +
            "color AS color, net_material AS netMaterial, handle AS handle, lock_set AS lockSet, " +
            "handle_direction AS handleDirection, add_rod AS addRod, fixed_bottom AS fixedBottom, " +
            "deduct_width AS deductWidth, net_width AS netWidth, remark AS remark, unit AS unit, " +
            "sub_order_no AS subOrderNo " +
            "FROM t_sales_order_item WHERE order_id = #{orderId}")
    List<Map<String, Object>> selectOrderItems(@Param("orderId") Long orderId);

    /** 按字典产品类型取 BOM（dict_type 维度） */
    @Select("SELECT material_id AS materialId, use_num AS useNum, loss_rate AS lossRate " +
            "FROM t_product_bom WHERE dict_type = #{dictType} AND del_flag = 0 ORDER BY sort")
    List<Map<String, Object>> selectBomByDictType(@Param("dictType") String dictType);

    /** 按产品ID取 BOM（兼容旧数据） */
    @Select("SELECT material_id AS materialId, use_num AS useNum, loss_rate AS lossRate " +
            "FROM t_product_bom WHERE product_id = #{productId} AND del_flag = 0 ORDER BY sort")
    List<Map<String, Object>> selectBomByProductId(@Param("productId") Long productId);

    /** 物料信息 */
    @Select("SELECT id, material_code AS materialCode, material_name AS materialName, unit, stock_num AS stockNum " +
            "FROM t_material_stock WHERE id = #{materialId}")
    Map<String, Object> selectMaterial(@Param("materialId") Long materialId);

    /** 扣减库存 */
    @Update("UPDATE t_material_stock SET stock_num = stock_num - #{num}, update_time = NOW() WHERE id = #{materialId} AND stock_num >= #{num}")
    int deductStock(@Param("materialId") Long materialId, @Param("num") BigDecimal num);

    /** 写入库存流水（生产领料） */
    @Insert("INSERT INTO t_stock_record (material_id, material_code, material_name, biz_type, in_num, out_num, after_num, relate_type, relate_no, remark, create_by, create_time) " +
            "SELECT m.id, m.material_code, m.material_name, 3, 0, #{num}, m.stock_num, 'WORK_ORDER', #{relateNo}, #{remark}, #{createBy}, NOW() " +
            "FROM t_material_stock m WHERE m.id = #{materialId}")
    int insertPickRecord(@Param("materialId") Long materialId, @Param("num") BigDecimal num,
                         @Param("relateNo") String relateNo, @Param("remark") String remark,
                         @Param("createBy") Long createBy);

    /** 更新销售订单状态（回写订单流转） */
    @Update("UPDATE t_sales_order SET order_status = #{status}, update_time = NOW() WHERE order_id = #{orderId}")
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") int status);

    /** 更新明细行状态与工单ID（t_sales_order_item 无 update_time 列，只更新业务字段） */
    @Update("UPDATE t_sales_order_item SET item_status = #{itemStatus}, work_order_id = #{workId} " +
            "WHERE order_id = #{orderId}")
    int updateItemByOrder(@Param("orderId") Long orderId, @Param("itemStatus") int itemStatus, @Param("workId") Long workId);

    /** 查询订单下的明细数量合计（完工入库数量用） */
    @Select("SELECT IFNULL(SUM(num),0) AS totalNum FROM t_sales_order_item WHERE order_id = #{orderId}")
    Map<String, Object> selectOrderItemNum(@Param("orderId") Long orderId);

    /** 用户真实姓名 */
    @Select("SELECT real_name AS realName FROM sys_user WHERE id = #{userId}")
    Map<String, Object> selectUserName(@Param("userId") Long userId);
}
