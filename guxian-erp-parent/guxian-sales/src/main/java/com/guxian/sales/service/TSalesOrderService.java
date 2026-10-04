package com.guxian.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.sales.dto.ReceivePaymentDTO;
import com.guxian.sales.dto.SalesOrderDTO;
import com.guxian.sales.entity.TSalesOrder;
import com.guxian.sales.entity.TSalesOrderItem;
import com.guxian.sales.vo.DictOptionVO;
import com.guxian.sales.vo.OrderItemQueryVO;
import com.guxian.sales.vo.SalesOrderItemRowVO;
import com.guxian.sales.vo.SalesOrderQueryVO;
import com.guxian.sales.vo.SalesOrderStatsVO;

import java.util.List;
import java.util.Map;

public interface TSalesOrderService extends IService<TSalesOrder> {

    /** 订单分页（订单维度：订单列表 / 进度跟踪共用） */
    IPage<SalesOrderDTO> pageOrder(Page<TSalesOrder> page, SalesOrderQueryVO query);

    /** 订单明细分页（产品行/子单维度：下单、订单明细页面共用） */
    IPage<SalesOrderItemRowVO> pageItem(Page<TSalesOrderItem> page, OrderItemQueryVO query);

    /** 订单明细合计（底部加载统计） */
    SalesOrderStatsVO stats(OrderItemQueryVO query);

    /** 订单详情（主表 + 明细） */
    SalesOrderDTO getDetail(Long orderId);

    /** 新增/编辑订单（后端强制重算面积与金额），保存后为订单未受理 */
    void saveOrder(SalesOrderDTO dto);

    /** 受理审核：pass=true受理(0->1)，false驳回(0->6) */
    void audit(Long orderId, boolean pass, String rejectReason);

    /** 收款登记（累加实收，自动推导财务状态） */
    void receivePayment(Long orderId, ReceivePaymentDTO dto);

    /** 结清（人工确认，财务状态 -> 已结清） */
    void settle(Long orderId);

    /** 开工：已受理1 -> 生产中2（生产模块预留，当前手工流转） */
    void startProduce(Long orderId);

    /** 完工：生产中2 -> 已完工3（生产模块预留，当前手工流转） */
    void finishProduce(Long orderId);

    /** 发货（已完工3 -> 已发货4） */
    void deliver(Long orderId);

    /** 完成（已发货4 -> 已完成5） */
    void complete(Long orderId);

    /** 取消（未受理0/已驳回6 -> 已取消7） */
    void cancel(Long orderId, String cancelReason);

    /** 字典下拉选项：产品(style_* 系列) + 颜色/网子/把手/锁具/加杆等属性 */
    Map<String, List<DictOptionVO>> dictOptions();

    /** 保存后工单生成情况提示（新增订单自动拆单后调用；无工单时提示手动开工补齐） */
    String checkWorkOrderTip(Long orderId);

    /** 删除订单：校验工单/业务流程后，先删明细再逻辑删订单 */
    void deleteOrder(Long orderId);
}
