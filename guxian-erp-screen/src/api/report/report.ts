import request from '@/api/request'

/** 销售看板：指标卡 + 下单趋势 + 产品销量排名 + 客户销量排名（report 8088） */
export const getSalesOverview = () => request.get('/report/sales/overview')

/** 生产看板：工单统计 + 生产趋势 + 工序产量排名 + 工人产量排名 */
export const getProductionOverview = () => request.get('/report/production/overview')

/** 库存看板：入库统计 + 入库趋势 + 库位入库排名 */
export const getStockOverview = () => request.get('/report/stock/overview')

/** 利润看板：月销售/月利润/毛利率 + 12月利润趋势 + 产品利润排名 */
export const getProfitOverview = () => request.get('/report/profit/overview')
