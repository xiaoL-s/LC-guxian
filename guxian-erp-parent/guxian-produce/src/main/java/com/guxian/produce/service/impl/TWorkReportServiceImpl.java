package com.guxian.produce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.produce.entity.TProcessDict;
import com.guxian.produce.entity.TWorkOrder;
import com.guxian.produce.entity.TWorkReport;
import com.guxian.produce.mapper.TProcessDictMapper;
import com.guxian.produce.mapper.TWorkOrderMapper;
import com.guxian.produce.mapper.TWorkReportMapper;
import com.guxian.produce.service.TWorkReportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TWorkReportServiceImpl extends ServiceImpl<TWorkReportMapper, TWorkReport> implements TWorkReportService {

    @Resource
    private TProcessDictMapper processDictMapper;
    @Resource
    private TWorkOrderMapper workOrderMapper;
    @Resource
    private com.guxian.produce.mapper.SysUserReadMapper sysUserReadMapper;

    @Override
    public IPage<Map<String, Object>> pageReport(Page<Map<String, Object>> page, Long workOrderId, Long workerId, Long processId, String keyword) {
        LambdaQueryWrapper<TWorkReport> w = new LambdaQueryWrapper<>();
        w.eq(workOrderId != null, TWorkReport::getWorkOrderId, workOrderId);
        w.eq(workerId != null, TWorkReport::getWorkerId, workerId);
        w.eq(processId != null, TWorkReport::getProcessId, processId);
        // 按工单号模糊：先按关键字匹配工单ID集合，再过滤报工记录
        if (StringUtils.hasText(keyword)) {
            List<TWorkOrder> orders = workOrderMapper.selectList(new LambdaQueryWrapper<TWorkOrder>()
                    .like(TWorkOrder::getWorkNo, keyword.trim()));
            if (orders.isEmpty()) {
                Page<Map<String, Object>> empty = new Page<>(page.getCurrent(), page.getSize(), 0);
                empty.setRecords(new ArrayList<>());
                return empty;
            }
            w.in(TWorkReport::getWorkOrderId, orders.stream().map(TWorkOrder::getWorkId).toList());
        }
        w.orderByDesc(TWorkReport::getId);
        Page<TWorkReport> p = new Page<>(page.getCurrent(), page.getSize());
        IPage<TWorkReport> rp = baseMapper.selectPage(p, w);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (TWorkReport r : rp.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", r.getId());
            row.put("workOrderId", r.getWorkOrderId());
            row.put("processId", r.getProcessId());
            row.put("workerId", r.getWorkerId());
            row.put("qualifiedNum", r.getQualifiedNum());
            row.put("badNum", r.getBadNum());
            row.put("unitPrice", r.getUnitPrice());
            row.put("pieceWage", r.getPieceWage());
            row.put("reportTime", r.getReportTime() == null ? "" : r.getReportTime().toString());
            TProcessDict proc = processDictMapper.selectById(r.getProcessId());
            row.put("processName", proc == null ? "" : proc.getProcessName());
            TWorkOrder wo = workOrderMapper.selectById(r.getWorkOrderId());
            row.put("workNo", wo == null ? "" : wo.getWorkNo());
            rows.add(row);
        }
        Page<Map<String, Object>> voPage = new Page<>(rp.getCurrent(), rp.getSize(), rp.getTotal());
        voPage.setRecords(rows);
        return voPage;
    }

    @Override
    public Map<String, Object> wageSummary(Long workerId, String dateFrom, String dateTo) {
        LambdaQueryWrapper<TWorkReport> w = new LambdaQueryWrapper<>();
        w.eq(workerId != null, TWorkReport::getWorkerId, workerId);
        w.ge(StringUtils.hasText(dateFrom), TWorkReport::getReportTime, dateFrom + " 00:00:00");
        w.le(StringUtils.hasText(dateTo), TWorkReport::getReportTime, dateTo + " 23:59:59");
        List<TWorkReport> list = baseMapper.selectList(w);
        BigDecimal totalWage = BigDecimal.ZERO;
        int totalQualified = 0;
        int totalBad = 0;
        for (TWorkReport r : list) {
            totalWage = totalWage.add(r.getPieceWage() == null ? BigDecimal.ZERO : r.getPieceWage());
            totalQualified += r.getQualifiedNum() == null ? 0 : r.getQualifiedNum();
            totalBad += r.getBadNum() == null ? 0 : r.getBadNum();
        }
        Map<String, Object> vo = new HashMap<>();
        vo.put("totalWage", totalWage);
        vo.put("totalQualified", totalQualified);
        vo.put("totalBad", totalBad);
        vo.put("count", list.size());
        return vo;
    }

    @Override
    public List<Map<String, Object>> wageByWorker(String dateFrom, String dateTo) {
        List<Map<String, Object>> rows = baseMapper.sumWageByWorker(
                StringUtils.hasText(dateFrom) ? dateFrom + " 00:00:00" : null,
                StringUtils.hasText(dateTo) ? dateTo + " 23:59:59" : null);
        for (Map<String, Object> row : rows) {
            Object userId = row.get("workerId");
            row.put("realName", "");
            if (userId != null) {
                Map<String, Object> user = sysUserReadMapper.selectUser(((Number) userId).longValue());
                if (user != null && user.get("realName") != null) {
                    row.put("realName", user.get("realName").toString());
                }
            }
        }
        return rows;
    }
}
