export interface TCustomerDTO {
  customerId?: number
  customerName: string
  contact: string
  phone: string
  address: string
  remark: string
  createBy?: number
  createTime?: string
  updateBy?: number
  updateTime?: string
}

export interface TCustomerFollowDTO {
  id?: number
  customerId?: number
  followContent: string
  followTime?: string
  followUser: string
  createTime?: string
}
