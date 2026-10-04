# -*- coding: utf-8 -*-
"""
方案A落地：
1. 新增 WorkOrderCheckMapper（查工单）
2. sales service：编辑工单校验、删除订单（校验+删明细+删订单）、拆单失败标记、日志埋点、checkWorkOrderTip
3. sales controller：save 返回工单提示、remove 调 service
4. MiniApiApplication scanBasePackages
5. GlobalExceptionHandler 兜底返回真实信息
6. 前端 OrderAdd 提示使用后端 tip
"""
import io, os

def read(p):
    with io.open(p, "r", encoding="utf-8") as f:
        return f.read()

def write(p, c):
    with io.open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(c)

# ============ 1. 新增 WorkOrderCheckMapper ============
mapper_p = r"D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java\com\guxian\sales\mapper\WorkOrderCheckMapper.java"
mapper_code = '''package com.guxian.sales.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 只读访问生产模块工单表（同库共享）。
 * 用于销售订单“编辑/删除”前置校验：已生成生产工单的订单禁止修改与删除，
 * 防止销售单与生产工单数据不一致。
 */
@Mapper
public interface WorkOrderCheckMapper {

    /** 订单已生成的工单数量（未逻辑删除） */
    @Select("SELECT COUNT(*) FROM t_work_order WHERE sales_order_id = #{orderId} AND del_flag = 0")
    int countBySalesOrderId(@Param("orderId") Long orderId);

    /** 订单最新工单号（拆单成功提示用） */
    @Select("SELECT work_no FROM t_work_order WHERE sales_order_id = #{orderId} AND del_flag = 0 ORDER BY work_id DESC LIMIT 1")
    String latestWorkNo(@Param("orderId") Long orderId);
}
'''
if not os.path.exists(mapper_p):
    write(mapper_p, mapper_code)
    print("新建 WorkOrderCheckMapper: True")
else:
    print("WorkOrderCheckMapper 已存在，跳过")

# ============ 2. Service 接口 ============
svc_p = r"D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java\com\guxian\sales\service\TSalesOrderService.java"
c = read(svc_p)
old = '''    /** 字典下拉选项：产品(style_* 系列) + 颜色/网子/把手/锁具/加杆等属性 */
    Map<String, List<DictOptionVO>> dictOptions();
}'''
new = '''    /** 字典下拉选项：产品(style_* 系列) + 颜色/网子/把手/锁具/加杆等属性 */
    Map<String, List<DictOptionVO>> dictOptions();

    /** 保存后工单生成情况提示（新增订单自动拆单后调用；无工单时提示手动开工补齐） */
    String checkWorkOrderTip(Long orderId);

    /** 删除订单：校验工单/业务流程后，先删明细再逻辑删订单 */
    void deleteOrder(Long orderId);
}'''
assert old in c, "接口锚点未命中"
c = c.replace(old, new)
write(svc_p, c)
print("Service 接口: True")

print("STEP1 完成")
