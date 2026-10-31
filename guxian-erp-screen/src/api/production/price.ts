import request from '@/api/request'

// ===== 工序产品计件价 =====
// 分页查询：按工序/产品名过滤
export const getPricePage = (params: any) =>
  request.get('/production/price/page', { params })

// 新增/修改单条
export const savePrice = (data: any) =>
  request.post('/production/price/save', data)

// 删除单条
export const delPrice = (id: number) =>
  request.delete(`/production/price/${id}`)

// Excel 批量导入（三列通用格式）
export const importPriceExcel = (file: File) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/production/price/import', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}

// 按当前工序导入：文件两列（产品名称|计件单价）
export const importPriceByProcess = (processId: number, file: File) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post(`/production/price/importByProcess/${processId}`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}

// 模板下载（返回 blob）
export const downloadPriceTemplate = () =>
  request.get('/production/price/template', { responseType: 'blob' })

// 按筛选条件导出 Excel
export const exportPriceExcel = (params: any) =>
  request.get('/production/price/export', { params, responseType: 'blob' })
