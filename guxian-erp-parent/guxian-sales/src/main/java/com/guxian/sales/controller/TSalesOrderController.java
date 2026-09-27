package com.guxian.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.result.Result;
import com.guxian.sales.dto.ReceivePaymentDTO;
import com.guxian.sales.dto.SalesOrderDTO;
import com.guxian.sales.entity.TSalesOrder;
import com.guxian.sales.entity.TSalesOrderItem;
import com.guxian.sales.service.TSalesOrderService;
import com.guxian.sales.vo.DictOptionVO;
import com.guxian.sales.vo.OrderItemQueryVO;
import com.guxian.sales.vo.SalesOrderItemRowVO;
import com.guxian.sales.vo.SalesOrderQueryVO;
import com.guxian.sales.vo.SalesOrderStatsVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 销售订单：下单录入、订单列表、订单明细、收付款、状态流转
 */
@RestController
@RequestMapping("/sales/order")
public class TSalesOrderController {

    @Resource
    private TSalesOrderService orderService;

    // ==================== 查询 ====================

    /** 订单分页（订单维度：订单列表&审核 / 跟踪共用） */
    @GetMapping("/page")
    public Result<IPage<SalesOrderDTO>> page(SalesOrderQueryVO query) {
        Page<TSalesOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        return Result.success(orderService.pageOrder(page, query));
    }

    /** 订单明细分页（产品行/子单维度：下单、订单明细页面共用） */
    @GetMapping("/item/page")
    public Result<IPage<SalesOrderItemRowVO>> itemPage(OrderItemQueryVO query) {
        Page<TSalesOrderItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        return Result.success(orderService.pageItem(page, query));
    }

    /** 订单明细底部加载统计（合计） */
    @GetMapping("/item/stats")
    public Result<SalesOrderStatsVO> itemStats(OrderItemQueryVO query) {
        return Result.success(orderService.stats(query));
    }

    /** 字典下拉选项：产品(style_* 系列) + 颜色/网子/把手/锁具/加杆/安装方式等 */
    @GetMapping("/dict/options")
    public Result<Map<String, List<DictOptionVO>>> dictOptions() {
        return Result.success(orderService.dictOptions());
    }

    /** 订单详情（主表+明细） */
    @GetMapping("/{orderId}")
    public Result<SalesOrderDTO> info(@PathVariable("orderId") Long orderId) {
        return Result.success(orderService.getDetail(orderId));
    }

    // ==================== 录入 ====================

    /** 新增/编辑订单（保存为订单未受理） */
    @PostMapping("/save")
    @OperLog(operModule = "销售订单", operType = "保存", operContent = "新增/编辑销售订单")
    public Result<Void> save(@RequestBody SalesOrderDTO dto) {
        orderService.saveOrder(dto);
        return Result.success();
    }

    // ==================== 状态流转 ====================

    /** 受理通过 */
    @PutMapping("/audit/pass/{orderId}")
    @OperLog(operModule = "销售订单", operType = "受理", operContent = "受理销售订单")
    public Result<Void> auditPass(@PathVariable("orderId") Long orderId) {
        orderService.audit(orderId, true, null);
        return Result.success();
    }

    /** 驳回（body 传 rejectReason） */
    @PutMapping("/audit/reject/{orderId}")
    @OperLog(operModule = "销售订单", operType = "受理", operContent = "驳回销售订单")
    public Result<Void> auditReject(@PathVariable("orderId") Long orderId, @RequestBody Map<String, String> body) {
        orderService.audit(orderId, false, body == null ? null : body.get("rejectReason"));
        return Result.success();
    }

    /** 开工（已受理 -> 生产中，生产模块预留） */
    @PutMapping("/produce/start/{orderId}")
    @OperLog(operModule = "销售订单", operType = "生产", operContent = "订单开工（预留）")
    public Result<Void> startProduce(@PathVariable("orderId") Long orderId) {
        orderService.startProduce(orderId);
        return Result.success();
    }

    /** 完工（生产中 -> 已完工，生产模块预留） */
    @PutMapping("/produce/finish/{orderId}")
    @OperLog(operModule = "销售订单", operType = "生产", operContent = "订单完工（预留）")
    public Result<Void> finishProduce(@PathVariable("orderId") Long orderId) {
        orderService.finishProduce(orderId);
        return Result.success();
    }

    /** 发货（已完工 -> 已发货） */
    @PutMapping("/deliver/{orderId}")
    @OperLog(operModule = "销售订单", operType = "发货", operContent = "订单发货")
    public Result<Void> deliver(@PathVariable("orderId") Long orderId) {
        orderService.deliver(orderId);
        return Result.success();
    }

    /** 完成（已发货 -> 已完成） */
    @PutMapping("/complete/{orderId}")
    @OperLog(operModule = "销售订单", operType = "完成", operContent = "订单完成确认")
    public Result<Void> complete(@PathVariable("orderId") Long orderId) {
        orderService.complete(orderId);
        return Result.success();
    }

    /** 取消（未受理/已驳回 -> 已取消） */
    @PutMapping("/cancel/{orderId}")
    @OperLog(operModule = "销售订单", operType = "取消", operContent = "取消销售订单")
    public Result<Void> cancel(@PathVariable("orderId") Long orderId,
                               @RequestBody(required = false) Map<String, String> body) {
        orderService.cancel(orderId, body == null ? null : body.get("cancelReason"));
        return Result.success();
    }

    // ==================== 收付款 ====================

    /** 收款登记 */
    @PutMapping("/receive/{orderId}")
    @OperLog(operModule = "销售订单", operType = "收款", operContent = "订单收款登记")
    public Result<Void> receive(@PathVariable("orderId") Long orderId, @RequestBody ReceivePaymentDTO dto) {
        orderService.receivePayment(orderId, dto);
        return Result.success();
    }

    /** 结清（人工确认） */
    @PutMapping("/settle/{orderId}")
    @OperLog(operModule = "销售订单", operType = "收款", operContent = "订单结清")
    public Result<Void> settle(@PathVariable("orderId") Long orderId) {
        orderService.settle(orderId);
        return Result.success();
    }

    // ==================== 删除 ====================

    /** 删除订单（逻辑删除，仅未受理/已驳回/已取消可删） */
    @DeleteMapping("/{orderId}")
    @OperLog(operModule = "销售订单", operType = "删除", operContent = "删除销售订单")
    public Result<Void> remove(@PathVariable("orderId") Long orderId) {
        TSalesOrder order = orderService.getById(orderId);
        if (order == null) {
            return Result.success();
        }
        // 已进入生产流程的订单不允许删除
        if (order.getOrderStatus() != null
                && order.getOrderStatus() >= 1 && order.getOrderStatus() <= 5) {
            return Result.fail("订单已进入业务流程，不允许删除，可取消");
        }
        orderService.removeById(orderId);
        return Result.success();
    }
}
