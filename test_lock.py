# -*- coding: utf-8 -*-
# 登录并测试拦截逻辑
import json, urllib.request, io

def post(url, body, token=None):
    req = urllib.request.Request(url, data=json.dumps(body).encode("utf-8"), method="POST")
    req.add_header("Content-Type", "application/json")
    if token: req.add_header("Authorization", token)
    with urllib.request.urlopen(req, timeout=10) as r:
        return json.loads(r.read().decode("utf-8"))

def delete(url, token):
    req = urllib.request.Request(url, method="DELETE")
    if token: req.add_header("Authorization", token)
    with urllib.request.urlopen(req, timeout=10) as r:
        return json.loads(r.read().decode("utf-8"))

# 登录
with io.open(r"D:\Java\guxian\lg.json", "r", encoding="utf-8-sig") as f:
    lg = json.load(f)
r = post("http://127.0.0.1:8081/system/login/doLogin", lg)
token = "Bearer " + r["data"]["token"]
print("登录:", r["code"], "token长度", len(token))

# 1. 编辑有工单订单 order 17 -> 应拒
body = {"orderId": 17, "customerId": 1, "itemList": [{"productName": "测试", "width": 500, "height": 800}]}
print("1 编辑有工单(order17):", post("http://127.0.0.1:8083/sales/order/save", body, token))

# 2. 删除有工单订单 order 17 -> 应拒
print("2 删除有工单(order17):", delete("http://127.0.0.1:8083/sales/order/17", token))

# 3. 删除无工单但已受理 order 16 -> 应拒(已进入业务流程)
print("3 删除已受理(order16):", delete("http://127.0.0.1:8083/sales/order/16", token))
