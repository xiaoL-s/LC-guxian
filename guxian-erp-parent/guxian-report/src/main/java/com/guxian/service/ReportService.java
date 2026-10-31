package com.guxian.service;

import java.util.Map;

/**
 * 报表统计服务
 */
public interface ReportService {

    /** 销售看板 */
    Map<String, Object> salesOverview();

    /** 生产看板 */
    Map<String, Object> productionOverview();

    /** 库存看板 */
    Map<String, Object> stockOverview();

    /** 利润看板 */
    Map<String, Object> profitOverview();
}
