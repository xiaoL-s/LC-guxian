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
public class TSalesOrderServiceImpl extends ServiceImpl<TSalesOrderMapper, TSalesOrder> implements TSalesOrderService {

    /** 平方毫米 -> 平方米 换算除数 */
    private static final BigDecimal MM2_TO_M2 = new BigDecimal("1000000");
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

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

        TSalesOrder order = new TSalesOrder();
        BeanUtils.copyProperties(dto, order);

        boolean isEdit = dto.getOrderId() != null;
        if (isEdit) {
            TSalesOrder exist = baseMapper.selectById(dto.getOrderId());
            if (exist == null) {
                throw new BusinessException("订单不存在，无法编辑");
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

            // 扣宽：净宽 = 总宽 - 扣宽（不小于 0）
            BigDecimal deduct = nz(in.getDeductWidth());
            BigDecimal netWidth = in.getWidth().subtract(deduct);
            if (netWidth.signum() < 0) {
                netWidth = BigDecimal.ZERO;
            }
            item.setDeductWidth(deduct);
            item.setNetWidth(netWidth);

            // 单扇面积 = 净宽 * 高 / 1e6，保留4位
            BigDecimal singleArea = netWidth.multiply(in.getHeight())
                    .divide(MM2_TO_M2, 4, RoundingMode.HALF_UP);
            item.setSingleArea(singleArea);
            // 计费面积 = max(单扇面积, 最小起算方)
            BigDecimal chargeArea = singleArea.max(minArea);
            item.setChargeArea(chargeArea);
            // 行总面积 = 计费面积 * 数量
            BigDecimal itemTotalArea = chargeArea.multiply(BigDecimal.valueOf(num)).setScale(4, RoundingMode.HALF_UP);
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
            BigDecimal lineAmount = calcType == 2
                    ? unitPrice.multiply(BigDecimal.valueOf(num))
                    : itemTotalArea.multiply(unitPrice);
            lineAmount = lineAmount.setScale(2, RoundingMode.HALF_UP);
            item.setLineAmount(lineAmount);

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
            // 新增：生成订单号，状态未受理
            order.setOrderNo(generateOrderNo());
            order.setOrderStatus(OrderStatusEnum.PENDING_AUDIT.getCode());
            order.setReceiveAmount(BigDecimal.ZERO);
            order.setPaidAmount(BigDecimal.ZERO);
            order.setUnpaidAmount(order.getTotalAmount());
            order.setProfitAmount(profitTotal.setScale(2, RoundingMode.HALF_UP));
            order.setGrossProfitRate(rate(profitTotal, order.getTotalAmount()));
            order.setFinanceStatus(FinanceStatusEnum.UNPAID.getCode());
            baseMapper.insert(order);
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
                // 无请求上下文（如定时任务）时以空 token 调用，由生产模块鉴权提示
            }
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
     * 订单号：SO + yyyyMMdd + 3位当天序号，如 SO20260919001
     * 数据库 uk_order_no 唯一约束兜底，重试 3 次
     */
    private synchronized String generateOrderNo() {
        String prefix = "SO" + LocalDate.now().format(ORDER_NO_FMT);
        LambdaQueryWrapper<TSalesOrder> w = new LambdaQueryWrapper<>();
        w.likeRight(TSalesOrder::getOrderNo, prefix).orderByDesc(TSalesOrder::getOrderNo).last("LIMIT 1");
        TSalesOrder last = baseMapper.selectOne(w);
        int seq = 1;
        if (last != null && last.getOrderNo() != null && last.getOrderNo().length() >= prefix.length()) {
            try {
                seq = Integer.parseInt(last.getOrderNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    private SalesOrderDTO toListDTO(TSalesOrder order) {
        SalesOrderDTO dto = new SalesOrderDTO();
        BeanUtils.copyProperties(order, dto);
        dto.setOrderStatusDesc(OrderStatusEnum.descOf(order.getOrderStatus()));
        dto.setFinanceStatusDesc(FinanceStatusEnum.descOf(order.getFinanceStatus()));
        return dto;
    }
}
