package com.guxian.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.guxian.entity.TCollectionAccount;
import com.guxian.result.Result;
import com.guxian.service.CollectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 财务管理：收款账户 / 收款明细 / 成品应收 / 客户往来 / 合并收款（端口8087）
 */
@Tag(name = "财务管理-收付款")
@RestController
@RequestMapping("/finance")
public class CollectionController {

    @Resource
    private CollectionService collectionService;

    // ==================== 收款账户 ====================

    @GetMapping("/account/list")
    public Result<List<Map<String, Object>>> accountList() {
        return Result.success(collectionService.accountList());
    }

    @PostMapping("/account")
    public Result<TCollectionAccount> saveAccount(@RequestBody TCollectionAccount account) {
        return Result.success(collectionService.saveAccount(account));
    }

    @DeleteMapping("/account/{id}")
    public Result<Boolean> deleteAccount(@PathVariable Long id) {
        return Result.success(collectionService.deleteAccount(id));
    }

    // ==================== 收款明细 ====================

    @GetMapping("/record/page")
    public Result<IPage<Map<String, Object>>> recordPage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        return Result.success(collectionService.recordPage(pageNum, pageSize, keyword, dateFrom, dateTo));
    }

    /** 单笔收款（联动更新订单财务状态与账户余额） */
    @PostMapping("/record")
    public Result<Long> saveRecord(@RequestBody Map<String, Object> body) {
        Long accountId = ((Number) body.get("accountId")).longValue();
        Long salesOrderId = body.get("salesOrderId") == null ? null : ((Number) body.get("salesOrderId")).longValue();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String remark = (String) body.get("remark");
        return Result.success(collectionService.saveRecord(accountId, salesOrderId, amount, remark));
    }

    /** 撤销收款（联动回退订单财务状态与账户余额） */
    @DeleteMapping("/record/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        return Result.success(collectionService.deleteRecord(id));
    }

    /** 合并收款：多订单批量收款 */
    @PostMapping("/merge")
    public Result<Integer> mergeCollect(@RequestBody Map<String, Object> body) {
        Long accountId = ((Number) body.get("accountId")).longValue();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) body.get("orders");
        String remark = (String) body.get("remark");
        return Result.success(collectionService.mergeCollect(accountId, orders, remark));
    }

    // ==================== 成品应收 ====================

    @GetMapping("/receivable/page")
    public Result<IPage<Map<String, Object>>> receivablePage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Integer financeStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        return Result.success(collectionService.receivablePage(
                pageNum, pageSize, keyword, orderStatus, financeStatus, dateFrom, dateTo));
    }

    @GetMapping("/receivable/summary")
    public Result<Map<String, Object>> receivableSummary(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Integer financeStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        return Result.success(collectionService.receivableSummary(
                keyword, orderStatus, financeStatus, dateFrom, dateTo));
    }

    // ==================== 客户往来 ====================

    @GetMapping("/customer/page")
    public Result<IPage<Map<String, Object>>> customerPage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(collectionService.customerPage(pageNum, pageSize, keyword));
    }

    @GetMapping("/customer/summary")
    public Result<Map<String, Object>> customerSummary(@RequestParam(required = false) String keyword) {
        return Result.success(collectionService.customerSummary(keyword));
    }

    /** 客户充值（余额增加） */
    @PostMapping("/customer/recharge")
    public Result<Boolean> rechargeCustomer(@RequestBody Map<String, Object> body) {
        Long customerId = ((Number) body.get("customerId")).longValue();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        collectionService.rechargeCustomer(customerId, amount);
        return Result.success(true);
    }
}
