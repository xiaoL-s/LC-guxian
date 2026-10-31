import request from '@/api/request'

/** 计件工资汇总（财务管理 8087） */
export const getWageSummary = (params: any) => request.get('/finance/wage/summary', { params })

/** 按工人汇总计件工资 */
export const getWageByWorker = (params: any) => request.get('/finance/wage/byWorker', { params })

/** 计件工资明细（分页，按 员工×工单×工序 聚合） */
export const getWageDetail = (params: any) => request.get('/finance/wage/detail', { params })

/** 月度工资确认列表 */
export const getMonthWageList = (params: any) => request.get('/finance/wage/month', { params })

/** 生成/保存月度工资 */
export const saveMonthWage = (data: any) => request.post('/finance/wage/month', data)

/** 删除月度工资记录 */
export const deleteMonthWage = (id: number) => request.delete(`/finance/wage/month/${id}`)
