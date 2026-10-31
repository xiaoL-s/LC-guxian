package com.guxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.context.UserContext;
import com.guxian.entity.TCollectionAccount;
import com.guxian.entity.TCollectionRecord;
import com.guxian.mapper.FinanceQueryMapper;
import com.guxian.mapper.SysUserReadMapper;
import com.guxian.mapper.TCollectionAccountMapper;
import com.guxian.mapper.TCollectionRecordMapper;
import com.guxian.service.CollectionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollectionServiceImpl implements CollectionService {

    @Resource
    private TCollectionAccountMapper accountMapper;
    @Resource
    private TCollectionRecordMapper recordMapper;
    @Resource
    private FinanceQueryMapper financeQueryMapper;
    @Resource
    private SysUserReadMapper sysUserReadMapper;

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** 财务状态推导：0未收账 -> 1部分已收账 -> 2已收账（与 sales 模块口径一致） */
    private static Integer deriveStatus(BigDecimal total, BigDecimal received) {
        if (received.signum() <= 0) {
            return 0;
        }
        return received.compareTo(total) >= 0 ? 2 : 1;
    }

    @Override
    public List<Map<String, Object>> accountList() {
        List<TCollectionAccount> list = accountMapper.selectList(
                new LambdaQueryWrapper<TCollectionAccount>()
                        .eq(TCollectionAccount::getDelFlag, 0)
                        .orderByDesc(TCollectionAccount::getIsSystem)
                        .orderByAsc(TCollectionAccount::getAccountId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (TCollectionAccount a : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("accountId", a.getAccountId());
            m.put("accountName", a.getAccountName());
            m.put("holderName", a.getHolderName());
            m.put("bankCard", a.getBankCard());
            m.put("balance", nz(a.getBalance()).setScale(2, RoundingMode.HALF_UP));
            m.put("isSystem", a.getIsSystem());
            m.put("remark", a.getRemark());
            result.add(m);
        }
        return result;
    }

    @Override
    @Transactional
    public TCollectionAccount saveAccount(TCollectionAccount account) {
        if (account.getAccountId() == null) {
            account.setBalance(nz(account.getBalance()));
            account.setDelFlag(0);
            accountMapper.insert(account);
        } else {
            // 系统账号名称与勿删提示不可改，其余字段可编辑
            TCollectionAccount old = accountMapper.selectById(account.getAccountId());
            if (old != null && old.getIsSystem() != null && old.getIsSystem() == 1) {
                account.setAccountName(old.getAccountName());
                account.setRemark(old.getRemark());
            }
            account.setBalance(nz(account.getBalance()));
            accountMapper.updateById(account);
        }
        return account;
    }

    @Override
    @Transactional
    public boolean deleteAccount(Long accountId) {
        TCollectionAccount account = accountMapper.selectById(accountId);
        if (account == null) {
            return false;
        }
        if (account.getIsSystem() != null && account.getIsSystem() == 1) {
            throw new RuntimeException("此账号为系统账号，切勿删除");
        }
        accountMapper.deleteById(accountId);
        return true;
    }

    @Override
    public IPage<Map<String, Object>> recordPage(long pageNum, long pageSize, String keyword,
                                                 String dateFrom, String dateTo) {
        return financeQueryMapper.selectRecordPage(new Page<>(pageNum, pageSize),
                empty(keyword), empty(dateFrom), empty(dateTo));
    }

    @Override
    @Transactional
    public Long saveRecord(Long accountId, Long salesOrderId, BigDecimal amount, String remark) {
        if (amount == null || amount.signum() <= 0) {
            throw new RuntimeException("收款金额必须大于0");
        }
        TCollectionAccount account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new RuntimeException("收款账户不存在");
        }
        // 订单财务联动（与 sales 模块口径一致：实收累计 + 未付 = 总额 - 实收）
        Map<String, Object> order = null;
        if (salesOrderId != null) {
            order = financeQueryMapper.selectOrderForFinance(salesOrderId);
            if (order == null) {
                throw new RuntimeException("订单不存在或已删除");
            }
            BigDecimal total = nz((BigDecimal) order.get("totalAmount"));
            BigDecimal received = nz((BigDecimal) order.get("receiveAmount")).add(amount);
            BigDecimal unpaid = total.subtract(received).max(BigDecimal.ZERO);
            financeQueryMapper.updateOrderFinance(salesOrderId, received, unpaid,
                    deriveStatus(total, received), LocalDateTime.now());
        }
        financeQueryMapper.addAccountBalance(accountId, amount);

        TCollectionRecord record = new TCollectionRecord();
        record.setAccountId(accountId);
        record.setAccountName(account.getAccountName());
        record.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        record.setSalesOrderId(salesOrderId);
        record.setOrderNo(order == null ? "" : String.valueOf(order.get("orderNo")));
        record.setCustomerId(order == null ? null : ((Number) order.get("customerId")).longValue());
        record.setCustomerName(order == null ? "" : String.valueOf(order.get("customerName")));
        Long uid = UserContext.getUserId();
        record.setOperatorId(uid);
        String operatorName = uid == null ? "" : nvl(sysUserReadMapper.queryRealName(uid));
        record.setOperatorName(operatorName);
        record.setRemark(remark == null ? "" : remark);
        record.setReceiveTime(LocalDateTime.now());
        record.setDelFlag(0);
        recordMapper.insert(record);
        return record.getRecordId();
    }

    @Override
    @Transactional
    public boolean deleteRecord(Long recordId) {
        TCollectionRecord record = recordMapper.selectById(recordId);
        if (record == null || (record.getDelFlag() != null && record.getDelFlag() == 1)) {
            return false;
        }
        // 撤销订单收款
        if (record.getSalesOrderId() != null) {
            Map<String, Object> order = financeQueryMapper.selectOrderForFinance(record.getSalesOrderId());
            if (order != null) {
                BigDecimal total = nz((BigDecimal) order.get("totalAmount"));
                BigDecimal received = nz((BigDecimal) order.get("receiveAmount")).subtract(nz(record.getAmount()));
                if (received.signum() < 0) {
                    throw new RuntimeException("撤销金额超过该订单已收金额，无法撤销");
                }
                financeQueryMapper.updateOrderFinance(record.getSalesOrderId(), received,
                        total.subtract(received).max(BigDecimal.ZERO),
                        deriveStatus(total, received), LocalDateTime.now());
            }
        }
        financeQueryMapper.subAccountBalance(record.getAccountId(), nz(record.getAmount()));
        // 逻辑删除（全局 logic-delete-field: delFlag，必须用 deleteById）
        recordMapper.deleteById(record.getRecordId());
        return true;
    }

    @Override
    @Transactional
    public int mergeCollect(Long accountId, List<Map<String, Object>> orders, String remark) {
        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("请先选择要收款的订单");
        }
        int count = 0;
        for (Map<String, Object> item : orders) {
            Long orderId = ((Number) item.get("orderId")).longValue();
            BigDecimal amount = new BigDecimal(item.get("amount").toString());
            saveRecord(accountId, orderId, amount, remark);
            count++;
        }
        return count;
    }

    @Override
    public IPage<Map<String, Object>> receivablePage(long pageNum, long pageSize, String keyword,
                                                     Integer orderStatus, Integer financeStatus,
                                                     String dateFrom, String dateTo) {
        return financeQueryMapper.selectReceivablePage(new Page<>(pageNum, pageSize),
                empty(keyword), orderStatus, financeStatus, empty(dateFrom), empty(dateTo));
    }

    @Override
    public Map<String, Object> receivableSummary(String keyword, Integer orderStatus,
                                                 Integer financeStatus, String dateFrom, String dateTo) {
        Map<String, Object> s = financeQueryMapper.selectReceivableSummary(
                empty(keyword), orderStatus, financeStatus, empty(dateFrom), empty(dateTo));
        return fmtSummary(s);
    }

    @Override
    public IPage<Map<String, Object>> customerPage(long pageNum, long pageSize, String keyword) {
        return financeQueryMapper.selectCustomerPage(new Page<>(pageNum, pageSize), empty(keyword));
    }

    @Override
    public Map<String, Object> customerSummary(String keyword) {
        return fmtSummary(financeQueryMapper.selectCustomerSummary(empty(keyword)));
    }

    @Override
    @Transactional
    public void rechargeCustomer(Long customerId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        if (financeQueryMapper.rechargeCustomer(customerId, amount.setScale(2, RoundingMode.HALF_UP)) == 0) {
            throw new RuntimeException("客户不存在或已删除");
        }
    }

    private static Map<String, Object> fmtSummary(Map<String, Object> s) {
        if (s == null) {
            return new HashMap<>();
        }
        for (String k : List.of("totalSales", "totalPaid", "totalUnpaid", "totalDiscount")) {
            s.put(k, nz((BigDecimal) s.get(k)).setScale(2, RoundingMode.HALF_UP));
        }
        return s;
    }

    private static String empty(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
