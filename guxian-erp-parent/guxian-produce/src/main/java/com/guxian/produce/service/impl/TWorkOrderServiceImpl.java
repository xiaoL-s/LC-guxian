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

    /** 全局统一时间格式（与系统其他模块一致） */
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
    private SysUserReadMapper sysUserReadMapper;
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
    /** 工序产品计件价（同一工序不同产品单价不同） */
    @Resource
    private TProcessProductPriceMapper processProductPriceMapper;

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
        IPage<TWorkOrder> p = baseMapper.selectPage(page, w);
        // 列表补充客户名称与产品名（同库共享订单表）
        for (TWorkOrder wk : p.getRecords()) {
            if (wk.getSalesOrderId() != null) {
                Map<String, Object> h = salesOrderReadMapper.selectOrderHeader(wk.getSalesOrderId());
                if (h != null && h.get("customerName") != null) {
                    wk.setCustomerName(h.get("customerName").toString());
                }
                // 产品名汇总（逗号分隔，列表展示）
                List<Map<String, Object>> oi = salesOrderReadMapper.selectOrderItems(wk.getSalesOrderId());
                if (oi != null && !oi.isEmpty()) {
                    String names = oi.stream()
                            .map(x -> x.get("productName") == null ? "" : x.get("productName").toString())
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .collect(Collectors.joining("、"));
                    wk.setProductNames(names);
                }
            }
        }
        return p;
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
        // 工序进度：done = 该工序在工单全部产品行都已报工（多产品工单整体进度）
        List<TProcessDict> processes = processDictMapper.selectList(new LambdaQueryWrapper<TProcessDict>()
                .eq(TProcessDict::getStatus, 1).orderByAsc(TProcessDict::getProcessSort));
        List<Map<String, Object>> orderItems = vo.getOrderItems();
        Set<Long> itemIds = new HashSet<>();
        for (Map<String, Object> it : orderItems) {
            if (it.get("itemId") != null) itemIds.add(((Number) it.get("itemId")).longValue());
        }
        List<TWorkProcessRecord> records = processRecordMapper.selectList(new LambdaQueryWrapper<TWorkProcessRecord>()
                .eq(TWorkProcessRecord::getWorkId, workId));
        // processId -> (itemId -> 操作人/时间)
        Map<Long, Map<Long, Map<String, Object>>> doneMap = new HashMap<>();
        for (TWorkProcessRecord r : records) {
            doneMap.computeIfAbsent(r.getProcessId(), k -> new HashMap<>())
                    .put(r.getItemId(), Map.of("operUsername", r.getOperUsername() == null ? "" : r.getOperUsername(),
                            "scanTime", r.getScanTime() == null ? "" : TIME_FMT.format(r.getScanTime())));
        }
        List<Map<String, Object>> processList = new ArrayList<>();
        for (TProcessDict p : processes) {
            Map<String, Object> row = new HashMap<>();
            row.put("processId", p.getProcessId());
            row.put("processCode", p.getProcessCode());
            row.put("processName", p.getProcessName());
            row.put("processSort", p.getProcessSort());
            row.put("unitPrice", p.getUnitPrice());
            Map<Long, Map<String, Object>> byItem = doneMap.getOrDefault(p.getProcessId(), new HashMap<>());
            boolean done = !itemIds.isEmpty() && byItem.keySet().containsAll(itemIds);
            row.put("done", done);
            // 最后一条操作信息（列表展示用）
            Map<String, Object> last = new HashMap<>();
            for (Map<String, Object> v : byItem.values()) last = v;
            row.put("operUsername", last.getOrDefault("operUsername", ""));
            row.put("scanTime", last.getOrDefault("scanTime", ""));
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
            row.put("itemId", r.getItemId());
            row.put("productName", r.getProductName() == null ? "" : r.getProductName());
            row.put("workerId", r.getWorkerId());
            row.put("workerName", loadUserName(r.getWorkerId()));
            row.put("qualifiedNum", r.getQualifiedNum());
            row.put("badNum", r.getBadNum());
            row.put("unitPrice", r.getUnitPrice());
            row.put("pieceWage", r.getPieceWage());
            row.put("remark", r.getRemark() == null ? "" : r.getRemark());
            row.put("reportTime", r.getReportTime() == null ? "" : TIME_FMT.format(r.getReportTime()));
            row.put("updateTime", r.getUpdateTime() == null ? "" : TIME_FMT.format(r.getUpdateTime()));
            TProcessDict p = processDictMapper.selectById(r.getProcessId());
            row.put("processName", p == null ? "" : p.getProcessName());
            reportList.add(row);
        }
        vo.setReportList(reportList);
        return vo;
    }

    @Override
    public Map<String, Object> scanInfo(Long workId) {
        TWorkOrder work = mustGet(workId);
        Map<String, Object> vo = new HashMap<>();
        vo.put("workId", work.getWorkId());
        vo.put("workNo", work.getWorkNo());
        vo.put("workStatus", work.getWorkStatus());
        vo.put("workStatusDesc", descOf(work.getWorkStatus()));
        // 客户与订单信息
        Map<String, Object> header = work.getSalesOrderId() == null ? null
                : salesOrderReadMapper.selectOrderHeader(work.getSalesOrderId());
        if (header != null) {
            vo.put("orderNo", header.get("orderNo") == null ? "" : header.get("orderNo").toString());
            vo.put("customerName", header.get("customerName") == null ? "" : header.get("customerName").toString());
        }
        // 该工单订单的产品明细行（一个订单多产品，按产品行分别报工、分别计价）
        List<Map<String, Object>> items = work.getSalesOrderId() == null ? new ArrayList<>()
                : salesOrderReadMapper.selectOrderItems(work.getSalesOrderId());
        // 已报记录按「产品行:工序」归集
        List<TWorkReport> reports = workReportMapper.selectList(new LambdaQueryWrapper<TWorkReport>()
                .eq(TWorkReport::getWorkOrderId, workId));
        Map<String, Integer> reportedByItemProc = new HashMap<>();
        for (TWorkReport r : reports) {
            String key = r.getItemId() + ":" + r.getProcessId();
            reportedByItemProc.merge(key, r.getQualifiedNum() == null ? 0 : r.getQualifiedNum(), Integer::sum);
        }
        // 已完成工序按产品行归集
        List<TWorkProcessRecord> recs = processRecordMapper.selectList(new LambdaQueryWrapper<TWorkProcessRecord>()
                .eq(TWorkProcessRecord::getWorkId, workId));
        Map<Long, Set<Long>> doneByItem = new HashMap<>();
        for (TWorkProcessRecord r : recs) {
            doneByItem.computeIfAbsent(r.getItemId(), k -> new HashSet<>()).add(r.getProcessId());
        }
        List<Map<String, Object>> itemList = new ArrayList<>();
        // 启用工序（供计算各产品可报工序：0 元单价 = 该产品无此工序）
        List<TProcessDict> enabledProcs = processDictMapper.selectList(new LambdaQueryWrapper<TProcessDict>()
                .eq(TProcessDict::getStatus, 1).orderByAsc(TProcessDict::getProcessSort));
        // 该工单所有产品名
        java.util.Set<String> allProducts = new java.util.HashSet<>();
        for (Map<String, Object> it : items) {
            String pn = it.get("productName") == null ? "" : it.get("productName").toString();
            if (!pn.isEmpty()) allProducts.add(pn);
        }
        // 一次性查出本工单所有产品的工序计件价配置
        Map<String, Map<Long, BigDecimal>> cfgByProduct = new HashMap<>();
        if (!allProducts.isEmpty()) {
            List<TProcessProductPrice> cfgs = processProductPriceMapper.selectList(new LambdaQueryWrapper<TProcessProductPrice>()
                    .in(TProcessProductPrice::getProductName, allProducts));
            for (TProcessProductPrice pp : cfgs) {
                cfgByProduct.computeIfAbsent(pp.getProductName(), k -> new HashMap<>())
                        .put(pp.getProcessId(), pp.getPrice());
            }
        }
        for (Map<String, Object> it : items) {
            Long itemId = it.get("itemId") == null ? null : ((Number) it.get("itemId")).longValue();
            String productName = it.get("productName") == null ? "" : it.get("productName").toString();
            Map<String, Object> row = new HashMap<>();
            row.put("itemId", itemId);
            row.put("productName", productName);
            row.put("num", it.get("num") == null ? 0 : ((Number) it.get("num")).intValue());
            Set<Long> doneProcs = doneByItem.getOrDefault(itemId, new HashSet<>());
            row.put("doneProcessIds", doneProcs);
            // 该产品可报工序：严格以产品计件价表为准（t_process_product_price）
            // 表中配置且单价 > 0 才显示；0 元或未配置 = 该产品无此工序
            Map<Long, BigDecimal> cfg = cfgByProduct.getOrDefault(productName, Collections.emptyMap());
            List<Map<String, Object>> avail = new ArrayList<>();
            for (TProcessDict pd : enabledProcs) {
                BigDecimal cfgPrice = cfg.get(pd.getProcessId());
                if (cfgPrice != null && cfgPrice.compareTo(BigDecimal.ZERO) > 0) {
                    Map<String, Object> a = new HashMap<>();
                    a.put("processId", pd.getProcessId());
                    a.put("processName", pd.getProcessName());
                    a.put("processCode", pd.getProcessCode());
                    a.put("unitPrice", cfgPrice);
                    avail.add(a);
                }
            }
            row.put("availableProcesses", avail);
            // 该产品行各工序已报合格数量
            Map<Long, Integer> procReported = new HashMap<>();
            for (Map.Entry<String, Integer> e : reportedByItemProc.entrySet()) {
                String[] parts = e.getKey().split(":");
                if (parts[0].equals(String.valueOf(itemId))) {
                    procReported.merge(Long.valueOf(parts[1]), e.getValue(), Integer::sum);
                }
            }
            row.put("processReported", procReported);
            itemList.add(row);
        }
        vo.put("items", itemList);
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
        if (dto.getItemId() == null) {
            throw new BusinessException("请选择要报工的产品");
        }
        if (dto.getWorkerId() == null) {
            throw new BusinessException("请选择报工工人");
        }
        TWorkOrder work = mustGet(dto.getWorkId());
        if (STATUS_FINISHED.equals(work.getWorkStatus())) {
            throw new BusinessException("工单已完工，不能报工");
        }
        if (work.getSalesOrderId() == null) {
            throw new BusinessException("工单未关联订单，无法报工");
        }
        // ---- 产品明细行校验：一个工单多产品，按产品行分别报工、分别计价 ----
        Map<String, Object> item = salesOrderReadMapper.selectOrderItemById(dto.getItemId());
        if (item == null || item.get("itemId") == null) {
            throw new BusinessException("报工产品不存在");
        }
        List<Map<String, Object>> orderItems = salesOrderReadMapper.selectOrderItems(work.getSalesOrderId());
        boolean belongs = orderItems.stream().anyMatch(x -> x.get("itemId") != null
                && String.valueOf(x.get("itemId")).equals(String.valueOf(dto.getItemId())));
        if (!belongs) {
            throw new BusinessException("该产品不属于此工单的订单");
        }
        String productName = item.get("productName") == null ? "" : item.get("productName").toString();
        int itemNum = item.get("num") == null ? 0 : ((Number) item.get("num")).intValue();
        if (itemNum <= 0) {
            throw new BusinessException("该产品数量异常，无法报工");
        }
        // 待生产状态的工单：首次报工自动转为生产中
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
        // ---- 防重复：同一工单 同一产品行 同一工序 只能报一次 ----
        LambdaQueryWrapper<TWorkProcessRecord> dup = new LambdaQueryWrapper<>();
        dup.eq(TWorkProcessRecord::getWorkId, dto.getWorkId())
                .eq(TWorkProcessRecord::getItemId, dto.getItemId())
                .eq(TWorkProcessRecord::getProcessId, dto.getProcessId());
        if (processRecordMapper.selectCount(dup) > 0) {
            throw new BusinessException("该产品该工序已完成报工，请勿重复");
        }
        // ---- 工序流转校验（按产品行独立流水线）----
        LambdaQueryWrapper<TWorkProcessRecord> doneQ = new LambdaQueryWrapper<>();
        doneQ.eq(TWorkProcessRecord::getWorkId, dto.getWorkId())
                .eq(TWorkProcessRecord::getItemId, dto.getItemId());
        List<TWorkProcessRecord> doneRecs = processRecordMapper.selectList(doneQ);
        java.util.Set<String> doneCodes = new java.util.HashSet<>();
        for (TWorkProcessRecord r : doneRecs) {
            if (r.getProcessCode() != null) doneCodes.add(r.getProcessCode());
        }
        if ("ASSEMBLE".equals(process.getProcessCode())) {
            if (!doneCodes.contains("CUT") || !doneCodes.contains("CUT_NET")) {
                throw new BusinessException("该产品的开料和剪网两个工序全部完成后，才能组装");
            }
        }
        if ("PACKAGE".equals(process.getProcessCode()) && !doneCodes.contains("ASSEMBLE")) {
            throw new BusinessException("该产品组装完成后才能打包");
        }
        if ("STOCK_IN".equals(process.getProcessCode()) && !doneCodes.contains("PACKAGE")) {
            throw new BusinessException("该产品打包完成后才能入库上架");
        }
        int qualified = dto.getQualifiedNum() == null ? 0 : dto.getQualifiedNum();
        int bad = dto.getBadNum() == null ? 0 : dto.getBadNum();
        if (qualified <= 0 && bad <= 0) {
            throw new BusinessException("合格数量与不良数量不能同时为 0");
        }
        // ---- 数量上限校验（防虚报）：同一产品同一工序累计合格数 ≤ 该产品订单数量 ----
        List<TWorkReport> exists = workReportMapper.selectList(new LambdaQueryWrapper<TWorkReport>()
                .eq(TWorkReport::getWorkOrderId, dto.getWorkId())
                .eq(TWorkReport::getItemId, dto.getItemId())
                .eq(TWorkReport::getProcessId, dto.getProcessId()));
        int reported = exists.stream().mapToInt(r -> r.getQualifiedNum() == null ? 0 : r.getQualifiedNum()).sum();
        if (qualified > 0 && reported + qualified > itemNum) {
            throw new BusinessException("报工数量超过该产品订单数量：「" + productName + "」共" + itemNum
                    + "件，该工序已报" + reported + "件，本次最多还能报" + (itemNum - reported) + "件");
        }
        // ---- 计件工资 = 合格数 × 单价快照 ----
        // 计价规则：严格以「工序 × 产品」计件价表为准；
        // 未配置或单价 0 元 = 该产品没有此工序，禁止报工
        LambdaQueryWrapper<TProcessProductPrice> priceQw = new LambdaQueryWrapper<TProcessProductPrice>()
                .eq(TProcessProductPrice::getProcessId, dto.getProcessId())
                .eq(TProcessProductPrice::getProductName, productName)
                .last("LIMIT 1");
        TProcessProductPrice productPrice = processProductPriceMapper.selectOne(priceQw);
        if (productPrice == null || productPrice.getPrice() == null
                || productPrice.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("该产品「" + productName + "」未配置「" + process.getProcessName()
                    + "」计件单价（0元），无法报工");
        }
        BigDecimal price = productPrice.getPrice();
        BigDecimal wage = price.multiply(BigDecimal.valueOf(qualified)).setScale(2, RoundingMode.HALF_UP);

        TWorkReport report = new TWorkReport();
        report.setWorkOrderId(dto.getWorkId());
        report.setProcessId(dto.getProcessId());
        report.setItemId(dto.getItemId());
        report.setProductName(productName);
        report.setWorkerId(dto.getWorkerId());
        report.setQualifiedNum(qualified);
        report.setBadNum(bad);
        report.setUnitPrice(price);
        report.setPieceWage(wage);
        report.setRemark(dto.getRemark());
        report.setReportTime(LocalDateTime.now());
        workReportMapper.insert(report);

        // 工序流水（防重复 + 进度展示）
        TWorkProcessRecord record = new TWorkProcessRecord();
        record.setWorkId(dto.getWorkId());
        record.setWorkNo(work.getWorkNo());
        record.setProcessId(process.getProcessId());
        record.setItemId(dto.getItemId());
        record.setProductName(productName);
        record.setProcessCode(process.getProcessCode());
        record.setOperUserId(dto.getWorkerId());
        record.setOperUsername(loadUserName(dto.getWorkerId()));
        record.setScanTime(LocalDateTime.now());
        record.setResultStatus(bad > 0 ? "FAIL" : "PASS");
        record.setRemark(dto.getRemark());
        processRecordMapper.insert(record);

        // 报工流水同步到订单备注：某员工：年月日 时:分：某工序 N件
        if (work.getSalesOrderId() != null) {
            String workerName = record.getOperUsername();
            String line = "\n" + (workerName.isEmpty() ? "工人" : workerName)
                    + "：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    + "：" + process.getProcessName()
                    + (itemNum > 0 ? "（" + productName + "）" : "")
                    + " " + qualified + "件";
            salesOrderReadMapper.appendOrderRemark(work.getSalesOrderId(), line);
        }
    }

    // ==================================================================
    // 撤回操作（仅超级管理员，防止工人误报/错报后无法修正）
    // ==================================================================

    /** 超级管理员判定：系统内置超管(id=1) 或 roles 中含"超级管理员" */
    private void requireSuperAdmin() {
        Long uid = UserContext.getUserId();
        if (uid == null) {
            throw new BusinessException("登录状态失效，请重新登录");
        }
        if (uid == 1L) {
            return;
        }
        String roles = sysUserReadMapper.selectRoles(uid);
        if (roles == null || !roles.contains("超级管理员")) {
            throw new BusinessException("仅超级管理员可执行该操作");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeReport(Long workId, Long itemId, Long processId) {
        requireSuperAdmin();
        if (workId == null || itemId == null || processId == null) {
            throw new BusinessException("工单、产品与工序不能为空");
        }
        TWorkOrder work = mustGet(workId);
        if (STATUS_FINISHED.equals(work.getWorkStatus())) {
            throw new BusinessException("工单已完工入库，请先撤回完工入库后再撤回报工");
        }
        // 该产品该工序必须已有报工记录
        LambdaQueryWrapper<TWorkReport> reportQ = new LambdaQueryWrapper<>();
        reportQ.eq(TWorkReport::getWorkOrderId, workId)
                .eq(TWorkReport::getItemId, itemId)
                .eq(TWorkReport::getProcessId, processId);
        if (workReportMapper.selectCount(reportQ) == 0) {
            throw new BusinessException("该产品该工序尚未报工，无需撤回");
        }
        // 删除报工记录与工序流水（工人工资/数量统计同步失效）
        workReportMapper.delete(reportQ);
        LambdaQueryWrapper<TWorkProcessRecord> recQ = new LambdaQueryWrapper<>();
        recQ.eq(TWorkProcessRecord::getWorkId, workId)
                .eq(TWorkProcessRecord::getItemId, itemId)
                .eq(TWorkProcessRecord::getProcessId, processId);
        processRecordMapper.delete(recQ);
        // 工单状态回退：若当前"生产中"且已无任何已完成工序，回到"待生产"
        LambdaQueryWrapper<TWorkProcessRecord> remainQ = new LambdaQueryWrapper<>();
        remainQ.eq(TWorkProcessRecord::getWorkId, workId);
        if (STATUS_PROCESSING.equals(work.getWorkStatus()) && processRecordMapper.selectCount(remainQ) == 0) {
            TWorkOrder upd = new TWorkOrder();
            upd.setWorkId(workId);
            upd.setWorkStatus(STATUS_WAIT);
            baseMapper.updateById(upd);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeFinish(Long workId) {
        requireSuperAdmin();
        TWorkOrder work = mustGet(workId);
        if (!STATUS_FINISHED.equals(work.getWorkStatus())) {
            throw new BusinessException("工单未完工，无需撤回");
        }
        // 删除该工单的成品入库单
        LambdaQueryWrapper<TProductInstock> instockQ = new LambdaQueryWrapper<>();
        instockQ.eq(TProductInstock::getWorkOrderId, workId);
        productInstockMapper.delete(instockQ);
        // 工单回"生产中"，清空完工时间
        TWorkOrder upd = new TWorkOrder();
        upd.setWorkId(workId);
        upd.setWorkStatus(STATUS_PROCESSING);
        upd.setFinishTime(null);
        baseMapper.updateById(upd);
        // 销售订单回写"生产中"（明细行同步回生产中）
        salesOrderReadMapper.updateOrderStatus(work.getSalesOrderId(), ORDER_PRODUCING);
        salesOrderReadMapper.updateItemByOrder(work.getSalesOrderId(), ITEM_PRODUCING, workId);
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
        // 校验：仅对「该产品在计件价表中配置了且单价>0」的工序要求完工。
        // 与小程序扫码报工口径一致：0元或未配置 = 该产品无此工序，不参与完工校验。
        // 否则会出现"工人看不到、报不了、系统却要求完成"的死锁，导致永远无法入库。
        List<Map<String, Object>> orderItems = salesOrderReadMapper.selectOrderItems(work.getSalesOrderId());
        if (orderItems.isEmpty()) {
            throw new BusinessException("订单无产品明细，无法完工入库");
        }
        // 本订单所有产品名
        java.util.Set<String> orderProducts = new java.util.HashSet<>();
        for (Map<String, Object> it : orderItems) {
            String pn = it.get("productName") == null ? "" : it.get("productName").toString();
            if (!pn.isEmpty()) orderProducts.add(pn);
        }
        // 产品名 -> (工序id -> 单价)
        Map<String, Map<Long, BigDecimal>> cfgByProduct = new HashMap<>();
        if (!orderProducts.isEmpty()) {
            List<TProcessProductPrice> cfgs = processProductPriceMapper.selectList(
                    new LambdaQueryWrapper<TProcessProductPrice>()
                            .in(TProcessProductPrice::getProductName, orderProducts));
            for (TProcessProductPrice pp : cfgs) {
                cfgByProduct.computeIfAbsent(pp.getProductName(), k -> new HashMap<>())
                        .put(pp.getProcessId(), pp.getPrice());
            }
        }
        // 启用工序（取工序名用于报错提示）
        List<TProcessDict> enabledProcs = processDictMapper.selectList(new LambdaQueryWrapper<TProcessDict>()
                .eq(TProcessDict::getStatus, 1).orderByAsc(TProcessDict::getProcessSort));
        Map<Long, String> procNameMap = new HashMap<>();
        for (TProcessDict pd : enabledProcs) {
            procNameMap.put(pd.getProcessId(), pd.getProcessName());
        }
        // 已完成记录：产品行id -> 已完成工序id集合
        List<TWorkProcessRecord> records = processRecordMapper.selectList(new LambdaQueryWrapper<TWorkProcessRecord>()
                .eq(TWorkProcessRecord::getWorkId, workId));
        Map<Long, Set<Long>> doneByItem = new HashMap<>();
        for (TWorkProcessRecord r : records) {
            doneByItem.computeIfAbsent(r.getItemId(), k -> new HashSet<>()).add(r.getProcessId());
        }
        // 逐产品行校验：只校验该产品 price>0 的工序
        for (Map<String, Object> it : orderItems) {
            Long itemId = it.get("itemId") == null ? null : ((Number) it.get("itemId")).longValue();
            String productName = it.get("productName") == null ? "" : it.get("productName").toString();
            if (itemId == null) continue;
            Map<Long, BigDecimal> cfg = cfgByProduct.getOrDefault(productName, Collections.emptyMap());
            Set<Long> doneProcs = doneByItem.getOrDefault(itemId, new HashSet<>());
            for (Map.Entry<Long, BigDecimal> e : cfg.entrySet()) {
                BigDecimal price = e.getValue();
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) continue; // 0元工序不校验
                Long procId = e.getKey();
                if (!doneProcs.contains(procId)) {
                    String procName = procNameMap.getOrDefault(procId, "工序#" + procId);
                    throw new BusinessException("尚有未完成：「" + productName + "」的「" + procName + "」");
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
