package com.guxian.sales.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 只读访问生产模块工单表（同库共享）。
 * 用于销售订单“编辑/删除”前置校验：已生成生产工单的订单禁止修改与删除，
 * 防止销售单与生产工单数据不一致。
 */
@Mapper
public interface WorkOrderCheckMapper {

    /** 订单已生成的工单数量（未逻辑删除） */
    @Select("SELECT COUNT(*) FROM t_work_order WHERE sales_order_id = #{orderId} AND del_flag = 0")
    int countBySalesOrderId(@Param("orderId") Long orderId);

    /** 订单最新工单号（拆单成功提示用） */
    @Select("SELECT work_no FROM t_work_order WHERE sales_order_id = #{orderId} AND del_flag = 0 ORDER BY work_id DESC LIMIT 1")
    String latestWorkNo(@Param("orderId") Long orderId);
}
