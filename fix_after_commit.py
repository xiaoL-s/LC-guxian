# -*- coding: utf-8 -*-
"""下单自动拆单改为事务提交后(afterCommit)触发，避免未提交数据导致生产模块查不到明细"""
import io

p = r"D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java\com\guxian\sales\service\impl\TSalesOrderServiceImpl.java"
with io.open(p, "r", encoding="utf-8") as f:
    c = f.read()

old = '''        // ---------- 新增订单：自动拆单生成生产工单（工单号=订单号） ----------
        if (!isEdit) {
            // 明细行状态与订单同步为已受理，保证拆单/生产侧数据一致
            updateItemStatus(order.getOrderId(), OrderStatusEnum.AUDITED.getCode(), null);
            autoSplitOrder(order.getOrderId());
        }
    }'''
new = '''        // ---------- 新增订单：事务提交后自动拆单生成生产工单（工单号=订单号） ----------
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
    }'''
ok = old in c
if ok:
    c = c.replace(old, new)
with io.open(p, "w", encoding="utf-8", newline="\n") as f:
    f.write(c)
print("事务提交后自动拆单:", ok)
