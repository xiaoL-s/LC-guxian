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

    /** 拆单生成工单 */
    Long splitOrderToWork(Long salesOrderId, Long shelfId, String remark);

    IPage<TWorkOrder> pageWork(Page<TWorkOrder> page, String keyword, String workStatus, Long customerId);

    WorkOrderDetailVO getDetail(Long workId);

    /** 扫码报工信息：工单号/客户/订单数量上限/已报数量（小程序扫码后展示与校验） */
    Map<String, Object> scanInfo(Long workId);

    /** 工序报工（计件） */
    void reportProcess(WorkReportDTO dto);

    /** 撤回报工（仅超级管理员）：撤销指定产品行指定工序的报工记录与流水 */
    void revokeReport(Long workId, Long itemId, Long processId);

    /** 撤回完工入库（仅超级管理员）：删除入库单，工单回生产中，订单回生产中 */
    void revokeFinish(Long workId);

    /** 完工入库：生成成品入库单 + 回写销售订单已完工 */
    TProductInstock finishAndInstock(Long workId, Long shelfId, String remark);
}
