import request from '../request.ts'

/**
 * 分页查询打印模板
 * @param pageNum 页码
 * @param pageSize 每页条数
 * @param templateName 模板名称（模糊）
 * @param templateType 单据类型
 * @param status 状态：1=启用 0=禁用，传空=全部
 */
export function getPrintTemplatePage(pageNum: number,
                                     pageSize: number,
                                     templateName: string,
                                     templateType: string,
                                     status: number | '' = '') {
  const params: Record<string, any> = { pageNum, pageSize, templateName, templateType }
  if (status !== '' && status !== null && status !== undefined) {
    params.status = status
  }
  return request({
    url: '/system/printTemplate/page',
    method: 'get',
    params
  })
}

/** 新增/编辑 */
export function savePrintTemplate(data: any) {
  return request({
    url: '/system/printTemplate/save',
    method: 'post',
    data
  })
}

/** 删除（逻辑删除） */
export function delPrintTemplate(templateId: number) {
  return request({
    url: `/system/printTemplate/delete/${templateId}`,
    method: 'delete'
  })
}

/** 编辑回显 */
export function getPrintTemplateById(templateId: number) {
  return request({
    url: `/system/printTemplate/getById/${templateId}`,
    method: 'get'
  })
}

/** 打印模板启用 / 禁用切换 */
export function togglePrintTemplateStatus(templateId: number, status: number) {
  return request({
    url: `/system/printTemplate/toggleStatus/${templateId}`,
    method: 'put',
    params: { status }
  })
}

/** 所有去重后的单据类型（下拉用） */
export function getPrintTemplateTypes() {
  return request({
    url: '/system/printTemplate/types',
    method: 'get'
  })
}

/** 模板预览（返回原始模板内容，前端用模拟数据渲染） */
export function getTemplatePreview(templateId: number) {
  return request({
    url: `/system/printTemplate/preview/${templateId}`,
    method: 'get'
  })
}
