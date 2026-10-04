# -*- coding: utf-8 -*-
# 新增订单 -> 验证保存返回工单提示 + 工单号=订单号
import json, urllib.request, io

def post(url, body, token):
    req = urllib.request.Request(url, data=json.dumps(body).encode("utf-8"), method="POST")
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", token)
    with urllib.request.urlopen(req, timeout=20) as r:
        return json.loads(r.read().decode("utf-8"))

with io.open(r"D:\Java\guxian\lg.json", "r", encoding="utf-8-sig") as f:
    lg = json.load(f)
r = post("http://127.0.0.1:8081/system/login/doLogin", lg, None)
token = "Bearer " + r["data"]["token"]

# 新增订单：客户13 罗普斯金，产品 110款（dictType=style_kuangzhongkuang, dictValue=9），500x800
body = {
    "customerId": 13,
    "customerName": "罗普斯金",
    "itemList": [{
        "productId": None,
        "productName": "拆洗款护童-压条款（110款）",
        "itemCategory": "框中框系列",
        "dictType": "style_kuangzhongkuang",
        "dictValue": "9",
        "width": 500, "height": 800,
        "num": 1,
        "unitPrice": 110,
        "color": "灰色",
        "netMaterial": "超清网",
        "addRod": "全防护",
        "fixedBottom": "0",
        "unit": "套"
    }],
    "orderDate": "2026-10-04"
}
resp = post("http://127.0.0.1:8083/sales/order/save", body, token)
print("保存返回:", json.dumps(resp, ensure_ascii=False))
