package com.guxian.sales.controller;

import com.guxian.annotation.OperLog;
import com.guxian.result.Result;
import com.guxian.sales.entity.TOrderRemark;
import com.guxian.sales.service.TOrderRemarkService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单备注记录：订单详情「备注信息」标签的数据接口（工作人员手动备注）
 */
@RestController
@RequestMapping("/sales/order/remark")
public class TOrderRemarkController {

    @Resource
    private TOrderRemarkService remarkService;

    /** 按订单查询备注列表（创建时间倒序） */
    @GetMapping("/list")
    public Result<List<TOrderRemark>> list(@RequestParam("orderId") Long orderId) {
        return Result.success(remarkService.listByOrder(orderId));
    }

    /** 新增工作人员备注（自动记录操作人姓名与时间） */
    @PostMapping("/save")
    @OperLog(operModule = "销售订单", operType = "备注", operContent = "订单添加工作人员备注")
    public Result<Void> save(@RequestBody Map<String, Object> body) {
        Long orderId = body.get("orderId") == null ? null : Long.valueOf(String.valueOf(body.get("orderId")));
        String remark = body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        remarkService.saveRemark(orderId, remark);
        return Result.success();
    }
}
