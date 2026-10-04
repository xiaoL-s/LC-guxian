# -*- coding: utf-8 -*-
"""需求：下单保存后自动生成生产工单；工单号统一=订单编号。
1) produce splitOrderToWork: workNo 取订单号（orderNo）
2) sales saveOrder 新增: 订单状态=已受理(1) + 自动调用拆单生成工单（失败不阻断下单）
"""
import io

# ========== 1) produce：工单号=订单号 ==========
p1 = r"D:\Java\guxian\guxian-erp-parent\guxian-produce\src\main\java\com\guxian\produce\service\impl\TWorkOrderServiceImpl.java"
with io.open(p1, "r", encoding="utf-8") as f:
    c1 = f.read()

old1 = '''        // 创建工单
        TWorkOrder work = new TWorkOrder();
        work.setWorkNo(generateWorkNo());'''
new1 = '''        // 创建工单：工单号统一使用销售订单编号（下单保存即自动拆单，便于按订单追溯生产）
        Object orderNoObj = header.get("orderNo");
        String orderNo = orderNoObj == null ? null : orderNoObj.toString();
        TWorkOrder work = new TWorkOrder();
        work.setWorkNo(StringUtils.hasText(orderNo) ? orderNo : generateWorkNo());'''
ok1 = old1 in c1
if ok1:
    c1 = c1.replace(old1, new1)
with io.open(p1, "w", encoding="utf-8", newline="\n") as f:
    f.write(c1)

# ========== 2) sales：下单自动拆单 ==========
p2 = r"D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java\com\guxian\sales\service\impl\TSalesOrderServiceImpl.java"
with io.open(p2, "r", encoding="utf-8") as f:
    c2 = f.read()

ok = {}
# 2.1 类上加 logger
old = "@Service\npublic class TSalesOrderServiceImpl extends ServiceImpl<TSalesOrderMapper, TSalesOrder> implements T"
new = "@Service\n@lombok.extern.slf4j.Slf4j\npublic class TSalesOrderServiceImpl extends ServiceImpl<TSalesOrderMapper, TSalesOrder> implements T"
ok["log"] = old in c2
if ok["log"]:
    c2 = c2.replace(old, new)

# 2.2 新增订单状态：待受理 -> 已受理（保存即拆单）
old = '''            // 新增：生成订单号，状态未受理
            order.setOrderNo(generateOrderNo());
            order.setOrderStatus(OrderStatusEnum.PENDING_AUDIT.getCode());'''
new = '''            // 新增：生成订单号，状态=已受理（保存后自动拆单生成生产工单）
            order.setOrderNo(generateOrderNo());
            order.setOrderStatus(OrderStatusEnum.AUDITED.getCode());'''
ok["status"] = old in c2
if ok["status"]:
    c2 = c2.replace(old, new)

# 2.3 明细落库后：新增订单同步明细状态=已受理，并自动拆单
old = '''        // ---------- 落明细，生成子单号 ----------
        int seq = 1;
        for (TSalesOrderItem item : entities) {
            item.setOrderId(order.getOrderId());
            item.setItemId(null);
            item.setSubOrderNo(buildSubOrderNo(order.getOrderNo(), seq++));
            orderItemMapper.insert(item);
        }
    }'''
new = '''        // ---------- 落明细，生成子单号 ----------
        int seq = 1;
        for (TSalesOrderItem item : entities) {
            item.setOrderId(order.getOrderId());
            item.setItemId(null);
            item.setSubOrderNo(buildSubOrderNo(order.getOrderNo(), seq++));
            orderItemMapper.insert(item);
        }

        // ---------- 新增订单：自动拆单生成生产工单（工单号=订单号） ----------
        if (!isEdit) {
            // 明细行状态与订单同步为已受理，保证拆单/生产侧数据一致
            updateItemStatus(order.getOrderId(), OrderStatusEnum.AUDITED.getCode(), null);
            autoSplitOrder(order.getOrderId());
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
            if (resp.statusCode() == 200 && body != null && body.contains("\\"code\\":200")) {
                log.info("下单自动拆单成功：orderId={}，已生成生产工单（工单号=订单号）", orderId);
            } else {
                log.warn("下单自动拆单未成功：orderId={}, http={}, resp={}", orderId,
                        resp.statusCode(), body == null ? "" : body.substring(0, Math.min(300, body.length())));
            }
        } catch (Exception e) {
            log.warn("下单自动拆单异常：orderId={}, msg={}", orderId, e.getMessage());
        }
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
    }'''
ok["autosplit"] = old in c2
if ok["autosplit"]:
    c2 = c2.replace(old, new)

# 2.4 startProduce 内 token 透传块复用 currentToken（保留原逻辑，减少重复）
old = '''        // 真正的开工 = 调用生产模块拆单，生成生产工单（拆单成功会自动回写订单状态为生产中）
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
            HttpClient client = HttpClient.newBuilder()'''
new = '''        // 真正的开工 = 调用生产模块拆单，生成生产工单（拆单成功会自动回写订单状态为生产中）
        try {
            // 透传当前登录用户的 token，生产模块拆单接口需要鉴权
            String token = currentToken();
            HttpClient client = HttpClient.newBuilder()'''
ok["start"] = old in c2
if ok["start"]:
    c2 = c2.replace(old, new)

with io.open(p2, "w", encoding="utf-8", newline="\n") as f:
    f.write(c2)

print("produce 工单号=订单号:", ok1)
print("sales log:", ok["log"])
print("sales 状态已受理:", ok["status"])
print("sales 自动拆单:", ok["autosplit"])
print("sales startProduce复用:", ok["start"])
