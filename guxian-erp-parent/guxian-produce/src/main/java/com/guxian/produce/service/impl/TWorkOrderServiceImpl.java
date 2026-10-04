package com.guxian.produce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.context.UserContext;
import com.guxian.exception.BusinessException;
import com.guxian.produce.dto.WorkReportDTO;
import com.guxian.produce.entity.*;
import com.guxian.produce.mapper.*;
import com.guxian.produce.service.TWorkOrderService;
import com.guxian.produce.vo.WorkOrderDetailVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TWorkOrderServiceImpl extends ServiceImpl<TWorkOrderMapper, TWorkOrder> implements TWorkOrderService {

    /** 订单状态：1已审核(待排产) 2生产中 3已完工 */
    private static final int ORDER_AUDITED = 1;
    private static final int ORDER_PRODUCING = 2;
    private static final int ORDER_FINISHED = 3;

    /** 工单状态 */
    private static final String STATUS_WAIT = "WAIT_PROCESS";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_FINISHED = "FINISHED";

    /** 明细行状态：1已审核 2生产中 3已完工 */
    private static final int ITEM_AUDITED = 1;
    private static final int ITEM_PRODUCING = 2;
    private static final int ITEM_FINISHED = 3;

    @Resource
    private SalesOrderReadMapper salesOrderReadMapper;
    @Resource
    private TProcessDictMapper processDictMapper;
    @Resource
    private TWorkProcessRecordMapper processRecordMapper;
    @Resource
    private TWorkReportMapper workReportMapper;
    @Resource
    private TProductInstockMapper productInstockMapper;
    @Resource
    private TShelfReadMapper shelfReadMapper;

    // ==================================================================
    // 拆单：已审核订单 -> 工单 + 物料需求
    // ==================================================================

    @Override
    public List<Map<String, Object>> listAuditedOrders(String keyword) {
        String k = keyword == null ? "" : keyword.trim();
        if (!k.isEmpty()) {
            return salesOrderReadMapper.selectAuditedOrdersWithKeyword("%" + k + "%");
        }
        return salesOrderReadMapper.selectAuditedOrders();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long splitOrderToWork(Long salesOrderId, Long shelfId, String remark) {
        Map<String, Object> header = salesOrderReadMapper.selectOrderHeader(salesOrderId);
        if (header == null) {
            throw new BusinessException("销售订单不存在");
        }
        Object statusObj = header.get("orderStatus");
        int orderStatus = statusObj == null ? -1 : ((Number) statusObj).intValue();
        // 容错：已受理(1) 或 生产中(2) 且无工单（历史"预留开工"造成的卡死单）均可拆单
        if (orderStatus != ORDER_AUDITED && orderStatus != ORDER_PRODUCING) {
            throw new BusinessException("仅已受理订单可拆单生产");
        }
        // 已存在工单则禁止重复拆单
        LambdaQueryWrapper<TWorkOrder> existW = new LambdaQueryWrapper<>();
        existW.eq(TWorkOrder::getSalesOrderId, salesOrderId);
        if (baseMapper.selectCount(existW) > 0) {
            throw new BusinessException("该订单已拆单生成工单，请勿重复拆单");
        }

        // 订单明细
        List<Map<String, Object>> items = salesOrderReadMapper.selectOrderItems(salesOrderId);
        if (items == null || items.isEmpty()) {
            throw new BusinessException("订单无产品明细，无法拆单");
        }

        // 创建工单：工单号统一使用销售订单编号（下单保存即自动拆单，便于按订单追溯生产）
        Object orderNoObj = header.get("orderNo");
        String orderNo = orderNoObj == null ? null : orderNoObj.toString();
        TWorkOrder work = new TWorkOrder();
        work.setWorkNo(StringUtils.hasText(orderNo) ? orderNo : generateWorkNo());
        work.setSalesOrderId(salesOrderId);
        work.setCustomerId(header.get("customerId") == null ? null : ((Number) header.get("customerId")).longValue());
        // 未指定货架时自动分配第一个启用货架（销售侧一键开工场景）
        Long targetShelf = shelfId;
        if (targetShelf == null) {
            Map<String, Object> firstShelf = shelfReadMapper.selectFirstEnabled();
            if (firstShelf == null) {
                throw new BusinessException("未配置成品货架，请先到【库存-货架管理】创建启用状态的货架");
            }
            targetShelf = ((Number) firstShelf.get("shelfId")).longValue();
        }
        work.setShelfId(targetShelf);
        BigDecimal totalArea = header.get("totalArea") == null ? BigDecimal.ZERO
                : new BigDecimal(header.get("totalArea").toString());
        work.setTotalArea(totalArea.setScale(2, RoundingMode.HALF_UP));
        work.setWorkStatus(STATUS_WAIT);
        work.setIsRework(0);
        work.setRemark(remark);
        baseMapper.insert(work);

        // 回写销售订单：状态=生产中，明细行=生产中 + 工单ID
        salesOrderReadMapper.updateOrderStatus(salesOrderId, ORDER_PRODUCING);
        salesOrderReadMapper.updateItemByOrder(salesOrderId, ITEM_PRODUCING, work.getWorkId());
        return work.getWorkId();
    }

    // ==================================================================
    // 工单查询
    // ==================================================================

    @Override
    public IPage<TWorkOrder> pageWork(Page<TWorkOrder> page, String keyword, String workStatus, Long customerId) {
        LambdaQueryWrapper<TWorkOrder> w = new LambdaQueryWrapper<>();
        w.like(StringUtils.hasText(keyword), TWorkOrder::getWorkNo, keyword);
        w.eq(StringUtils.hasText(workStatus), TWorkOrder::getWorkStatus, workStatus);
        w.eq(customerId != null, TWorkOrder::getCustomerId, customerId);
        w.orderByDesc(TWorkOrder::getWorkId);
        return baseMapper.selectPage(page, w);
    }

    @Override
    public WorkOrderDetailVO getDetail(Long workId) {
        TWorkOrder work = mustGet(workId);
        WorkOrderDetailVO vo = new WorkOrderDetailVO();
        vo.setWorkId(work.getWorkId());
        vo.setWorkNo(work.getWorkNo());
        vo.setSalesOrderId(work.getSalesOrderId());
        vo.setCustomerId(work.getCustomerId());
        vo.setShelfId(work.getShelfId());
        vo.setTotalArea(work.getTotalArea());
        vo.setWorkStatus(work.getWorkStatus());
        vo.setWorkStatusDesc(descOf(work.getWorkStatus()));
        vo.setIsRework(work.getIsRework());
        vo.setRemark(work.getRemark());
        vo.setFinishTime(work.getFinishTime());
        vo.setCreateTime(work.getCreateTime());

        // 订单信息
        Map<String, Object> header = salesOrderReadMapper.selectOrderHeader(work.getSalesOrderId());
        if (header != null) {
            vo.setOrderNo(header.get("orderNo") == null ? "" : header.get("orderNo").toString());
            vo.setCustomerName(header.get("customerName") == null ? "" : header.get("customerName").toString());
            vo.setCustomerPhone(header.get("customerPhone") == null ? "" : header.get("customerPhone").toString());
            vo.setCustomerAddress(header.get("customerAddress") == null ? "" : header.get("customerAddress").toString());
            vo.setOrderType(header.get("orderType") == null ? "" : header.get("orderType").toString());
            vo.setOrderDate(toLocalDate(header.get("orderDate")));
            vo.setExpectDate(toLocalDate(header.get("expectDate")));
        }
        // 货架名
        if (work.getShelfId() != null) {
            Map<String, Object> shelf = shelfReadMapper.selectShelf(work.getShelfId());
            if (shelf != null) {
                vo.setShelfName(shelf.get("shelfName") == null ? "" : shelf.get("shelfName").toString());
            }
        }
        // 订单明细
        vo.setOrderItems(salesOrderReadMapper.selectOrderItems(work.getSalesOrderId()));
        // 工序进度
        List<TProcessDict> processes = processDictMapper.selectList(new LambdaQueryWrapper<TProcessDict>()
                .eq(TProcessDict::getStatus, 1).orderByAsc(TProcessDict::getProcessSort));
        Map<Long, Map<String, Object>> doneMap = new HashMap<>();
        List<TWorkProcessRecord> records = processRecordMapper.selectList(new LambdaQueryWrapper<TWorkProcessRecord>()
                .eq(TWorkProcessRecord::getWorkId, workId));
        for (TWorkProcessRecord r : records) {
            doneMap.put(r.getProcessId(), Map.of("operUsername", r.getOperUsername() == null ? "" : r.getOperUsername(),
                    "scanTime", r.getScanTime() == null ? "" : r.getScanTime().toString()));
        }
        List<Map<String, Object>> processList = new ArrayList<>();
        for (TProcessDict p : processes) {
            Map<String, Object> row = new HashMap<>();
            row.put("processId", p.getProcessId());
            row.put("processCode", p.getProcessCode());
            row.put("processName", p.getProcessName());
            row.put("processSort", p.getProcessSort());
            row.put("unitPrice", p.getUnitPrice());
            row.put("done", doneMap.containsKey(p.getProcessId()));
            row.put("operUsername", doneMap.getOrDefault(p.getProcessId(), Map.of()).get("operUsername"));
            row.put("scanTime", doneMap.getOrDefault(p.getProcessId(), Map.of()).get("scanTime"));
            processList.add(row);
        }
        vo.setProcessList(processList);
        // 报工记录
        List<Map<String, Object>> reportList = new ArrayList<>();
        List<TWorkReport> reports = workReportMapper.selectList(new LambdaQueryWrapper<TWorkReport>()
                .eq(TWorkReport::getWorkOrderId, workId).orderByDesc(TWorkReport::getId));
        for (TWorkReport r : reports) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", r.getId());
            row.put("processId", r.getProcessId());
            row.put("workerId", r.getWorkerId());
            row.put("qualifiedNum", r.getQualifiedNum());
            row.put("badNum", r.getBadNum());
            row.put("unitPrice", r.getUnitPrice());
            row.put("pieceWage", r.getPieceWage());
            row.put("reportTime", r.getReportTime() == null ? "" : r.getReportTime().toString());
            TProcessDict p = processDictMapper.selectById(r.getProcessId());
            row.put("processName", p == null ? "" : p.getProcessName());
            reportList.add(row);
        }
        vo.setReportList(reportList);
        return vo;
    }

    // ==================================================================
    // 工序报工（计件）
    // ==================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportProcess(WorkReportDTO dto) {
        if (dto.getWorkId() == null || dto.getProcessId() == null) {
            throw new BusinessException("工单与工序不能为空");
        }
        if (dto.getWorkerId() == null) {
            throw new BusinessException("请选择报工工人");
        }
        TWorkOrder work = mustGet(dto.getWorkId());
        if (STATUS_FINISHED.equals(work.getWorkStatus())) {
            throw new BusinessException("工单已完工，不能报工");
        }
        // 待生产状态的工单：首次报工自动转为生产中（无需领料）
        if (STATUS_WAIT.equals(work.getWorkStatus())) {
            TWorkOrder upd = new TWorkOrder();
            upd.setWorkId(dto.getWorkId());
            upd.setWorkStatus(STATUS_PROCESSING);
            baseMapper.updateById(upd);
        }
        TProcessDict process = processDictMapper.selectById(dto.getProcessId());
        if (process == null || process.getStatus() == null || process.getStatus() != 1) {
            throw new BusinessException("工序不存在或已停用");
        }
        // 同一工单同一工序防重复
        LambdaQueryWrapper<TWorkProcessRecord> dup = new LambdaQueryWrapper<>();
        dup.eq(TWorkProcessRecord::getWorkId, dto.getWorkId())
                .eq(TWorkProcessRecord::getProcessId, dto.getProcessId());
        if (processRecordMapper.selectCount(dup) > 0) {
            throw new BusinessException("该工序已完成报工，请勿重复");
        }
        // ---- 工序流转校验（按纱窗厂业务流程）----
        // 已完成的工序 code 集合
        LambdaQueryWrapper<TWorkProcessRecord> doneQ = new LambdaQueryWrapper<>();
        doneQ.eq(TWorkProcessRecord::getWorkId, dto.getWorkId());
        List<TWorkProcessRecord> doneRecs = processRecordMapper.selectList(doneQ);
        java.util.Set<String> doneCodes = new java.util.HashSet<>();
        for (TWorkProcessRecord r : doneRecs) {
            if (r.getProcessCode() != null) doneCodes.add(r.getProcessCode());
        }
        // 组装：必须开料(CUT)和剪网(CUT_NET)都完成（两工序并行，互不阻塞）
        if ("ASSEMBLE".equals(process.getProcessCode())) {
            if (!doneCodes.contains("CUT") || !doneCodes.contains("CUT_NET")) {
                throw new BusinessException("开料和剪网两个工序全部完成后，才能组装");
            }
        }
        // 打包：必须组装完成
        if ("PACKAGE".equals(process.getProcessCode()) && !doneCodes.contains("ASSEMBLE")) {
            throw new BusinessException("组装完成后才能打包");
        }
        // 入库上架：必须打包完成
        if ("STOCK_IN".equals(process.getProcessCode()) && !doneCodes.contains("PACKAGE")) {
            throw new BusinessException("打包完成后才能入库上架");
        }
        int qualified = dto.getQualifiedNum() == null ? 0 : dto.getQualifiedNum();
        int bad = dto.getBadNum() == null ? 0 : dto.getBadNum();
        if (qualified <= 0 && bad <= 0) {
            throw new BusinessException("合格数量与不良数量不能同时为 0");
        }
        // 计件工资 = 合格 × 单价快照
        BigDecimal price = process.getUnitPrice() == null ? BigDecimal.ZERO : process.getUnitPrice();
        BigDecimal wage = price.multiply(BigDecimal.valueOf(qualified)).setScale(2, RoundingMode.HALF_UP);

        TWorkReport report = new TWorkReport();
        report.setWorkOrderId(dto.getWorkId());
        report.setProcessId(dto.getProcessId());
        report.setWorkerId(dto.getWorkerId());
        report.setQualifiedNum(qualified);
        report.setBadNum(bad);
        report.setUnitPrice(price);
        report.setPieceWage(wage);
        report.setReportTime(LocalDateTime.now());
        workReportMapper.insert(report);

        // 工序流水（防重复 + 进度展示）
        TWorkProcessRecord record = new TWorkProcessRecord();
        record.setWorkId(dto.getWorkId());
        record.setWorkNo(work.getWorkNo());
        record.setProcessId(process.getProcessId());
        record.setProcessCode(process.getProcessCode());
        record.setOperUserId(dto.getWorkerId());
        record.setOperUsername(loadUserName(dto.getWorkerId()));
        record.setScanTime(LocalDateTime.now());
        record.setResultStatus(bad > 0 ? "FAIL" : "PASS");
        record.setRemark(dto.getRemark());
        processRecordMapper.insert(record);
    }

    // ==================================================================
    // 完工入库：生成入库单 + 回写订单已完工
    // ==================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TProductInstock finishAndInstock(Long workId, Long shelfId, String remark) {
        TWorkOrder work = mustGet(workId);
        if (STATUS_FINISHED.equals(work.getWorkStatus())) {
            throw new BusinessException("工单已完工入库，请勿重复操作");
        }
        // 校验全部启用工序已完成
        List<TProcessDict> processes = processDictMapper.selectList(new LambdaQueryWrapper<TProcessDict>()
                .eq(TProcessDict::getStatus, 1).orderByAsc(TProcessDict::getProcessSort));
        if (!processes.isEmpty()) {
            List<TWorkProcessRecord> records = processRecordMapper.selectList(new LambdaQueryWrapper<TWorkProcessRecord>()
                    .eq(TWorkProcessRecord::getWorkId, workId));
            Set<Long> doneProcessIds = records.stream().map(TWorkProcessRecord::getProcessId).collect(Collectors.toSet());
            for (TProcessDict p : processes) {
                if (!doneProcessIds.contains(p.getProcessId())) {
                    throw new BusinessException("尚有工序未完成：" + p.getProcessName());
                }
            }
        }
        // 入库数量 = 订单明细数量合计
        Map<String, Object> numMap = salesOrderReadMapper.selectOrderItemNum(work.getSalesOrderId());
        int inNum = numMap == null || numMap.get("totalNum") == null ? 0 : ((Number) numMap.get("totalNum")).intValue();

        TProductInstock instock = new TProductInstock();
        instock.setWorkOrderId(workId);
        instock.setOrderId(work.getSalesOrderId());
        instock.setShelfId(shelfId == null ? work.getShelfId() : shelfId);
        instock.setInNum(inNum);
        instock.setInstockStatus(2); // 直接完成
        instock.setStockUserId(UserContext.getUserId());
        instock.setInstockTime(LocalDateTime.now());
        instock.setRemark(remark);
        productInstockMapper.insert(instock);

        // 工单完工
        TWorkOrder upd = new TWorkOrder();
        upd.setWorkId(workId);
        upd.setWorkStatus(STATUS_FINISHED);
        upd.setFinishTime(LocalDateTime.now());
        baseMapper.updateById(upd);

        // 回写销售订单：已完工 + 明细已完工
        salesOrderReadMapper.updateOrderStatus(work.getSalesOrderId(), ORDER_FINISHED);
        salesOrderReadMapper.updateItemByOrder(work.getSalesOrderId(), ITEM_FINISHED, workId);
        return instock;
    }

    // ==================================================================
    // 私有方法
    // ==================================================================

    private TWorkOrder mustGet(Long workId) {
        TWorkOrder work = baseMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("工单不存在");
        }
        return work;
    }

    private String loadUserName(Long userId) {
        Map<String, Object> user = salesOrderReadMapper.selectUserName(userId);
        if (user == null || user.get("realName") == null) {
            return "";
        }
        return user.get("realName").toString();
    }

    private synchronized String generateWorkNo() {
        String prefix = "WO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<TWorkOrder> w = new LambdaQueryWrapper<>();
        w.likeRight(TWorkOrder::getWorkNo, prefix).orderByDesc(TWorkOrder::getWorkNo).last("LIMIT 1");
        TWorkOrder last = baseMapper.selectOne(w);
        int seq = 1;
        if (last != null && last.getWorkNo() != null && last.getWorkNo().length() >= prefix.length()) {
            try {
                seq = Integer.parseInt(last.getWorkNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    private String descOf(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case STATUS_WAIT -> "待生产";
            case STATUS_PROCESSING -> "生产中";
            case STATUS_FINISHED -> "已完工";
            case "CANCELED" -> "已取消";
            default -> status;
        };
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** 只读查询返回的日期字段兼容转换（Date / LocalDate / 字符串） */
    private java.time.LocalDate toLocalDate(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof java.time.LocalDate ld) {
            return ld;
        }
        if (v instanceof java.sql.Date sd) {
            return sd.toLocalDate();
        }
        String s = v.toString().trim();
        if (s.length() < 10) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(s.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }
}
