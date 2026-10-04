import request from '../request.ts'

//字典类型
export function getDictTypePage(params: any) {
  return request({ url: '/system/dict/type/page', method: 'get', params })
}
export function addDictType(data: any) {
  return request({ url: '/system/dict/type/save', method: 'post', data })
}
export function updateDictType(data: any) {
  return request({ url: '/system/dict/type/save', method: 'post', data })
}
export function delDictType(id: number) {
  return request({ url: `/system/dict/type/${id}`, method: 'delete' })
}
export function getDictTypeInfo(id: number) {
  return request({ url: `/system/dict/type/${id}`, method: 'get' })
}

//字典数据
export function getDictDataPage(params: any) {
  return request({ url: '/system/dict/data/page', method: 'get', params })
}
export function addDictData(data: any) {
  return request({ url: '/system/dict/data/save', method: 'post', data })
}
export function updateDictData(data: any) {
  return request({ url: '/system/dict/data/save', method: 'post', data })
}
export function delDictData(id: number) {
  return request({ url: `/system/dict/data/${id}`, method: 'delete' })
}
export function getDictDataInfo(id: number) {
  return request({ url: `/system/dict/data/${id}`, method: 'get' })
}
//产品公式测试（字典项公式配置 + 输入参数 → 下料/剪网/面积/金额）
export function testDictFormula(data: any) {
  return request({ url: '/system/dict/data/formula/test', method: 'post', data })
}
