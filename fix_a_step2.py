# -*- coding: utf-8 -*-
"""步骤2：TSalesOrderServiceImpl 改造（编辑校验/删除/拆单标记/日志/checkWorkOrderTip）"""
import io

p = r"D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java\com\guxian\sales\service\impl\TSalesOrderServiceImpl.java"
with io.open(p, "r", encoding="utf-8") as f:
    c = f.read()

def rep(old, new, tag):
    global c
    assert old in c, "锚点未命中: " + tag
    c = c.replace(old, new)
    print(tag + ": True")

# ---- 2.1 注入 WorkOrderCheckMapper ----
rep('''    @Resource
    private TSalesOrderItemMapper orderItemMapper;
    @Resource
    private TProductMapper productMapper;''',
'''    @Resource
    private TSalesOrderItemMapper orderItemMapper;
    @Resource
    private TProductMapper productMapper;
    /** 只读访问生产工单表：编辑/删除前置校验 */
    @Resource
    private com.guxian.sales.mapper.WorkOrderCheckMapper workOrderCheckMapper;''',
"注入Mapper")

# ---- 2.2 saveOrder 编辑分支：工单存在禁止编辑 ----
rep('''        boolean isEdit = dto.getOrderId() != null;
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
        }''',
'''        boolean isEdit = dto.getOrderId() != null;
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
        }''',
"编辑工单校验")

# ---- 2.3 saveOrder 日志埋点 ----
rep('''    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(SalesOrderDTO dto) {
        // ---------- 基础校验 ----------
        if (dto.getCustomerId() == null) {
            throw new BusinessException("请选择客户");
        }
        List<SalesOrderItemDTO> inList = dto.getItemList();
        if (inList == null || inList.isEmpty()) {
            throw new BusinessException("请至少添加一条产品明细");
        }''',
'''    @Override
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
                dto.getOrderId(), isEdit, dto.getCustomerName(), inList.size());''',
"保存日志开始")

# ---- 2.4 公式行日志 ----
rep('''                        Map<String, Object> result = callFormulaCalc(
                                in.getDictType(), in.getDictValue(), params);
                        String formulaJson = objectMapper.writeValueAsString(result);
                        item.setFormulaResult(formulaJson);''',
'''                        Map<String, Object> result = callFormulaCalc(
                                in.getDictType(), in.getDictValue(), params);
                        String formulaJson = objectMapper.writeValueAsString(result);
                        item.setFormulaResult(formulaJson);
                        // 公式结果日志：便于按订单号追溯下料/剪网数据是否正确
                        log.info("公式计算成功：产品={}, 宽x高={}x{}, 数量={}, 结果={}",
                                item.getProductName(), in.getWidth(), in.getHeight(), num,
                                formulaJson.length() > 400 ? formulaJson.substring(0, 400) + "..." : formulaJson);''',
"公式日志")

# ---- 2.5 保存完成日志 ----
rep('''        // ---------- 新增订单：事务提交后自动拆单生成生产工单（工单号=订单号） ----------
        if (!isEdit) {''',
'''        log.info("订单保存完成：orderNo={}, orderId={}, 明细数={}, 总面积={}, 总金额={}, 状态={}",
                order.getOrderNo(), order.getOrderId(), entities.size(),
                order.getTotalArea(), order.getTotalAmount(), order.getOrderStatus());

        // ---------- 新增订单：事务提交后自动拆单生成生产工单（工单号=订单号） ----------
        if (!isEdit) {''',
"保存完成日志")

# ---- 2.6 autoSplitOrder：失败标记 + 完整日志 ----
rep('''    private void autoSplitOrder(Long orderId) {
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
            if (resp.statusCode() == 200 && body != null && body.contains("\\"code\\":200")) {
                log.info("下单自动拆单成功：orderId={}，已生成生产工单（工单号=订单号）", orderId);
            } else {
                log.warn("下单自动拆单未成功：orderId={}, http={}, resp={}", orderId,
                        resp.statusCode(), body == null ? "" : body.substring(0, Math.min(300, body.length())));
            }
        } catch (Exception e) {
            log.warn("下单自动拆单异常：orderId={}, msg={}", orderId, e.getMessage());
        }
    }''',
'''    private void autoSplitOrder(Long orderId) {
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
            if (resp.statusCode() == 200 && body != null && body.contains("\\"code\\":200")) {
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
    }''',
"拆单失败标记")

# ---- 2.7 新增 checkWorkOrderTip / deleteOrder ----
rep('''    /** 当前请求上下文中的 Authorization 头（供跨服务调用透传） */
    private String currentToken() {''',
'''    @Override
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
    private String currentToken() {''',
"checkWorkOrderTip/deleteOrder")

with io.open(p, "w", encoding="utf-8", newline="\n") as f:
    f.write(c)
print("ServiceImpl 全部完成")
