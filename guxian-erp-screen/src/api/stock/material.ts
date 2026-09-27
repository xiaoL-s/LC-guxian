import request from '@/api/request'
import type { Material, StockRecord, Shelf } from '@/types/production'

// ===== 物料 =====
export const getMaterialPage = (params: any) => request.get('/stock/material/page', { params })
export const listEnabledMaterial = () => request.get('/stock/material/listEnabled')
export const saveMaterial = (data: Material) => request.post('/stock/material/save', data)
export const delMaterial = (id: number) => request.delete(`/stock/material/${id}`)
export const changeStock = (data: any) => request.post('/stock/material/change', data)
export const batchOutStock = (data: any[]) => request.post('/stock/material/batchOut', data)

// ===== 库存流水 =====
export const getStockRecordPage = (params: any) => request.get('/stock/material/record/page', { params })

// ===== 货架 =====
export const getShelfList = () => request.get('/stock/shelf/list')
export const getShelfEnabled = () => request.get('/stock/shelf/listEnabled')
export const saveShelf = (data: Shelf) => request.post('/stock/shelf/save', data)
export const delShelf = (id: number) => request.delete(`/stock/shelf/${id}`)

// ===== 出入库单据 =====
export const createBill = (data: any) => request.post('/stock/bill/create', data)
export const getBillPage = (params: any) => request.get('/stock/bill/page', { params })

// ===== 库存盘点 =====
export const createCheck = (remark?: string) => request.post('/stock/check/create', null, { params: { remark } })
export const saveCheckItems = (checkId: number, items: any[]) => request.post('/stock/check/items/save', items, { params: { checkId } })
export const confirmCheck = (checkId: number) => request.post('/stock/check/confirm', null, { params: { checkId } })
export const getCheckPage = (params: any) => request.get('/stock/check/page', { params })
export const getCheckDetail = (checkId: number) => request.get(`/stock/check/${checkId}`)

// ===== 库存预警 =====
export const getWarnList = () => request.get('/stock/material/warn/list')
