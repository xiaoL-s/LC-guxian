package com.guxian.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 报表统计 Mapper：跨模块只读聚合查询（订单/工单/报工/收款/入库）
 * 全部基于 guxian-erp 单库实时统计
 */
@Mapper
public interface ReportMapper {

    // ==================== 销售报表 ====================

    /** 销售看板指标卡：今日新增订单/月销售金额/月销售面积/应收款/生产中/延期/加急/未结清 */
    @Select("SELECT " +
            " (SELECT COUNT(*) FROM t_sales_order WHERE del_flag=0 AND order_date=CURDATE()) AS todayOrders, " +
            " (SELECT IFNULL(SUM(total_amount),0) FROM t_sales_order WHERE del_flag=0 AND order_date>=DATE_FORMAT(CURDATE(),'%Y-%m-01') AND order_date<=CURDATE()) AS monthAmount, " +
            " (SELECT IFNULL(SUM(total_area),0) FROM t_sales_order WHERE del_flag=0 AND order_date>=DATE_FORMAT(CURDATE(),'%Y-%m-01') AND order_date<=CURDATE()) AS monthArea, " +
            " (SELECT IFNULL(SUM(unpaid_amount),0) FROM t_sales_order WHERE del_flag=0 AND order_status<>7) AS receivable, " +
            " (SELECT COUNT(*) FROM t_sales_order WHERE del_flag=0 AND order_status=2) AS productionOrders, " +
            " (SELECT COUNT(*) FROM t_sales_order WHERE del_flag=0 AND expect_date IS NOT NULL AND expect_date<CURDATE() AND order_status NOT IN (5,7)) AS delayedOrders, " +
            " (SELECT COUNT(*) FROM t_sales_order WHERE del_flag=0 AND order_type LIKE '%加急%') AS urgentOrders, " +
            " (SELECT COUNT(*) FROM t_sales_order WHERE del_flag=0 AND unpaid_amount>0 AND order_status<>7) AS unpaidOrders")
    Map<String, Object> salesStats();

    /** 下单趋势（近30天，按订单日期汇总数量/金额） */
    @Select("SELECT order_date AS date, COUNT(*) AS cnt, IFNULL(SUM(total_amount),0) AS amount " +
            "FROM t_sales_order WHERE del_flag=0 AND order_date>=DATE_SUB(CURDATE(), INTERVAL 29 DAY) " +
            "GROUP BY order_date ORDER BY order_date")
    List<Map<String, Object>> salesTrend();

