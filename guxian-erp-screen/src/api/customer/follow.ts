import request from '../request.ts'
import type { TCustomerFollowDTO } from '@/types/customer.ts'

// 按客户ID分页查询跟进记录
export function getFollowPage(params: any) {
  return request({
    url: '/customer/follow/page',
    method: 'get',
    params
  })
}

// 新增/编辑跟进记录
export function saveFollow(data: TCustomerFollowDTO) {
  return request({
    url: '/customer/follow/save',
    method: 'post',
    data
  })
}

// 删除跟进记录
export function delFollow(id: number) {
  return request({
    url: `/customer/follow/delete/${id}`,
    method: 'delete'
  })
}
