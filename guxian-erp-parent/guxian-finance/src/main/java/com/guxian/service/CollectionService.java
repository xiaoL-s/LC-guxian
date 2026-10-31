package com.guxian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.guxian.entity.TCollectionAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 财务管理：收款账户 / 收款明细 / 成品应收 / 客户往来 / 合并收款
 */
public interface CollectionService {

    /** 收款账户列表 */
    List<Map<String, Object>> accountList();

    /** 新增/编辑收款账户 */
    TCollectionAccount saveAccount(TCollectionAccount account);

    /** 删除收款账户（系统账号禁止删除） */
    boolean deleteAccount(Long accountId);

    /** 收款明细分页 */
    IPage<Map<String, Object>> recordPage(long pageNum, long pageSize, String keyword,
                                          String dateFrom, String dateTo);

    /** 单笔收款（联动更新订单财务状态与账户余额） */
    Long saveRecord(Long accountId, Long salesOrderId, BigDecimal amount, String remark);

    /** 撤销收款（联动回退订单财务状态与账户余额） */
    boolean deleteRecord(Long recordId);

    /** 合并收款：多订单批量收款 */
    int mergeCollect(Long accountId, List<Map<String, Object>> orders, String remark);

    /** 成品应收：订单财务列表分页 */
    IPage<Map<String, Object>> receivablePage(long pageNum, long pageSize, String keyword,
                                              Integer orderStatus, Integer financeStatus,
                                              String dateFrom, String dateTo);

    /** 成品应收：统计合计 */
    Map<String, Object> receivableSummary(String keyword, Integer orderStatus,
                                          Integer financeStatus, String dateFrom, String dateTo);

    /** 客户往来分页（按客户聚合销售/收付款） */
    IPage<Map<String, Object>> customerPage(long pageNum, long pageSize, String keyword);

    /** 客户往来统计合计 */
    Map<String, Object> customerSummary(String keyword);

    /** 客户充值（余额增加） */
    void rechargeCustomer(Long customerId, BigDecimal amount);
}
