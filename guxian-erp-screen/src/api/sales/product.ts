import request from '../request.ts'

// ===== 产品档案 =====
export function getProductPage(params: any) {
  return request({ url: '/sales/product/page', method: 'get', params })
}
export function getProductInfo(productId: number) {
  return request({ url: `/sales/product/${productId}`, method: 'get' })
}
export function saveProduct(data: any) {
  return request({ url: '/sales/product/save', method: 'post', data })
}
export function delProduct(productId: number) {
  return request({ url: `/sales/product/${productId}`, method: 'delete' })
}
export function listEnabledProduct() {
  return request({ url: '/sales/product/listEnabled', method: 'get' })
}

// ===== 产品BOM =====
export function getProductBom(productId: number) {
  return request({ url: `/sales/product/bom/${productId}`, method: 'get' })
}
export function saveProductBom(productId: number, data: any[]) {
  return request({ url: `/sales/product/bom/save/${productId}`, method: 'post', data })
}
export function listMaterials() {
  return request({ url: '/sales/product/materials', method: 'get' })
}
