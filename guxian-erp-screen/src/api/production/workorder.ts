import request from '@/api/request'
import type { ProcessDict, WorkReportRow } from '@/types/production'

// ===== 工序 =====
export const getProcessList = () => request.get('/production/process/list')
export const getProcessEnabled = () => request.get('/production/process/listEnabled')
export const saveProcess = (data: ProcessDict) => request.post('/production/process/save', data)
export const delProcess = (id: number) => request.delete(`/production/process/${id}`)

// ===== 工单 =====
export const getAuditedOrders = (keyword?: string) => request.get('/production/workorder/auditedOrders', { params: { keyword } })
export const splitWorkOrder = (salesOrderId: number, shelfId?: number, remark?: string) =>
  request.post('/production/workorder/split', null, { params: { salesOrderId, shelfId, remark } })
export const getWorkOrderPage = (params: any) => request.get('/production/workorder/page', { params })
export const getWorkOrderDetail = (workId: number) => request.get(`/production/workorder/${workId}`)
export const pickMaterials = (workId: number, pickList: any[]) =>
  request.post('/production/workorder/pick', pickList, { params: { workId } })
export const reportProcess = (data: any) => request.post('/production/workorder/report', data)
export const finishInstock = (workId: number, shelfId?: number, remark?: string) =>
  request.post('/production/workorder/finish', null, { params: { workId, shelfId, remark } })

// ===== 报工记录 =====
export const getReportPage = (params: any) => request.get('/production/report/page', { params })
export const getWageSummary = (params: any) => request.get('/production/report/wage/summary', { params })
export const getWageByWorker = (params: any) => request.get('/production/report/wage/byWorker', { params })

// ===== 公共只读 =====
export const getUsers = () => request.get('/production/common/users')
export const getInstockList = (params: any) => request.get('/production/common/instock', { params })
