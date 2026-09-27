package com.guxian.produce.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.produce.entity.TWorkReport;

import java.util.List;
import java.util.Map;

public interface TWorkReportService extends IService<TWorkReport> {

    IPage<Map<String, Object>> pageReport(Page<Map<String, Object>> page, Long workOrderId, Long workerId, Long processId, String keyword);

    /** 计件工资汇总 */
    Map<String, Object> wageSummary(Long workerId, String dateFrom, String dateTo);

    /** 按工人汇总计件工资（计件工资核算页） */
    List<Map<String, Object>> wageByWorker(String dateFrom, String dateTo);
}
