import request from '../request.ts'
import type { TCustomerDTO } from '@/types/customer.ts'

// 客户分页查询
export function getCustomerPage(params: any) {
  return request({
    url: '/customer/list/page',
    method: 'get',
    params
  })
}

// 获取客户详情
export function getCustomerInfo(customerId: number) {
  return request({
    url: `/customer/list/${customerId}`,
    method: 'get'
  })
}

// 新增/编辑客户
export function saveCustomer(data: TCustomerDTO) {
  return request({
    url: '/customer/list/save',
    method: 'post',
    data
  })
}

// 删除单个客户
export function delCustomer(customerId: number) {
  return request({
    url: `/customer/list/${customerId}`,
    method: 'delete'
  })
}

// 批量删除
export function batchDelCustomer(ids: number[]) {
  return request({
    url: '/customer/list/batch',
    method: 'delete',
    data: ids
  })
}

// 获取全部客户下拉选项（订单页使用）
export function getAllCustomerList() {
  return request({
    url: '/customer/list/listAll',
    method: 'get'
  })
}
