package com.guxian.produce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.produce.entity.TWorkReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TWorkReportMapper extends BaseMapper<TWorkReport> {

    /** 按工人汇总计件：合格/不良/工资/次数（日期区间可选） */
    @Select("<script>" +
            "SELECT worker_id AS workerId, " +
            "IFNULL(SUM(qualified_num),0) AS totalQualified, " +
            "IFNULL(SUM(bad_num),0) AS totalBad, " +
            "IFNULL(SUM(piece_wage),0) AS totalWage, " +
            "COUNT(*) AS count " +
            "FROM t_work_report " +
            "<where>" +
            "<if test='dateFrom != null'>AND report_time &gt;= #{dateFrom}</if>" +
            "<if test='dateTo != null'>AND report_time &lt;= #{dateTo}</if>" +
            "</where>" +
            "GROUP BY worker_id ORDER BY totalWage DESC" +
            "</script>")
    List<Map<String, Object>> sumWageByWorker(@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo);
}
