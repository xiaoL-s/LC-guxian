# -*- coding: utf-8 -*-
"""步骤3：Controller（save返回提示 / remove调service）+ MiniApi + GlobalExceptionHandler + 前端提示"""
import io

def read(p):
    with io.open(p, "r", encoding="utf-8") as f:
        return f.read()

def write(p, c):
    with io.open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(c)

def rep(p, old, new, tag):
    c = read(p)
    assert old in c, "锚点未命中: " + tag + " @" + p.split("\\")[-1]
    c = c.replace(old, new)
    write(p, c)
    print(tag + ": True")

# ---- 3.1 Controller：save 返回工单提示 ----
p = r"D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java\com\guxian\sales\controller\TSalesOrderController.java"
rep(p, '''    /** 新增/编辑订单（保存为订单未受理） */
    @PostMapping("/save")
    @OperLog(operModule = "销售订单", operType = "保存", operContent = "新增/编辑销售订单")
    public Result<Void> save(@RequestBody SalesOrderDTO dto) {
        orderService.saveOrder(dto);
        return Result.success();
    }''',
'''    /** 新增/编辑订单（保存为订单未受理；新增自动拆单，返回工单生成提示） */
    @PostMapping("/save")
    @OperLog(operModule = "销售订单", operType = "保存", operContent = "新增/编辑销售订单")
    public Result<String> save(@RequestBody SalesOrderDTO dto) {
        orderService.saveOrder(dto);
        // 新增订单自动拆单：返回工单生成情况，前端按此提示（拆单失败不再静默）
        return Result.success(orderService.checkWorkOrderTip(dto.getOrderId()));
    }''',
"Controller save")

rep(p, '''    /** 删除订单（逻辑删除，仅未受理/已驳回/已取消可删） */
    @DeleteMapping("/{orderId}")
    @OperLog(operModule = "销售订单", operType = "删除", operContent = "删除销售订单")
    public Result<Void> remove(@PathVariable("orderId") Long orderId) {
        TSalesOrder order = orderService.getById(orderId);
        if (order == null) {
            return Result.success();
        }
        // 已进入生产流程的订单不允许删除
        if (order.getOrderStatus() != null
                && order.getOrderStatus() >= 1 && order.getOrderStatus() <= 5) {
            return Result.fail("订单已进入业务流程，不允许删除，可取消");
        }
        orderService.removeById(orderId);
        return Result.success();
    }''',
'''    /** 删除订单（校验工单/业务流程后，先删明细再逻辑删订单） */
    @DeleteMapping("/{orderId}")
    @OperLog(operModule = "销售订单", operType = "删除", operContent = "删除销售订单")
    public Result<Void> remove(@PathVariable("orderId") Long orderId) {
        orderService.deleteOrder(orderId);
        return Result.success();
    }''',
"Controller remove")

# ---- 3.2 MiniApiApplication scanBasePackages ----
p = r"D:\Java\guxian\guxian-erp-parent\guxian-mini-api\src\main\java\com\guxian\mini\MiniApiApplication.java"
c = read(p)
old = "@SpringBootApplication"
new = "@SpringBootApplication(scanBasePackages = \"com.guxian\")"
if old in c:
    c = c.replace(old, new, 1)
    write(p, c)
    print("MiniApi scanBasePackages: True")
else:
    print("MiniApi 锚点未命中，人工检查")

# ---- 3.3 GlobalExceptionHandler 兜底返回真实信息 ----
p = r"D:\Java\guxian\guxian-erp-parent\guxian-common\guxian-common-core\src\main\java\com\guxian\exception\GlobalExceptionHandler.java"
rep(p, '''    /**
     * 兜底捕获所有未知系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("【系统未知异常】", e);
        return Result.fail(ResultCodeEnum.FAIL.getCode(), "系统繁忙，请稍后重试");
    }''',
'''    /**
     * 兜底捕获所有未知系统异常。
     * 返回精简后的异常信息（截断），便于前端定位问题、用户反馈，避免"系统繁忙"黑盒。
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("【系统未知异常】", e);
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = e.getClass().getSimpleName();
        }
        if (msg.length() > 120) {
            msg = msg.substring(0, 120) + "...";
        }
        return Result.fail(ResultCodeEnum.FAIL.getCode(), "系统异常：" + msg);
    }''',
"GlobalExceptionHandler")

# ---- 3.4 前端 OrderAdd.vue：提示使用后端 tip ----
p = r"D:\Java\guxian\guxian-erp-screen\src\views\sales\OrderAdd.vue"
rep(p, '''    const res: any = await saveOrder(payload)
    if (res.code === 200) {
      ElMessage.success(order.orderId ? '修改成功，已重新提交受理并生成生产工单' : '订单已保存，已自动生成生产工单（工单号=订单号）')
      router.push('/sales/order/entry')
    }''',
'''    const res: any = await saveOrder(payload)
    if (res.code === 200) {
      // 新增订单：后端返回工单生成情况（拆单失败也会明确提示，不再静默）
      ElMessage.success(order.orderId ? '修改成功' : (res.data || '订单已保存'))
      router.push('/sales/order/entry')
    }''',
"前端提示")

print("全部完成")
