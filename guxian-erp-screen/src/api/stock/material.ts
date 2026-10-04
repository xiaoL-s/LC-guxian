import request from '@/api/request'
import type { Shelf } from '@/types/production'

// ===== 货架（成品入库用） =====
export const getShelfList = () => request.get('/stock/shelf/list')
export const getShelfEnabled = () => request.get('/stock/shelf/listEnabled')
export const saveShelf = (data: Shelf) => request.post('/stock/shelf/save', data)
export const delShelf = (id: number) => request.delete(`/stock/shelf/${id}`)
