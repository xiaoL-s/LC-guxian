package com.guxian.produce.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.produce.dto.WorkReportDTO;
import com.guxian.produce.entity.TProductInstock;
import com.guxian.produce.entity.TWorkOrder;
import com.guxian.produce.vo.WorkOrderDetailVO;

import java.util.List;
import java.util.Map;

public interface TWorkOrderService extends IService<TWorkOrder> {

    /** 待拆单的已审核销售订单 */
    List<Map<String, Object>> listAuditedOrders(String keyword);

    /** 拆单生成工单：按订单明细×BOM算物料需求 */
    Long splitOrderToWork(Long salesOrderId, Long shelfId, String remark);

    IPage<TWorkOrder> pageWork(Page<TWorkOrder> page, String keyword, String workStatus, Long customerId);

    WorkOrderDetailVO getDetail(Long workId);

    /** 工单领料（扣减库存，一次领一个工单全部或部分） */
    void pickMaterials(Long workId, List<Map<String, Object>> pickList);

    /** 工序报工（计件） */
    void reportProcess(WorkReportDTO dto);

    /** 完工入库：生成成品入库单 + 回写销售订单已完工 */
    TProductInstock finishAndInstock(Long workId, Long shelfId, String remark);
}
