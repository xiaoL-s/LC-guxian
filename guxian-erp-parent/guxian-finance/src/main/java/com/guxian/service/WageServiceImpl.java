package com.guxian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.entity.TWorkerWage;
import com.guxian.mapper.SysUserReadMapper;
import com.guxian.mapper.TWorkerWageMapper;
import com.guxian.mapper.WageSummaryMapper;
import com.guxian.service.WageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WageServiceImpl implements WageService {

    /** 全局统一时间格式（与系统其他模块一致） */
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private WageSummaryMapper wageSummaryMapper;

    @Resource
    private TWorkerWageMapper workerWageMapper;

    @Resource
    private SysUserReadMapper sysUserReadMapper;

    @Override
    public Map<String, Object> summary(Long workerId, String dateFrom, String dateTo, String workerName) {
        return wageSummaryMapper.summary(workerId, dateFrom, dateTo, workerName);
    }

    @Override
    public List<Map<String, Object>> byWorker(String dateFrom, String dateTo, String workerName) {
        List<Map<String, Object>> rows = wageSummaryMapper.byWorker(dateFrom, dateTo, workerName);
        for (Map<String, Object> row : rows) {
            Object userId = row.get("workerId");
            String name = row.get("realName") == null ? "" : row.get("realName").toString();
            if (name.isEmpty() && userId != null) {
                row.put("realName", queryRealName(((Number) userId).longValue()));
            }
        }
        return rows;
    }

    @Override
    public IPage<Map<String, Object>> wageDetail(Page<Map<String, Object>> page, String dateFrom, String dateTo,
                                                 String workerName, String keyword) {
        return wageSummaryMapper.wageDetail(page, dateFrom, dateTo, workerName, keyword);
    }

    /** 从系统用户表取姓名 */
    private String queryRealName(Long userId) {
        try {
            String name = sysUserReadMapper.queryRealName(userId);
            return name == null ? "" : name;
        } catch (Exception ignored) {
        }
        return "";
    }

    @Override
    public List<Map<String, Object>> monthList(String wageMonth, Long workerId) {
        LambdaQueryWrapper<TWorkerWage> w = new LambdaQueryWrapper<>();
        w.eq(workerId != null, TWorkerWage::getWorkerId, workerId);
        if (wageMonth != null && !wageMonth.isEmpty()) {
            w.likeRight(TWorkerWage::getWageMonth, wageMonth);
        }
        w.orderByDesc(TWorkerWage::getWageMonth).orderByAsc(TWorkerWage::getWorkerId);
        List<TWorkerWage> list = workerWageMapper.selectList(w);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (TWorkerWage ww : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", ww.getId());
            row.put("workerId", ww.getWorkerId());
            row.put("realName", queryRealName(ww.getWorkerId()));
            row.put("wageMonth", ww.getWageMonth());
            row.put("totalWage", ww.getTotalWage());
            row.put("deductWage", ww.getDeductWage());
            row.put("addWage", ww.getAddWage());
            row.put("realWage", ww.getRealWage());
            row.put("status", ww.getStatus());
            row.put("remark", ww.getRemark());
            row.put("updateTime", ww.getUpdateTime() == null ? "" : TIME_FMT.format(ww.getUpdateTime()));
            rows.add(row);
        }
        return rows;
    }

    @Override
    public Map<String, Object> saveMonth(Long workerId, String wageMonth,
                                         BigDecimal deductWage, BigDecimal addWage, String remark) {
        // 该月计件工资合计（实时从报工表统计）
        String monthStart = wageMonth + "-01";
        LocalDate end = LocalDate.parse(monthStart).plusMonths(1).minusDays(1);
        String monthEnd = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        BigDecimal totalWage = wageSummaryMapper.monthTotal(workerId, monthStart + " 00:00:00", monthEnd + " 23:59:59");

        // 幂等：同一工人同一月份存在则更新，否则新建
        LambdaQueryWrapper<TWorkerWage> w = new LambdaQueryWrapper<>();
        w.eq(TWorkerWage::getWorkerId, workerId).eq(TWorkerWage::getWageMonth, wageMonth);
        TWorkerWage exist = workerWageMapper.selectOne(w);
        boolean isNew = exist == null;
        TWorkerWage ww = isNew ? new TWorkerWage() : exist;
        ww.setWorkerId(workerId);
        ww.setWageMonth(wageMonth);
        ww.setTotalWage(totalWage);
        ww.setDeductWage(deductWage == null ? BigDecimal.ZERO : deductWage);
        ww.setAddWage(addWage == null ? BigDecimal.ZERO : addWage);
        ww.setRealWage(totalWage.subtract(ww.getDeductWage()).add(ww.getAddWage()));
        ww.setStatus(1);
        ww.setRemark(remark);
        if (isNew) {
            workerWageMapper.insert(ww);
        } else {
            workerWageMapper.updateById(ww);
        }
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", ww.getId());
        vo.put("totalWage", totalWage);
        vo.put("realWage", ww.getRealWage());
        vo.put("isNew", isNew);
        return vo;
    }

    @Override
    public boolean deleteMonth(Long id) {
        return workerWageMapper.deleteById(id) > 0;
    }
}
