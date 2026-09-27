package com.guxian.produce.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.annotation.OperLog;
import com.guxian.produce.dto.WorkReportDTO;
import com.guxian.produce.entity.TProductInstock;
import com.guxian.produce.entity.TWorkOrder;
import com.guxian.produce.service.TWorkOrderService;
import com.guxian.produce.vo.WorkOrderDetailVO;
import com.guxian.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "生产工单管理")
@RestController
@RequestMapping("/production/workorder")
public class TWorkOrderController {

    @Resource
    private TWorkOrderService workOrderService;

    /** 待拆单的已审核订单 */
    @GetMapping("/auditedOrders")
    public Result<List<Map<String, Object>>> auditedOrders(@RequestParam(required = false) String keyword) {
        return Result.success(workOrderService.listAuditedOrders(keyword));
    }

    /** 拆单生成工单 */
    @OperLog(operModule = "生产工单", operContent = "拆单生成工单")
    @PostMapping("/split")
    public Result<Long> split(@RequestParam("salesOrderId") Long salesOrderId,
                              @RequestParam(value = "shelfId", required = false) Long shelfId,
                              @RequestParam(value = "remark", required = false) String remark) {
        return Result.success(workOrderService.splitOrderToWork(salesOrderId, shelfId, remark));
    }

    /** 工单分页 */
    @GetMapping("/page")
    public Result<IPage<TWorkOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                          @RequestParam(defaultValue = "10") long pageSize,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String workStatus,
                                          @RequestParam(required = false) Long customerId) {
        return Result.success(workOrderService.pageWork(new Page<>(pageNum, pageSize), keyword, workStatus, customerId));
    }

    /** 工单详情 */
    @GetMapping("/{workId}")
    public Result<WorkOrderDetailVO> detail(@PathVariable("workId") Long workId) {
        return Result.success(workOrderService.getDetail(workId));
    }

    /** 领料 */
    @OperLog(operModule = "生产工单", operContent = "工单领料")
    @PostMapping("/pick")
    public Result<Void> pick(@RequestParam("workId") Long workId,
                             @RequestBody List<Map<String, Object>> pickList) {
        workOrderService.pickMaterials(workId, pickList);
        return Result.success();
    }

    /** 工序报工 */
    @OperLog(operModule = "生产报工", operContent = "工序报工计件")
    @PostMapping("/report")
    public Result<Void> report(@RequestBody WorkReportDTO dto) {
        workOrderService.reportProcess(dto);
        return Result.success();
    }

    /** 完工入库 */
    @OperLog(operModule = "生产工单", operContent = "完工入库")
    @PostMapping("/finish")
    public Result<TProductInstock> finish(@RequestParam("workId") Long workId,
                                          @RequestParam(value = "shelfId", required = false) Long shelfId,
                                          @RequestParam(value = "remark", required = false) String remark) {
        return Result.success(workOrderService.finishAndInstock(workId, shelfId, remark));
    }
}
