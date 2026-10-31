package com.guxian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WageService {

    /** 计件工资汇总 */
    Map<String, Object> summary(Long workerId, String dateFrom, String dateTo, String workerName);

    /** 按工人汇总 */
    List<Map<String, Object>> byWorker(String dateFrom, String dateTo, String workerName);

    /** 计件工资明细（分页，按 员工×工单×工序 聚合） */
    IPage<Map<String, Object>> wageDetail(Page<Map<String, Object>> page, String dateFrom, String dateTo,
                                          String workerName, String keyword);

    /** 月度工资确认列表（t_worker_wage） */
    List<Map<String, Object>> monthList(String wageMonth, Long workerId);

    /** 生成/保存月度工资（totalWage 自动取该月报工合计，可调整扣款/加款） */
    Map<String, Object> saveMonth(Long workerId, String wageMonth,
                                  BigDecimal deductWage, BigDecimal addWage, String remark);

    /** 删除月度工资记录 */
    boolean deleteMonth(Long id);
}
