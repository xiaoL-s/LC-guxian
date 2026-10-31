import request from '@/api/request'

/** ============ 收款账户 ============ */
export const getAccountList = () => request.get('/finance/account/list')
export const saveAccount = (data: any) => request.post('/finance/account', data)
export const deleteAccount = (id: number) => request.delete(`/finance/account/${id}`)

/** ============ 收款明细 ============ */
export const getRecordPage = (params: any) => request.get('/finance/record/page', { params })
export const saveRecord = (data: any) => request.post('/finance/record', data)
export const deleteRecord = (id: number) => request.delete(`/finance/record/${id}`)

/** ============ 合并收款 ============ */
export const mergeCollect = (data: any) => request.post('/finance/merge', data)

/** ============ 成品应收 ============ */
export const getReceivablePage = (params: any) => request.get('/finance/receivable/page', { params })
export const getReceivableSummary = (params: any) => request.get('/finance/receivable/summary', { params })

/** ============ 客户往来 ============ */
export const getCustomerFinancePage = (params: any) => request.get('/finance/customer/page', { params })
export const getCustomerFinanceSummary = (params: any) => request.get('/finance/customer/summary', { params })
export const rechargeCustomer = (data: any) => request.post('/finance/customer/recharge', data)
