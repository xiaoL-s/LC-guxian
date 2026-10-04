package com.guxian.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.context.UserContext;
import com.guxian.exception.BusinessException;
import com.guxian.sales.dto.ReceivePaymentDTO;
import com.guxian.sales.dto.SalesOrderDTO;
import com.guxian.sales.dto.SalesOrderItemDTO;
import com.guxian.sales.entity.SysDictData;
import com.guxian.sales.entity.SysDictType;
import com.guxian.sales.entity.TProduct;
import com.guxian.sales.entity.TSalesOrder;
import com.guxian.sales.entity.TSalesOrderItem;
import com.guxian.sales.enums.FinanceStatusEnum;
import com.guxian.sales.enums.OrderStatusEnum;
import com.guxian.sales.mapper.SysDictDataMapper;
import com.guxian.sales.mapper.SysDictTypeMapper;
import com.guxian.sales.mapper.TProductMapper;
import com.guxian.sales.mapper.TSalesOrderItemMapper;
import com.guxian.sales.mapper.TSalesOrderMapper;
import com.guxian.sales.service.TSalesOrderService;
import com.guxian.sales.vo.DictOptionVO;
import com.guxian.sales.vo.OrderItemQueryVO;
import com.guxian.sales.vo.SalesOrderItemRowVO;
import com.guxian.sales.vo.SalesOrderQueryVO;
import com.guxian.sales.vo.SalesOrderStatsVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@lombok.extern.slf4j.Slf4j
public class TSalesOrderServiceImpl extends ServiceImpl<TSalesOrderMapper, TSalesOrder> implements TSalesOrderService {

    /** 平方毫米 -> 平方米 换算除数 */
    private static final BigDecimal MM2_TO_M2 = new BigDecimal("1000000");
    /** 订单号前缀格式：yyyyMMdd（如 20261004），拼接 X + 4位序号 -> 20261004X0001 */
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 系统服务（公式计算）地址，可被配置覆盖 */
    @org.springframework.beans.factory.annotation.Value("${guxian.system-url:http://127.0.0.1:8081}")
    private String systemUrl;

    /** JSON 序列化器（公式结果落库） */
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    /** 产品字典类型前缀：字典管理中 style_xxx 的类型视为产品系列 */
    private static final String PRODUCT_DICT_PREFIX = "style";

    /** 其余属性字典 key（存在即作为下拉选项，不存在前端自由输入） */
    private static final List<String> ATTR_DICT_KEYS = List.of(
            "color", "net", "material", "handle", "lock", "handle_direction",
            "add_rod", "fixed_bottom", "square_board", "open_direction",
            "order_type", "install_type", "customer_source", "logistics", "unit");

    @Resource
    private TSalesOrderItemMapper orderItemMapper;
    @Resource
    private TProductMapper productMapper;
    /** 只读访问生产工单表：编辑/删除前置校验 */
    @Resource
    private com.guxian.sales.mapper.WorkOrderCheckMapper workOrderCheckMapper;
    @Resource
    private SysDictTypeMapper dictTypeMapper;
    @Resource
    private SysDictDataMapper dictDataMapper;

    // ==================================================================
    // 订单维度（订单列表 / 进度跟踪）
    // ==================================================================

