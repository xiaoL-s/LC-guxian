import request from '../request.ts'

// 订单分页（订单维度）
export function getOrderPage(params: any) {
  return request({ url: '/sales/order/page', method: 'get', params })
}
// 订单明细分页（产品行/子单维度：下单、订单明细页面共用）
export function getOrderItemPage(params: any) {
  return request({ url: '/sales/order/item/page', method: 'get', params })
}
// 订单明细底部加载统计
export function getOrderItemStats(params: any) {
  return request({ url: '/sales/order/item/stats', method: 'get', params })
}
// 字典下拉选项（产品 style_* 系列 + 颜色/网子/把手/锁具等）
export function getOrderDictOptions() {
  return request({ url: '/sales/order/dict/options', method: 'get' })
}
// 订单详情
export function getOrderInfo(orderId: number) {
  return request({ url: `/sales/order/${orderId}`, method: 'get' })
}
// 新增/编辑订单
export function saveOrder(data: any) {
  return request({ url: '/sales/order/save', method: 'post', data })
}
// 受理通过
export function auditPassOrder(orderId: number) {
  return request({ url: `/sales/order/audit/pass/${orderId}`, method: 'put' })
}
// 驳回
export function auditRejectOrder(orderId: number, rejectReason: string) {
  return request({ url: `/sales/order/audit/reject/${orderId}`, method: 'put', data: { rejectReason } })
}
// 开工（生产预留）
export function startProduceOrder(orderId: number) {
  return request({ url: `/sales/order/produce/start/${orderId}`, method: 'put' })
}
// 完工（生产预留）
export function finishProduceOrder(orderId: number) {
  return request({ url: `/sales/order/produce/finish/${orderId}`, method: 'put' })
}
// 发货
export function deliverOrder(orderId: number) {
  return request({ url: `/sales/order/deliver/${orderId}`, method: 'put' })
}
// 完成
export function completeOrder(orderId: number) {
  return request({ url: `/sales/order/complete/${orderId}`, method: 'put' })
}
// 取消
export function cancelOrder(orderId: number, cancelReason: string) {
  return request({ url: `/sales/order/cancel/${orderId}`, method: 'put', data: { cancelReason } })
}
// 收款登记
export function receiveOrderPayment(orderId: number, data: { amount: number; remark?: string }) {
  return request({ url: `/sales/order/receive/${orderId}`, method: 'put', data })
}
// 结清
export function settleOrder(orderId: number) {
  return request({ url: `/sales/order/settle/${orderId}`, method: 'put' })
}
// 删除
export function delOrder(orderId: number) {
  return request({ url: `/sales/order/${orderId}`, method: 'delete' })
}
