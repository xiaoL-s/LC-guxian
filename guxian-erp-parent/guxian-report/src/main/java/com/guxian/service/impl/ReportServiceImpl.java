package com.guxian.service.impl;

import com.guxian.mapper.ReportMapper;
import com.guxian.service.ReportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表统计实现：各看板数据组装，趋势缺日期补零
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private ReportMapper reportMapper;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public Map<String, Object> salesOverview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", reportMapper.salesStats());
        result.put("trend", fillDaily(reportMapper.salesTrend(), "date", "cnt", 29, 0));
        result.put("amountTrend", fillDaily(reportMapper.salesTrend(), "date", "amount", 29, 0));
        result.put("productRank", reportMapper.productRank());
        result.put("customerRank", reportMapper.customerRank());
        return result;
    }

    @Override
    public Map<String, Object> productionOverview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", reportMapper.productionStats());
        result.put("trend", fillDaily(reportMapper.productionTrend(), "date", "cnt", 29, 0));
        result.put("processRank", reportMapper.processRank());
        result.put("workerRank", reportMapper.workerRank());
        return result;
    }

    @Override
    public Map<String, Object> stockOverview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", reportMapper.stockStats());
        result.put("trend", fillDaily(reportMapper.stockTrend(), "date", "value", 29, 0));
        result.put("shelfRank", reportMapper.shelfRank());
        return result;
    }

    @Override
    public Map<String, Object> profitOverview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", reportMapper.profitStats());
        result.put("trend", fillMonthly(reportMapper.profitTrend(), "ym", "profit", 11));
        result.put("salesTrend", fillMonthly(reportMapper.profitTrend(), "ym", "sales", 11));
        result.put("productProfitRank", reportMapper.productProfitRank());
        return result;
    }

    /** 近 N 天按日补零（days=29 表示最近30天） */
    private List<Map<String, Object>> fillDaily(List<Map<String, Object>> rows,
                                                String keyCol, String valCol, int backDays, double defaultVal) {
        Map<String, Map<String, Object>> index = new HashMap<>();
        for (Map<String, Object> r : rows) {
            Object k = r.get(keyCol);
            if (k != null) {
                index.put(String.valueOf(k).substring(0, 10), r);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = backDays; i >= 0; i--) {
            String day = today.minusDays(i).format(DAY);
            Map<String, Object> row = index.get(day);
            if (row == null) {
                row = new LinkedHashMap<>();
                row.put(keyCol, day);
                row.put(valCol, defaultVal);
            }
            result.add(row);
        }
        return result;
    }

    /** 最近12个月按月补零 */
    private List<Map<String, Object>> fillMonthly(List<Map<String, Object>> rows,
                                                  String keyCol, String valCol, int backMonths) {
        Map<String, Map<String, Object>> index = new HashMap<>();
        for (Map<String, Object> r : rows) {
            Object k = r.get(keyCol);
            if (k != null) {
                index.put(String.valueOf(k).substring(0, 7), r);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = backMonths; i >= 0; i--) {
            String ym = today.minusMonths(i).format(MONTH);
            Map<String, Object> row = index.get(ym);
            if (row == null) {
                row = new LinkedHashMap<>();
                row.put(keyCol, ym);
                row.put(valCol, 0);
            }
            result.add(row);
        }
        return result;
    }
}