    /** 产品销量排名（前10，按明细行汇总） */
    @Select("SELECT IFNULL(i.product_name,'其他') AS name, IFNULL(SUM(i.num),0) AS value, IFNULL(SUM(i.line_amount),0) AS amount " +
            "FROM t_sales_order_item i JOIN t_sales_order o ON o.order_id=i.order_id " +
            "WHERE o.del_flag=0 AND o.order_status<>7 AND i.product_name IS NOT NULL AND i.product_name<>'' " +
            "GROUP BY i.product_name ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> productRank();

    /** 经销商/客户销量排名（前10） */
    @Select("SELECT IFNULL(o.customer_name,'其他') AS name, COUNT(*) AS orders, IFNULL(SUM(o.total_amount),0) AS amount " +
            "FROM t_sales_order o WHERE o.del_flag=0 AND o.order_status<>7 AND o.customer_name IS NOT NULL AND o.customer_name<>'' " +
            "GROUP BY o.customer_name ORDER BY amount DESC LIMIT 10")
    List<Map<String, Object>> customerRank();

    // ==================== 生产报表 ====================

    /** 生产看板指标卡：工单总数/待生产/生产中/已完工/未打印生产单/延期工单 */
    @Select("SELECT " +
            " (SELECT COUNT(*) FROM t_work_order WHERE del_flag=0) AS totalOrders, " +
            " (SELECT COUNT(*) FROM t_work_order WHERE del_flag=0 AND work_status='WAIT_PROCESS') AS waitCount, " +
            " (SELECT COUNT(*) FROM t_work_order WHERE del_flag=0 AND work_status='PROCESSING') AS processingCount, " +
            " (SELECT COUNT(*) FROM t_work_order WHERE del_flag=0 AND work_status='FINISHED') AS finishedCount, " +
            " (SELECT COUNT(*) FROM t_sales_order WHERE del_flag=0 AND print_flag=0 AND order_status NOT IN (5,7)) AS noPrintOrders, " +
            " (SELECT COUNT(*) FROM t_work_order w JOIN t_sales_order o ON o.order_id=w.sales_order_id " +
            "   WHERE w.del_flag=0 AND o.expect_date IS NOT NULL AND o.expect_date<CURDATE() AND w.work_status IN ('WAIT_PROCESS','PROCESSING')) AS delayedWorks")
    Map<String, Object> productionStats();

    /** 生产趋势（近30天，按工单创建日期） */
    @Select("SELECT DATE(w.create_time) AS date, COUNT(*) AS cnt " +
            "FROM t_work_order w WHERE w.del_flag=0 AND w.create_time>=DATE_SUB(CURDATE(), INTERVAL 29 DAY) " +
            "GROUP BY DATE(w.create_time) ORDER BY date")
    List<Map<String, Object>> productionTrend();

    /** 工序产量排名（前10） */
    @Select("SELECT IFNULL(p.process_name,'未命名工序') AS name, IFNULL(SUM(r.qualified_num),0) AS value " +
            "FROM t_work_report r LEFT JOIN t_process_dict p ON p.process_id=r.process_id " +
            "GROUP BY p.process_id, p.process_name ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> processRank();

    /** 工人产量排名（前10，含计件工资） */
    @Select("SELECT IFNULL(u.real_name, CONCAT('工人', r.worker_id)) AS name, " +
            "IFNULL(SUM(r.qualified_num),0) AS value, IFNULL(SUM(r.piece_wage),0) AS wage " +
            "FROM t_work_report r LEFT JOIN sys_user u ON u.id=r.worker_id " +
            "GROUP BY r.worker_id ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> workerRank();

    // ==================== 库存报表 ====================

    /** 库存看板指标卡：累计入库/本月入库/今日入库/入库单数 */
    @Select("SELECT " +
            " (SELECT IFNULL(SUM(in_num),0) FROM t_product_instock) AS totalIn, " +
            " (SELECT IFNULL(SUM(in_num),0) FROM t_product_instock WHERE instock_time IS NOT NULL AND instock_time>=DATE_FORMAT(CURDATE(),'%Y-%m-01')) AS monthIn, " +
            " (SELECT IFNULL(SUM(in_num),0) FROM t_product_instock WHERE instock_time IS NOT NULL AND DATE(instock_time)=CURDATE()) AS todayIn, " +
            " (SELECT COUNT(*) FROM t_product_instock) AS inCount")
    Map<String, Object> stockStats();

    /** 入库趋势（近30天） */
    @Select("SELECT DATE(instock_time) AS date, IFNULL(SUM(in_num),0) AS value " +
            "FROM t_product_instock WHERE instock_time IS NOT NULL AND instock_time>=DATE_SUB(CURDATE(), INTERVAL 29 DAY) " +
            "GROUP BY DATE(instock_time) ORDER BY date")
    List<Map<String, Object>> stockTrend();

    /** 库位入库排名（前10） */
    @Select("SELECT IFNULL(s.shelf_name, CONCAT('库位', i.shelf_id)) AS name, IFNULL(SUM(i.in_num),0) AS value " +
            "FROM t_product_instock i LEFT JOIN t_shelf s ON s.shelf_id=i.shelf_id " +
            "GROUP BY i.shelf_id ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> shelfRank();

    // ==================== 利润报表 ====================

    /** 利润看板指标卡：本月销售额/本月利润/平均毛利率/应收款 */
    @Select("SELECT " +
            " (SELECT IFNULL(SUM(total_amount),0) FROM t_sales_order WHERE del_flag=0 AND order_status<>7 AND order_date>=DATE_FORMAT(CURDATE(),'%Y-%m-01') AND order_date<=CURDATE()) AS monthSales, " +
            " (SELECT IFNULL(SUM(profit_amount),0) FROM t_sales_order WHERE del_flag=0 AND order_status<>7 AND order_date>=DATE_FORMAT(CURDATE(),'%Y-%m-01') AND order_date<=CURDATE()) AS monthProfit, " +
            " (SELECT IFNULL(ROUND(AVG(gross_profit_rate)*100,2),0) FROM t_sales_order WHERE del_flag=0 AND order_status<>7 AND gross_profit_rate>0) AS avgProfitRate, " +
            " (SELECT IFNULL(SUM(unpaid_amount),0) FROM t_sales_order WHERE del_flag=0 AND order_status<>7) AS receivable")
    Map<String, Object> profitStats();

    /** 利润趋势（最近12个月，按月汇总销售额/利润） */
    @Select("SELECT DATE_FORMAT(order_date,'%Y-%m') AS ym, IFNULL(SUM(total_amount),0) AS sales, IFNULL(SUM(profit_amount),0) AS profit " +
            "FROM t_sales_order WHERE del_flag=0 AND order_status<>7 AND order_date IS NOT NULL " +
            "AND order_date>=DATE_SUB(DATE_FORMAT(CURDATE(),'%Y-%m-01'), INTERVAL 11 MONTH) " +
            "GROUP BY DATE_FORMAT(order_date,'%Y-%m') ORDER BY ym")
    List<Map<String, Object>> profitTrend();

    /** 产品利润排名（前10） */
    @Select("SELECT IFNULL(i.product_name,'其他') AS name, IFNULL(SUM(i.profit_amount),0) AS value " +
            "FROM t_sales_order_item i JOIN t_sales_order o ON o.order_id=i.order_id " +
            "WHERE o.del_flag=0 AND o.order_status<>7 AND i.product_name IS NOT NULL AND i.product_name<>'' " +
            "GROUP BY i.product_name ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> productProfitRank();
}