    @Override
    public IPage<SalesOrderDTO> pageOrder(Page<TSalesOrder> page, SalesOrderQueryVO query) {
        LambdaQueryWrapper<TSalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getOrderNo()), TSalesOrder::getOrderNo, query.getOrderNo());
        wrapper.like(StringUtils.hasText(query.getCustomerName()), TSalesOrder::getCustomerName, query.getCustomerName());
        wrapper.eq(query.getOrderStatus() != null, TSalesOrder::getOrderStatus, query.getOrderStatus());
        wrapper.eq(query.getFinanceStatus() != null, TSalesOrder::getFinanceStatus, query.getFinanceStatus());
        wrapper.eq(StringUtils.hasText(query.getOrderType()), TSalesOrder::getOrderType, query.getOrderType());
        wrapper.eq(StringUtils.hasText(query.getSalesman()), TSalesOrder::getSalesman, query.getSalesman());
        LocalDate from = parseDate(query.getDateFrom());
        LocalDate to = parseDate(query.getDateTo());
        wrapper.ge(from != null, TSalesOrder::getOrderDate, from);
        wrapper.le(to != null, TSalesOrder::getOrderDate, to);
        if (StringUtils.hasText(query.getSubOrderNo())) {
            List<Long> ids = orderIdsBySubOrderNo(query.getSubOrderNo());
            if (ids.isEmpty()) {
                return emptyOrderPage(page);
            }
            wrapper.in(TSalesOrder::getOrderId, ids);
        }
        wrapper.orderByDesc(TSalesOrder::getOrderId);
        IPage<TSalesOrder> orderPage = baseMapper.selectPage(page, wrapper);
        return orderPage.convert(this::toListDTO);
    }

    @Override
    public SalesOrderDTO getDetail(Long orderId) {
        TSalesOrder order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        SalesOrderDTO dto = new SalesOrderDTO();
        BeanUtils.copyProperties(order, dto);
        dto.setOrderStatusDesc(OrderStatusEnum.descOf(order.getOrderStatus()));
        dto.setFinanceStatusDesc(FinanceStatusEnum.descOf(order.getFinanceStatus()));

        LambdaQueryWrapper<TSalesOrderItem> iw = new LambdaQueryWrapper<>();
        iw.eq(TSalesOrderItem::getOrderId, orderId).orderByAsc(TSalesOrderItem::getItemId);
        List<TSalesOrderItem> items = orderItemMapper.selectList(iw);
        List<SalesOrderItemDTO> itemDTOList = new ArrayList<>();
        for (TSalesOrderItem it : items) {
            SalesOrderItemDTO idto = new SalesOrderItemDTO();
            BeanUtils.copyProperties(it, idto);
            idto.setItemStatusDesc(OrderStatusEnum.descOf(it.getItemStatus()));
            itemDTOList.add(idto);
        }
        dto.setItemList(itemDTOList);
        return dto;
    }

    // ==================================================================
    // 明细/子单维度（下单、订单明细）
    // ==================================================================

    @Override
    public IPage<SalesOrderItemRowVO> pageItem(Page<TSalesOrderItem> page, OrderItemQueryVO query) {
        List<Long> orderIds = resolveOrderIds(query);
        if (orderIds != null && orderIds.isEmpty()) {
            return emptyItemPage(page);
        }
        QueryWrapper<TSalesOrderItem> wrapper = buildItemWrapper(query, orderIds);
        wrapper.orderByDesc("order_id").orderByAsc("item_id");
        IPage<TSalesOrderItem> itemPage = orderItemMapper.selectPage(page, wrapper);

        List<SalesOrderItemRowVO> rows = new ArrayList<>();
        List<TSalesOrderItem> records = itemPage.getRecords();
        if (!records.isEmpty()) {
            Map<Long, TSalesOrder> orderMap = loadOrders(records);
            for (TSalesOrderItem it : records) {
                rows.add(toRowVO(it, orderMap.get(it.getOrderId())));
            }
        }
        Page<SalesOrderItemRowVO> voPage = new Page<>(itemPage.getCurrent(), itemPage.getSize(), itemPage.getTotal());
        voPage.setRecords(rows);
        return voPage;
    }

    @Override
    public SalesOrderStatsVO stats(OrderItemQueryVO query) {
        SalesOrderStatsVO vo = new SalesOrderStatsVO();
        List<Long> orderIds = resolveOrderIds(query);
        if (orderIds != null && orderIds.isEmpty()) {
            return vo;
        }
        QueryWrapper<TSalesOrderItem> wrapper = buildItemWrapper(query, orderIds);
        wrapper.select(
                "IFNULL(SUM(num),0) AS total_num",
                "IFNULL(SUM(item_total_area),0) AS total_area",
                "IFNULL(SUM(line_amount),0) AS total_amount",
                "IFNULL(SUM(freight),0) AS total_freight",
                "COUNT(*) AS total_rows");
        List<Map<String, Object>> maps = orderItemMapper.selectMaps(wrapper);
        if (maps == null || maps.isEmpty() || maps.get(0) == null) {
            return vo;
        }
        Map<String, Object> m = maps.get(0);
        vo.setTotalNum(dec(m.get("total_num")));
        vo.setTotalArea(dec(m.get("total_area")));
        vo.setTotalAmount(dec(m.get("total_amount")));
        vo.setTotalRows(dec(m.get("total_rows")).longValue());
        // 未付/实收按订单头汇总（与明细行过滤条件一致）
        List<Long> ids = orderIds != null ? orderIds : allOrderIds();
        BigDecimal unpaid = BigDecimal.ZERO;
        BigDecimal received = BigDecimal.ZERO;
        if (!ids.isEmpty()) {
            for (TSalesOrder o : baseMapper.selectBatchIds(ids)) {
                BigDecimal total = nz(o.getTotalAmount());
                BigDecimal rec = nz(o.getReceiveAmount());
                received = received.add(rec);
                unpaid = unpaid.add(total.subtract(rec).max(BigDecimal.ZERO));
            }
        }
        vo.setTotalReceive(received.setScale(2, RoundingMode.HALF_UP));
        vo.setTotalUnpaid(unpaid.setScale(2, RoundingMode.HALF_UP));
        return vo;
    }

    // ==================================================================
    // 保存
    // ==================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(SalesOrderDTO dto) {
        // ---------- 基础校验 ----------
        if (dto.getCustomerId() == null) {
            throw new BusinessException("请选择客户");
        }
        List<SalesOrderItemDTO> inList = dto.getItemList();
        if (inList == null || inList.isEmpty()) {
            throw new BusinessException("请至少添加一条产品明细");
        }
        boolean isEdit = dto.getOrderId() != null;
        log.info("保存订单开始：orderId={}, isEdit={}, 客户={}, 明细行数={}",
                dto.getOrderId(), isEdit, dto.getCustomerName(), inList.size());

        TSalesOrder order = new TSalesOrder();
        BeanUtils.copyProperties(dto, order);

        if (isEdit) {
            TSalesOrder exist = baseMapper.selectById(dto.getOrderId());
            if (exist == null) {
                throw new BusinessException("订单不存在，无法编辑");
            }
            // 已生成生产工单的订单禁止编辑（防止销售单与工单数据不一致，改动需作废重下）
            if (workOrderCheckMapper.countBySalesOrderId(dto.getOrderId()) > 0) {
                throw new BusinessException("该订单已生成生产工单，不允许修改，请作废后重新下单");
            }
            // 仅未受理、已驳回可编辑
            if (!OrderStatusEnum.PENDING_AUDIT.getCode().equals(exist.getOrderStatus())
                    && !OrderStatusEnum.REJECTED.getCode().equals(exist.getOrderStatus())) {
                throw new BusinessException("当前订单状态不允许编辑");
            }
        }

        // ---------- 逐行重算（宽高/扣宽/面积/金额） ----------
        List<TSalesOrderItem> entities = new ArrayList<>();
        BigDecimal totalArea = BigDecimal.ZERO;
        BigDecimal productAmount = BigDecimal.ZERO;
        BigDecimal profitTotal = BigDecimal.ZERO;

        for (SalesOrderItemDTO in : inList) {
            if (!StringUtils.hasText(in.getProductName()) && in.getProductId() == null) {
                throw new BusinessException("明细存在未选择产品的行");
            }
            if (in.getWidth() == null || in.getHeight() == null
                    || in.getWidth().signum() <= 0 || in.getHeight().signum() <= 0) {
                throw new BusinessException("请正确填写产品总宽和总高");
            }
            int num = in.getNum() == null || in.getNum() <= 0 ? 1 : in.getNum();

            TSalesOrderItem item = new TSalesOrderItem();
            BeanUtils.copyProperties(in, item);

            // 产品主数据兜底（字典驱动的产品可以没有 t_product 记录）
            TProduct product = in.getProductId() == null ? null : productMapper.selectById(in.getProductId());
            if (product != null) {
                if (!StringUtils.hasText(item.getProductName())) {
                    item.setProductName(product.getProductName());
                }
                if (!StringUtils.hasText(item.getProductType())) {
                    item.setProductType(product.getProductType());
                }
            }
            BigDecimal unitPrice = nz(in.getUnitPrice());
            if (unitPrice.signum() <= 0 && product != null) {
                unitPrice = nz(product.getUnitPrice());
            }
            item.setUnitPrice(unitPrice);

            // 品目/产品类型兜底：字典驱动时以"品目"作为产品类型
            if (!StringUtils.hasText(item.getProductType())) {
                item.setProductType(StringUtils.hasText(item.getItemCategory())
                        ? item.getItemCategory() : "未分类");
            }

            BigDecimal minArea = in.getMinArea() == null
                    ? (product == null ? BigDecimal.ZERO : nz(product.getMinArea()))
                    : in.getMinArea();
            item.setMinArea(minArea);
            item.setNum(num);

            // ================= 产品公式计算（下料/剪网/面积/金额） =================
            // 产品来自字典且该字典项配置了公式时：以公式引擎计算结果为准，
            // 型材=下料尺寸、纱网=剪网尺寸、面积/金额由公式输出，结果存 formula_result 供生产打印使用
            boolean hasFormula = false;
            if (StringUtils.hasText(in.getDictType()) && StringUtils.hasText(in.getDictValue())) {
                SysDictData dict = dictDataMapper.selectOne(new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, in.getDictType())
                        .eq(SysDictData::getDictValue, in.getDictValue())
                        .last("LIMIT 1"));
                if (dict != null && StringUtils.hasText(dict.getFormulaConfig())) {
                    try {
                        Map<String, Object> params = new LinkedHashMap<>();
                        params.put("总宽", in.getWidth());
                        params.put("总高", in.getHeight());
                        params.put("数量", num);
                        putIfText(params, "颜色", in.getColor());
                        putIfText(params, "网子", in.getNetMaterial());
                        putIfText(params, "把手", in.getHandle());
                        putIfText(params, "把手方向", in.getHandleDirection());
                        putIfText(params, "加杆", in.getAddRod());
                        // 下固定：优先转数字（公式按“下固定>0/下固定=0”判断），转不了按 0 处理
                        BigDecimal fbNum = parseNum(in.getFixedBottom());
                        params.put("下固定", fbNum != null ? fbNum : BigDecimal.ZERO);
                        params.put("单价", unitPrice);

                        Map<String, Object> result = callFormulaCalc(
                                in.getDictType(), in.getDictValue(), params);
                        String formulaJson = objectMapper.writeValueAsString(result);
                        item.setFormulaResult(formulaJson);
                        // 公式结果日志：便于按订单号追溯下料/剪网数据是否正确
                        log.info("公式计算成功：产品={}, 宽x高={}x{}, 数量={}, 结果={}",
                                item.getProductName(), in.getWidth(), in.getHeight(), num,
                                formulaJson.length() > 400 ? formulaJson.substring(0, 400) + "..." : formulaJson);

                        // 公式输出的“面积”为整行总面积（已含数量）
                        BigDecimal formulaArea = dec(result.get("面积"));
                        if (formulaArea.signum() > 0) {
                            BigDecimal perArea = formulaArea.divide(BigDecimal.valueOf(num), 4, RoundingMode.HALF_UP);
                            item.setSingleArea(perArea);
                            item.setChargeArea(perArea);
                            item.setItemTotalArea(formulaArea.setScale(4, RoundingMode.HALF_UP));
                        }
                        // 公式输出的“总金额”为整行金额（已含数量与加价）
                        BigDecimal formulaTotal = dec(result.get("总金额"));
                        if (formulaTotal.signum() > 0) {
                            item.setLineAmount(formulaTotal.setScale(2, RoundingMode.HALF_UP));
                            item.setSalePriceType(3); // 按公式计价
                        }
                        hasFormula = true;
                    } catch (BusinessException e) {
                        // 公式计算是下料/剪网/金额的唯一来源，失败则直接阻断，避免生成无尺寸订单
                        throw e;
                    } catch (Exception e) {
                        throw new BusinessException("产品【" + item.getProductName() + "】公式计算失败：" + e.getMessage());
                    }
                }
            }

            // 扣宽：净宽 = 总宽 - 扣宽（不小于 0）
            BigDecimal deduct = nz(in.getDeductWidth());
            BigDecimal netWidth = in.getWidth().subtract(deduct);
            if (netWidth.signum() < 0) {
                netWidth = BigDecimal.ZERO;
            }
            item.setDeductWidth(deduct);
            item.setNetWidth(netWidth);

            // 行总面积 / 行金额：公式或无公式分支都会赋值，供下方汇总
            BigDecimal itemTotalArea = BigDecimal.ZERO;
            BigDecimal lineAmount = BigDecimal.ZERO;

            if (!hasFormula) {
                // ============ 无公式产品：走面积/件数通用计价 ============
                // 单扇面积 = 净宽 * 高 / 1e6，保留4位
                BigDecimal singleArea = netWidth.multiply(in.getHeight())
                        .divide(MM2_TO_M2, 4, RoundingMode.HALF_UP);
                item.setSingleArea(singleArea);
                // 计费面积 = max(单扇面积, 最小起算方)
                BigDecimal chargeArea = singleArea.max(minArea);
                item.setChargeArea(chargeArea);
                // 行总面积 = 计费面积 * 数量
                itemTotalArea = chargeArea.multiply(BigDecimal.valueOf(num)).setScale(4, RoundingMode.HALF_UP);
                item.setItemTotalArea(itemTotalArea);

                // 计算方式：1按面积 2按件（默认按面积；产品主数据可覆盖）
                Integer calcType = in.getCalcType();
                if (calcType == null && product != null && product.getPriceType() != null) {
                    calcType = product.getPriceType();
                }
                if (calcType == null) {
                    calcType = 1;
                }
                item.setCalcType(calcType);
                if (item.getSalePriceType() == null) {
                    item.setSalePriceType(calcType);
                }
                lineAmount = calcType == 2
                        ? unitPrice.multiply(BigDecimal.valueOf(num))
                        : itemTotalArea.multiply(unitPrice);
                lineAmount = lineAmount.setScale(2, RoundingMode.HALF_UP);
                item.setLineAmount(lineAmount);
            } else {
                // 公式分支：金额/面积已在上面赋给 item，同步到汇总局部变量
                itemTotalArea = nz(item.getItemTotalArea());
                lineAmount = nz(item.getLineAmount());
            }

            item.setFreight(nz(item.getFreight()));
            item.setReceiveAmount(nz(item.getReceiveAmount()));
            item.setProfitAmount(nz(item.getProfitAmount()));
            item.setProduceProgress(item.getProduceProgress() == null ? 0 : item.getProduceProgress());
            if (item.getItemStatus() == null) {
                item.setItemStatus(OrderStatusEnum.PENDING_AUDIT.getCode());
            }

            totalArea = totalArea.add(itemTotalArea);
            productAmount = productAmount.add(lineAmount);
            profitTotal = profitTotal.add(item.getProfitAmount());
            entities.add(item);
        }

        // ---------- 汇总费用 ----------
        BigDecimal craftFee = nz(dto.getCraftFee());
        BigDecimal urgentFee = nz(dto.getUrgentFee());
        BigDecimal freight = nz(dto.getFreight());
        BigDecimal discount = nz(dto.getDiscountAmount());
        BigDecimal totalAmount = productAmount.add(craftFee).add(urgentFee).add(freight).subtract(discount);
        if (totalAmount.signum() < 0) {
            throw new BusinessException("优惠金额不能大于订单应收金额");
        }

        order.setTotalArea(totalArea.setScale(4, RoundingMode.HALF_UP));
        order.setProductAmount(productAmount.setScale(2, RoundingMode.HALF_UP));
        order.setCraftFee(craftFee);
        order.setUrgentFee(urgentFee);
        order.setFreight(freight);
        order.setDiscountAmount(discount);
        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        order.setSubOrderCount(entities.size());
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }
        if (!StringUtils.hasText(order.getOrderType())) {
            order.setOrderType("正常单");
        }

        if (!isEdit) {
            // 新增：生成订单号，状态=已受理（保存后自动拆单生成生产工单）
            order.setOrderNo(generateOrderNo());
            order.setOrderStatus(OrderStatusEnum.AUDITED.getCode());
            order.setReceiveAmount(BigDecimal.ZERO);
            order.setPaidAmount(BigDecimal.ZERO);
            order.setUnpaidAmount(order.getTotalAmount());
            order.setProfitAmount(profitTotal.setScale(2, RoundingMode.HALF_UP));
            order.setGrossProfitRate(rate(profitTotal, order.getTotalAmount()));
            order.setFinanceStatus(FinanceStatusEnum.UNPAID.getCode());
            baseMapper.insert(order);
            // 回写新订单ID，供调用方（Controller）查询拆单结果提示
            dto.setOrderId(order.getOrderId());
        } else {
            // 编辑：回到未受理，清空上次审核信息；保留已收款
            TSalesOrder old = baseMapper.selectById(dto.getOrderId());
            BigDecimal received = nz(old.getReceiveAmount());
            BigDecimal unpaid = order.getTotalAmount().subtract(received).max(BigDecimal.ZERO);
            order.setOrderId(dto.getOrderId());
            order.setOrderStatus(OrderStatusEnum.PENDING_AUDIT.getCode());
            order.setAuditBy(null);
            order.setAuditTime(null);
            order.setRejectReason(null);
            order.setReceiveAmount(received);
            order.setPaidAmount(received);
            order.setUnpaidAmount(unpaid);
            order.setProfitAmount(profitTotal.setScale(2, RoundingMode.HALF_UP));
            order.setGrossProfitRate(rate(profitTotal, order.getTotalAmount()));
            order.setFinanceStatus(FinanceStatusEnum.derive(order.getTotalAmount(), received));
            baseMapper.updateById(order);
            // 删除旧明细
            LambdaQueryWrapper<TSalesOrderItem> dw = new LambdaQueryWrapper<>();
            dw.eq(TSalesOrderItem::getOrderId, dto.getOrderId());
            orderItemMapper.delete(dw);
        }

        // ---------- 落明细，生成子单号 ----------
        int seq = 1;
        for (TSalesOrderItem item : entities) {
            item.setOrderId(order.getOrderId());
            item.setItemId(null);
            item.setSubOrderNo(buildSubOrderNo(order.getOrderNo(), seq++));
            orderItemMapper.insert(item);
        }

        log.info("订单保存完成：orderNo={}, orderId={}, 明细数={}, 总面积={}, 总金额={}, 状态={}",
                order.getOrderNo(), order.getOrderId(), entities.size(),
                order.getTotalArea(), order.getTotalAmount(), order.getOrderStatus());

        // ---------- 新增订单：事务提交后自动拆单生成生产工单（工单号=订单号） ----------
        if (!isEdit) {
            // 明细行状态与订单同步为已受理，保证拆单/生产侧数据一致
            updateItemStatus(order.getOrderId(), OrderStatusEnum.AUDITED.getCode(), null);
            final Long savedOrderId = order.getOrderId();
            // 拆单由生产模块(8085)读同一数据库完成；必须在本地事务提交后再触发，
            // 否则生产模块查不到尚未提交的订单/明细数据，导致拆单失败
            org.springframework.transaction.support.TransactionSynchronizationManager
                    .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            autoSplitOrder(savedOrderId);
                        }
                    });
        }
    }

    /**
     * 下单保存后自动调用生产模块拆单生成工单（工单号=订单编号）。
     * 拆单失败（如生产服务未启动、货架未配置）不阻断下单：
     * 订单保持"已受理"，可稍后在生产工单-拆单或订单列表"开工"手动补齐。
     */
    private void autoSplitOrder(Long orderId) {
        String token = currentToken();
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:8085/production/workorder/split?salesOrderId=" + orderId))
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.noBody());
            if (StringUtils.hasText(token)) {
                rb.header("Authorization", token);
            }
            HttpResponse<String> resp = client.send(rb.build(), HttpResponse.BodyHandlers.ofString());
            String body = resp.body();
            if (resp.statusCode() == 200 && body != null && body.contains("\"code\":200")) {
                String workNo = workOrderCheckMapper.latestWorkNo(orderId);
                log.info("下单自动拆单成功：orderId={}，工单号={}（工单号=订单号）", orderId,
                        workNo == null ? "" : workNo);
            } else {
                // 拆单失败：订单保持已受理，标记备注并打 error 日志，前端保存响应会提示手动开工补齐
                String reason = body == null ? "响应为空" : body.substring(0, Math.min(200, body.length()));
                log.error("下单自动拆单未成功：orderId={}, http={}, 响应={}", orderId, resp.statusCode(), reason);
                markSplitFailed(orderId, "生产模块拆单未成功(HTTP " + resp.statusCode() + ")，请手动开工补齐");
            }
        } catch (Exception e) {
            log.error("下单自动拆单异常：orderId={}", orderId, e);
            markSplitFailed(orderId, "自动拆单异常：" + e.getMessage());
        }
    }

    /** 拆单失败：在订单备注上追加标记，便于列表/详情中一眼识别需要手动处理 */
    private void markSplitFailed(Long orderId, String reason) {
        try {
            TSalesOrder o = baseMapper.selectById(orderId);
            if (o == null) {
                return;
            }
            String mark = "【自动拆单失败】" + reason;
            String oldRemark = o.getRemark();
            if (oldRemark != null && oldRemark.contains("【自动拆单失败】")) {
                return; // 已标记过，不重复追加
            }
            TSalesOrder upd = new TSalesOrder();
            upd.setOrderId(orderId);
            upd.setRemark(oldRemark == null || oldRemark.isBlank() ? mark : oldRemark + "；" + mark);
            baseMapper.updateById(upd);
        } catch (Exception ex) {
            log.warn("拆单失败标记写入异常：orderId={}, msg={}", orderId, ex.getMessage());
        }
    }

    @Override
    public String checkWorkOrderTip(Long orderId) {
        if (orderId == null) {
            return "订单已保存；工单生成情况请到生产工单中确认";
        }
        String workNo = workOrderCheckMapper.latestWorkNo(orderId);
        if (StringUtils.hasText(workNo)) {
            return "订单已保存，已自动生成生产工单（工单号=" + workNo + "）";
        }
        return "订单已保存；生产工单未生成成功（生产服务可能未启动），请到订单列表手动开工补齐";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId) {
        TSalesOrder order = mustGet(orderId);
        // 已生成生产工单的订单不允许删除（防止工单成为孤儿数据）
        if (workOrderCheckMapper.countBySalesOrderId(orderId) > 0) {
            throw new BusinessException("该订单已生成生产工单，不允许删除，请先在生产管理中处理");
        }
        // 已进入业务流程的订单不允许删除（受理1 ~ 已完成5 均含业务流转）
        if (order.getOrderStatus() != null
                && order.getOrderStatus() >= 1 && order.getOrderStatus() <= 5) {
            throw new BusinessException("订单已进入业务流程，不允许删除，可取消");
        }
        // 先删明细（物理删除），再逻辑删除订单头，避免明细成为孤儿数据
        LambdaQueryWrapper<TSalesOrderItem> dw = new LambdaQueryWrapper<>();
        dw.eq(TSalesOrderItem::getOrderId, orderId);
        orderItemMapper.delete(dw);
        baseMapper.deleteById(orderId);
        log.info("删除订单：orderId={}, orderNo={}（含其 {} 条明细）", orderId, order.getOrderNo(), 0);
    }

    /** 当前请求上下文中的 Authorization 头（供跨服务调用透传） */
    private String currentToken() {
        try {
            var attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                jakarta.servlet.http.HttpServletRequest cur =
                        ((org.springframework.web.context.request.ServletRequestAttributes) attrs).getRequest();
                String auth = cur.getHeader("Authorization");
                if (auth != null && !auth.isBlank()) {
                    return auth;
                }
            }
        } catch (Exception ignore) {
            // 无请求上下文时返回空串，由生产模块鉴权提示
        }
        return "";
    }

    // ==================================================================
    // 状态流转 & 收款
    // ==================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long orderId, boolean pass, String rejectReason) {
        TSalesOrder order = mustGet(orderId);
        if (!OrderStatusEnum.PENDING_AUDIT.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("仅未受理订单可受理/驳回");
        }
        TSalesOrder upd = new TSalesOrder();
        upd.setOrderId(orderId);
        upd.setAuditBy(UserContext.getUserId());
        upd.setAuditTime(LocalDateTime.now());
        if (pass) {
            upd.setOrderStatus(OrderStatusEnum.AUDITED.getCode());
            upd.setRejectReason(null);
            baseMapper.updateById(upd);
            updateItemStatus(orderId, OrderStatusEnum.AUDITED.getCode(), null);
        } else {
            if (!StringUtils.hasText(rejectReason)) {
                throw new BusinessException("驳回必须填写驳回原因");
            }
            upd.setOrderStatus(OrderStatusEnum.REJECTED.getCode());
            upd.setRejectReason(rejectReason);
            baseMapper.updateById(upd);
            updateItemStatus(orderId, OrderStatusEnum.PENDING_AUDIT.getCode(), null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receivePayment(Long orderId, ReceivePaymentDTO dto) {
        TSalesOrder order = mustGet(orderId);
        if (dto == null || dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new BusinessException("收款金额必须大于 0");
        }
        if (OrderStatusEnum.CANCELED.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("已取消订单不允许登记收款");
        }
        BigDecimal total = nz(order.getTotalAmount());
        BigDecimal received = nz(order.getReceiveAmount()).add(dto.getAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unpaid = total.subtract(received);
        if (unpaid.signum() < 0) {
            unpaid = BigDecimal.ZERO;
        }
        TSalesOrder upd = new TSalesOrder();
        upd.setOrderId(orderId);
        upd.setReceiveAmount(received);
        upd.setPaidAmount(received);
        upd.setUnpaidAmount(unpaid);
        upd.setFinanceStatus(FinanceStatusEnum.derive(total, received));
        upd.setReceiveTime(dto.getReceiveTime() == null ? LocalDateTime.now() : dto.getReceiveTime());
        baseMapper.updateById(upd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settle(Long orderId) {
        mustGet(orderId);
        TSalesOrder upd = new TSalesOrder();
        upd.setOrderId(orderId);
        upd.setFinanceStatus(FinanceStatusEnum.SETTLED.getCode());
        baseMapper.updateById(upd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startProduce(Long orderId) {
        TSalesOrder order = mustGet(orderId);
        if (!OrderStatusEnum.AUDITED.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("仅已受理订单可开工");
        }
        // 真正的开工 = 调用生产模块拆单，生成生产工单（拆单成功会自动回写订单状态为生产中）
        try {
            // 透传当前登录用户的 token，生产模块拆单接口需要鉴权
            String token = currentToken();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:8085/production/workorder/split?salesOrderId=" + orderId))
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.noBody());
            if (!token.isEmpty()) {
                rb.header("Authorization", token);
            }
            HttpResponse<String> resp = client.send(rb.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new BusinessException("生产服务拆单失败(HTTP " + resp.statusCode() + ")，请查看生产模块日志");
            }
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp.body());
            int code = node.path("code").asInt(-1);
            String msg = node.path("msg").asText("拆单失败");
            if (code != 200) {
                throw new BusinessException("开工失败：" + msg);
            }
        } catch (java.net.ConnectException e) {
            throw new BusinessException("生产服务(8085)未启动，请先启动 guxian-produce 模块");
        } catch (java.io.IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("调用生产服务拆单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishProduce(Long orderId) {
        TSalesOrder order = mustGet(orderId);
        if (!OrderStatusEnum.PRODUCING.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("仅生产中订单可标记完工");
        }
        TSalesOrder upd = new TSalesOrder();
        upd.setOrderId(orderId);
        upd.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
        baseMapper.updateById(upd);
        updateItemStatus(orderId, OrderStatusEnum.FINISHED.getCode(), 100);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliver(Long orderId) {
        TSalesOrder order = mustGet(orderId);
        if (!OrderStatusEnum.FINISHED.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("仅已完工订单可发货");
        }
        TSalesOrder upd = new TSalesOrder();
        upd.setOrderId(orderId);
        upd.setOrderStatus(OrderStatusEnum.DELIVERED.getCode());
        upd.setDeliveryTime(LocalDateTime.now());
        baseMapper.updateById(upd);
        updateItemStatus(orderId, OrderStatusEnum.DELIVERED.getCode(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long orderId) {
        TSalesOrder order = mustGet(orderId);
        if (!OrderStatusEnum.DELIVERED.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("仅已发货订单可完成");
        }
        TSalesOrder upd = new TSalesOrder();
        upd.setOrderId(orderId);
        upd.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        upd.setFinishTime(LocalDateTime.now());
        baseMapper.updateById(upd);
        updateItemStatus(orderId, OrderStatusEnum.COMPLETED.getCode(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId, String cancelReason) {
        TSalesOrder order = mustGet(orderId);
        if (!OrderStatusEnum.PENDING_AUDIT.getCode().equals(order.getOrderStatus())
                && !OrderStatusEnum.REJECTED.getCode().equals(order.getOrderStatus())) {
            throw new BusinessException("仅未受理或已驳回订单可取消");
        }
        TSalesOrder upd = new TSalesOrder();
        upd.setOrderId(orderId);
        upd.setOrderStatus(OrderStatusEnum.CANCELED.getCode());
        upd.setCancelReason(cancelReason);
        baseMapper.updateById(upd);
        updateItemStatus(orderId, OrderStatusEnum.ITEM_CANCELED.getCode(), null);
    }

    // ==================================================================
    // 字典选项
    // ==================================================================

    @Override
    public Map<String, List<DictOptionVO>> dictOptions() {
        Map<String, List<DictOptionVO>> result = new LinkedHashMap<>();
        List<SysDictType> types = dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                .and(w -> w.likeRight(SysDictType::getDictType, PRODUCT_DICT_PREFIX)
                        .or().in(SysDictType::getDictType, ATTR_DICT_KEYS))
                .and(w -> w.isNull(SysDictType::getStatus).or().eq(SysDictType::getStatus, 1)));
        for (String key : ATTR_DICT_KEYS) {
            result.put(key, new ArrayList<>());
        }
        result.put("product", new ArrayList<>());
        if (types.isEmpty()) {
            return result;
        }
        Map<String, String> typeNameMap = types.stream()
                .filter(t -> StringUtils.hasText(t.getDictType()))
                .collect(Collectors.toMap(SysDictType::getDictType,
                        t -> t.getDictName() == null ? "" : t.getDictName(), (a, b) -> a));
        List<String> typeCodes = new ArrayList<>(typeNameMap.keySet());
        List<SysDictData> dataList = dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .in(SysDictData::getDictType, typeCodes)
                .and(w -> w.isNull(SysDictData::getStatus).or().eq(SysDictData::getStatus, 1))
                .orderByAsc(SysDictData::getDictType)
                .orderByAsc(SysDictData::getSort));
        for (SysDictData d : dataList) {
            String type = d.getDictType();
            DictOptionVO vo = new DictOptionVO(type, typeNameMap.get(type),
                    d.getDictValue(), d.getDictLabel(), d.getSort());
            if (type != null && type.startsWith(PRODUCT_DICT_PREFIX)) {
                vo.setFormulaConfig(d.getFormulaConfig());
                result.get("product").add(vo);
            } else if (result.containsKey(type)) {
                result.get(type).add(vo);
            }
        }
        return result;
    }

    // ==================================================================
    // 私有方法
    // ==================================================================

    /** 按明细行条件反查订单ID；无订单级条件时返回 null 表示不限制 */
    private List<Long> resolveOrderIds(OrderItemQueryVO q) {
        boolean hasOrderFilter = StringUtils.hasText(q.getOrderNo())
                || StringUtils.hasText(q.getCustomerName())
                || q.getOrderStatus() != null
                || q.getFinanceStatus() != null
                || StringUtils.hasText(q.getOrderType())
                || StringUtils.hasText(q.getSalesman())
                || StringUtils.hasText(q.getDateFrom())
                || StringUtils.hasText(q.getDateTo())
                || StringUtils.hasText(q.getKeyword());
        if (!hasOrderFilter) {
            return null;
        }
        LambdaQueryWrapper<TSalesOrder> w = new LambdaQueryWrapper<>();
        w.like(StringUtils.hasText(q.getOrderNo()), TSalesOrder::getOrderNo, q.getOrderNo());
        w.like(StringUtils.hasText(q.getCustomerName()), TSalesOrder::getCustomerName, q.getCustomerName());
        w.eq(q.getOrderStatus() != null, TSalesOrder::getOrderStatus, q.getOrderStatus());
        w.eq(q.getFinanceStatus() != null, TSalesOrder::getFinanceStatus, q.getFinanceStatus());
        w.eq(StringUtils.hasText(q.getOrderType()), TSalesOrder::getOrderType, q.getOrderType());
        w.eq(StringUtils.hasText(q.getSalesman()), TSalesOrder::getSalesman, q.getSalesman());
        LocalDate from = parseDate(q.getDateFrom());
        LocalDate to = parseDate(q.getDateTo());
        w.ge(from != null, TSalesOrder::getOrderDate, from);
        w.le(to != null, TSalesOrder::getOrderDate, to);
        if (StringUtils.hasText(q.getKeyword())) {
            String k = q.getKeyword();
            w.and(x -> x.like(TSalesOrder::getOrderNo, k).or().like(TSalesOrder::getCustomerName, k));
        }
        w.select(TSalesOrder::getOrderId);
        return baseMapper.selectList(w).stream().map(TSalesOrder::getOrderId).collect(Collectors.toList());
    }

    private List<Long> orderIdsBySubOrderNo(String subOrderNo) {
        LambdaQueryWrapper<TSalesOrderItem> w = new LambdaQueryWrapper<>();
        w.like(TSalesOrderItem::getSubOrderNo, subOrderNo);
        w.select(TSalesOrderItem::getOrderId);
        return orderItemMapper.selectList(w).stream()
                .map(TSalesOrderItem::getOrderId).distinct().collect(Collectors.toList());
    }

    private List<Long> allOrderIds() {
        LambdaQueryWrapper<TSalesOrder> w = new LambdaQueryWrapper<>();
        w.select(TSalesOrder::getOrderId);
        return baseMapper.selectList(w).stream().map(TSalesOrder::getOrderId).collect(Collectors.toList());
    }

    /** 明细查询条件（分页与统计共用） */
    private QueryWrapper<TSalesOrderItem> buildItemWrapper(OrderItemQueryVO q, List<Long> orderIds) {
        QueryWrapper<TSalesOrderItem> w = new QueryWrapper<>();
        if (orderIds != null) {
            w.in("order_id", orderIds);
        }
        w.like(StringUtils.hasText(q.getProductName()), "product_name", q.getProductName());
        w.eq(StringUtils.hasText(q.getItemCategory()), "item_category", q.getItemCategory());
        w.like(StringUtils.hasText(q.getSubOrderNo()), "sub_order_no", q.getSubOrderNo());
        w.eq(StringUtils.hasText(q.getSalesOwner()), "sales_owner", q.getSalesOwner());
        w.eq(q.getItemStatus() != null, "item_status", q.getItemStatus());
        if (StringUtils.hasText(q.getKeyword())) {
            String k = q.getKeyword();
            w.and(x -> x.like("product_name", k).or().like("sub_order_no", k));
        }
        return w;
    }

    private Map<Long, TSalesOrder> loadOrders(List<TSalesOrderItem> records) {
        List<Long> ids = records.stream().map(TSalesOrderItem::getOrderId)
                .filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return baseMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(TSalesOrder::getOrderId, Function.identity(), (a, b) -> a));
    }

    private SalesOrderItemRowVO toRowVO(TSalesOrderItem it, TSalesOrder order) {
        SalesOrderItemRowVO vo = new SalesOrderItemRowVO();
        BeanUtils.copyProperties(it, vo);
        vo.setItemStatusDesc(OrderStatusEnum.descOf(it.getItemStatus()));
        BigDecimal lineAmount = nz(it.getLineAmount());
        vo.setGrossProfitRate(rate(nz(it.getProfitAmount()), lineAmount));
        if (order != null) {
            vo.setOrderId(order.getOrderId());
            vo.setOrderNo(order.getOrderNo());
            vo.setOrderType(order.getOrderType());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setOrderStatusDesc(OrderStatusEnum.descOf(order.getOrderStatus()));
            vo.setOrderStatusTag(OrderStatusEnum.tagOf(order.getOrderStatus()));
            vo.setFinanceStatus(order.getFinanceStatus());
            vo.setFinanceStatusDesc(FinanceStatusEnum.descOf(order.getFinanceStatus()));
            vo.setFinanceStatusTag(FinanceStatusEnum.tagOf(order.getFinanceStatus()));
            vo.setCustomerId(order.getCustomerId());
            vo.setCustomerName(order.getCustomerName());
            vo.setContact(order.getContact());
            vo.setPhone(order.getPhone());
            vo.setTerminalAddress(order.getTerminalAddress());
            vo.setLogistics(order.getLogistics());
            vo.setUnit(it.getUnit() != null ? it.getUnit() : order.getUnit());
            vo.setBrand(order.getBrand());
            vo.setInstallType(order.getInstallType());
            vo.setDesigner(order.getDesigner());
            vo.setSplitter(order.getSplitter());
            vo.setSalesman(order.getSalesman());
            vo.setCustomerSource(order.getCustomerSource());
            vo.setOrderDate(order.getOrderDate());
            vo.setExpectDate(order.getExpectDate());
            vo.setOrderCreateTime(order.getCreateTime());
            vo.setTotalAmount(nz(order.getTotalAmount()));
            vo.setUnpaidAmount(nz(order.getUnpaidAmount()));
            vo.setOrderFreight(nz(order.getFreight()));
            // 行实收：优先取明细登记值，否则按订单实收按金额占比分摊
            BigDecimal rowReceive = nz(it.getReceiveAmount());
            if (rowReceive.signum() <= 0 && nz(order.getTotalAmount()).signum() > 0
                    && nz(order.getReceiveAmount()).signum() > 0) {
                rowReceive = nz(order.getReceiveAmount())
                        .multiply(lineAmount)
                        .divide(nz(order.getTotalAmount()), 2, RoundingMode.HALF_UP);
            }
            vo.setReceiveAmount(rowReceive);
        } else {
            vo.setReceiveAmount(nz(it.getReceiveAmount()));
            vo.setUnit(it.getUnit());
        }
        return vo;
    }

    private void updateItemStatus(Long orderId, Integer itemStatus, Integer progress) {
        TSalesOrderItem upd = new TSalesOrderItem();
        upd.setItemStatus(itemStatus);
        if (progress != null) {
            upd.setProduceProgress(progress);
        }
        LambdaQueryWrapper<TSalesOrderItem> w = new LambdaQueryWrapper<>();
        w.eq(TSalesOrderItem::getOrderId, orderId);
        orderItemMapper.update(upd, w);
    }

    private TSalesOrder mustGet(Long orderId) {
        TSalesOrder order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private IPage<SalesOrderDTO> emptyOrderPage(Page<TSalesOrder> page) {
        Page<SalesOrderDTO> p = new Page<>(page.getCurrent(), page.getSize(), 0);
        p.setRecords(new ArrayList<>());
        return p;
    }

    private IPage<SalesOrderItemRowVO> emptyItemPage(Page<TSalesOrderItem> page) {
        Page<SalesOrderItemRowVO> p = new Page<>(page.getCurrent(), page.getSize(), 0);
        p.setRecords(new ArrayList<>());
        return p;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal dec(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(v.toString());
    }

    private BigDecimal rate(BigDecimal profit, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return profit.divide(amount, 4, RoundingMode.HALF_UP);
    }

    private LocalDate parseDate(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String buildSubOrderNo(String orderNo, int seq) {
        return orderNo + "-" + String.format("%02d", seq);
    }

    /**
     * 订单号：yyyyMMdd + X + 4位序号，如 20261004X0001
     * 序号按“年+月+日”前缀自增，跨日自动从 0001 重新开始
     * 数据库 uk_order_no 唯一约束兜底
     */
    private synchronized String generateOrderNo() {
        String prefix = LocalDate.now().format(ORDER_NO_FMT) + "X";
        LambdaQueryWrapper<TSalesOrder> w = new LambdaQueryWrapper<>();
        w.likeRight(TSalesOrder::getOrderNo, prefix).orderByDesc(TSalesOrder::getOrderNo).last("LIMIT 1");
        TSalesOrder last = baseMapper.selectOne(w);
        int seq = 1;
        if (last != null && last.getOrderNo() != null && last.getOrderNo().length() >= prefix.length() + 4) {
            try {
                seq = Integer.parseInt(last.getOrderNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    /**
     * 调用系统服务公式计算接口（下料/剪网/面积/金额）
     * 透传当前登录用户 token，保证生产链路上的公式数据口径一致
     */
    private Map<String, Object> callFormulaCalc(String dictType, String dictValue, Map<String, Object> params) {
        try {
            String token = "";
            try {
                var attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    jakarta.servlet.http.HttpServletRequest cur =
                            ((org.springframework.web.context.request.ServletRequestAttributes) attrs).getRequest();
                    String auth = cur.getHeader("Authorization");
                    if (auth != null && !auth.isBlank()) {
                        token = auth;
                    }
                }
            } catch (Exception ignore) {
                // 无请求上下文时以空 token 调用，由系统服务鉴权提示
            }
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(Map.of("dictType", dictType, "dictValue", dictValue, "params", params)),
                    java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create(systemUrl + "/system/dict/data/formula/test"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(body);
            if (!token.isEmpty()) {
                rb.header("Authorization", token);
            }
            HttpResponse<String> resp = client.send(rb.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new BusinessException("系统服务公式计算异常(HTTP " + resp.statusCode() + ")");
            }
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(resp.body());
            int code = node.path("code").asInt(-1);
            if (code != 200) {
                throw new BusinessException("系统服务公式计算失败：" + node.path("msg").asText("未知错误"));
            }
            com.fasterxml.jackson.databind.JsonNode data = node.path("data");
            if (data.isMissingNode() || !data.isObject()) {
                throw new BusinessException("系统服务公式计算无返回数据");
            }
            return objectMapper.convertValue(data, Map.class);
        } catch (java.net.ConnectException e) {
            throw new BusinessException("系统服务(8081)未启动，无法计算公式，请先启动 guxian-system 模块");
        } catch (java.io.IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("调用系统服务公式计算失败：" + e.getMessage());
        }
    }

    /** 参数非空才放入（空串/空白不参与公式条件判断） */
    private void putIfText(Map<String, Object> map, String key, String val) {
        if (StringUtils.hasText(val)) {
            map.put(key, val.trim());
        }
    }

    /** 字符串转数字（mm 类属性），转不了返回 null */
    private BigDecimal parseNum(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private SalesOrderDTO toListDTO(TSalesOrder order) {
        SalesOrderDTO dto = new SalesOrderDTO();
        BeanUtils.copyProperties(order, dto);
        dto.setOrderStatusDesc(OrderStatusEnum.descOf(order.getOrderStatus()));
        dto.setFinanceStatusDesc(FinanceStatusEnum.descOf(order.getFinanceStatus()));
        return dto;
    }
}
