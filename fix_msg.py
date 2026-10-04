# -*- coding: utf-8 -*-
import io
p = r'D:\Java\guxian\guxian-erp-screen\src\views\sales\OrderAdd.vue'
with io.open(p, 'r', encoding='utf-8') as f:
    c = f.read()
old = "ElMessage.success(order.orderId ? '修改成功，已重新提交受理' : '订单已保存，状态：订单未受理')"
new = "ElMessage.success(order.orderId ? '修改成功，已重新提交受理并生成生产工单' : '订单已保存，已自动生成生产工单（工单号=订单号）')"
print('命中:', old in c)
c = c.replace(old, new)
with io.open(p, 'w', encoding='utf-8', newline='\n') as f:
    f.write(c)
