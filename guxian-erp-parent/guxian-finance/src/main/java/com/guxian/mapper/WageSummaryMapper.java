package com.guxian.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 计件工资汇总查询（实时统计 t_work_report）
 */
@Mapper
public interface WageSummaryMapper {

    /** 汇总：工资总额 / 合格 / 不良 / 次数（可按工人、日期范围、员工名） */
    @Select("<script>" +
            "SELECT IFNULL(SUM(r.piece_wage),0) AS totalWage, " +
            "IFNULL(SUM(r.qualified_num),0) AS totalQualified, " +
            "IFNULL(SUM(r.bad_num),0) AS totalBad, COUNT(*) AS count " +
            "FROM t_work_report r " +
            "LEFT JOIN sys_user u ON u.id = r.worker_id " +
            "<where>" +
            "  <if test='workerId != null'> AND r.worker_id = #{workerId} </if>" +
            "  <if test='dateFrom != null and dateFrom != \"\"'> AND r.report_time &gt;= #{dateFrom} </if>" +
            "  <if test='dateTo != null and dateTo != \"\"'> AND r.report_time &lt; DATE_ADD(#{dateTo}, INTERVAL 1 DAY) </if>" +
            "  <if test='workerName != null and workerName != \"\"'> AND u.real_name LIKE CONCAT('%', #{workerName}, '%') </if>" +
            "</where>" +
            "</script>")
    Map<String, Object> summary(@Param("workerId") Long workerId,
                                @Param("dateFrom") String dateFrom,
                                @Param("dateTo") String dateTo,
                                @Param("workerName") String workerName);

    /** 按工人汇总（计件工资核算），支持员工名筛选 */
    @Select("<script>" +
            "SELECT r.worker_id AS workerId, IFNULL(u.real_name, '') AS realName, COUNT(*) AS count, " +
            "IFNULL(SUM(r.qualified_num),0) AS totalQualified, " +
            "IFNULL(SUM(r.bad_num),0) AS totalBad, " +
            "IFNULL(SUM(r.piece_wage),0) AS totalWage " +
            "FROM t_work_report r " +
            "LEFT JOIN sys_user u ON u.id = r.worker_id " +
            "<where>" +
            "  <if test='dateFrom != null and dateFrom != \"\"'> AND r.report_time &gt;= #{dateFrom} </if>" +
            "  <if test='dateTo != null and dateTo != \"\"'> AND r.report_time &lt; DATE_ADD(#{dateTo}, INTERVAL 1 DAY) </if>" +
            "  <if test='workerName != null and workerName != \"\"'> AND u.real_name LIKE CONCAT('%', #{workerName}, '%') </if>" +
            "</where>" +
            " GROUP BY r.worker_id ORDER BY totalWage DESC" +
            "</script>")
    List<Map<String, Object>> byWorker(@Param("dateFrom") String dateFrom,
                                       @Param("dateTo") String dateTo,
                                       @Param("workerName") String workerName);

    /** 计件工资明细（分页）：按 员工×工单×工序 聚合；关键字匹配 订单号/工序名/客户/地址 */
    @Select("<script>" +
            "SELECT r.worker_id AS workerId, IFNULL(u.real_name, CONCAT('工人', r.worker_id)) AS employeeName, " +
            "COUNT(*) AS processCount, " +
            "IFNULL(SUM(r.qualified_num),0) AS totalQualified, " +
            "IFNULL(SUM(r.bad_num),0) AS totalBad, " +
            "'按板件数' AS calcType, " +
            "ROUND(IFNULL(SUM(r.piece_wage),0) / NULLIF(SUM(r.qualified_num),0), 2) AS unitPrice, " +
            "IFNULL(SUM(r.piece_wage),0) AS totalWage, " +
            "IFNULL(MAX(p.process_name), '') AS processName, " +
            "MAX(r.report_time) AS scanTime, " +
            "IFNULL(MAX(o.order_no), '') AS orderNo, " +
            "IFNULL(MAX(o.customer_name), '') AS customerName, " +
            "IFNULL(MAX(o.address), '') AS address, " +
            "MAX(o.expect_date) AS expectDate, " +
            "IFNULL(GREATEST(DATEDIFF(CURDATE(), MAX(o.expect_date)), 0), 0) AS overdueDays, " +
            "IFNULL(MAX(r.product_name), '') AS productName " +
            "FROM t_work_report r " +
            "JOIN t_work_order w ON w.work_id = r.work_order_id " +
            "JOIN t_sales_order o ON o.order_id = w.sales_order_id " +
            "LEFT JOIN t_process_dict p ON p.process_id = r.process_id " +
            "LEFT JOIN sys_user u ON u.id = r.worker_id " +
            "<where>" +
            "  <if test='dateFrom != null and dateFrom != \"\"'> AND r.report_time &gt;= #{dateFrom} </if>" +
            "  <if test='dateTo != null and dateTo != \"\"'> AND r.report_time &lt; DATE_ADD(#{dateTo}, INTERVAL 1 DAY) </if>" +
            "  <if test='workerName != null and workerName != \"\"'> AND u.real_name LIKE CONCAT('%', #{workerName}, '%') </if>" +
            "  <if test='keyword != null and keyword != \"\"'> AND (o.order_no LIKE CONCAT('%', #{keyword}, '%') " +
            "        OR p.process_name LIKE CONCAT('%', #{keyword}, '%') " +
            "        OR o.customer_name LIKE CONCAT('%', #{keyword}, '%') " +
            "        OR o.address LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</where>" +
            "GROUP BY r.worker_id, r.work_order_id, r.process_id " +
            "ORDER BY scanTime DESC" +
            "</script>")
    IPage<Map<String, Object>> wageDetail(Page<Map<String, Object>> page,
                                          @Param("dateFrom") String dateFrom,
                                          @Param("dateTo") String dateTo,
                                          @Param("workerName") String workerName,
                                          @Param("keyword") String keyword);

    /** 某工人某月计件工资合计（生成月度工资时自动带出） */
    @Select("SELECT IFNULL(SUM(piece_wage),0) FROM t_work_report " +
            "WHERE worker_id = #{workerId} AND report_time &gt;= #{monthStart} " +
            "AND report_time &lt; DATE_ADD(#{monthEnd}, INTERVAL 1 DAY)")
    java.math.BigDecimal monthTotal(@Param("workerId") Long workerId,
                                    @Param("monthStart") String monthStart,
                                    @Param("monthEnd") String monthEnd);
}
