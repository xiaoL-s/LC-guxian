/**
 * 按钮级权限控制
 *
 * 权限规则：
 * - admin / boss: 全部可编辑
 * - clerk(文员): 销售订单、客户管理可编辑
 * - prod_manager(生产主管): 生产管理可编辑
 * - finance(财务): 财务管理可编辑
 * - worker(生产工人): 只能报工，其他只读
 * - hezuofang(合作方): 全部只读
 */

// 各角色可编辑的模块
const EDITABLE_MODULES: Record<string, string[]> = {
  admin: ['*'],           // 全部
  boss: ['*'],            // 全部
  clerk: ['sales', 'customer'],
  prod_manager: ['produce', 'sales'],
  finance: ['finance'],
  worker: ['produce_report'],  // 只能报工
  hezuofang: []           // 只读
}

export function getRoleCodes(): string[] {
  try {
    const userStr = localStorage.getItem('loginUser')
    if (!userStr) return []
    const user = JSON.parse(userStr)
    return user.roleCodes || []
  } catch {
    return []
  }
}

/**
 * 判断当前用户是否有指定模块的编辑权限
 * @param module 模块名：sales/customer/produce/finance/system/report/stock
 */
export function hasEditPerm(module: string): boolean {
  const roles = getRoleCodes()
  if (roles.length === 0) return false

  // admin/boss 全部放行
  if (roles.includes('admin') || roles.includes('boss')) return true

  for (const role of roles) {
    const editable = EDITABLE_MODULES[role] || []
    if (editable.includes('*') || editable.includes(module)) {
      return true
    }
  }
  return false
}

/** 是否是超管（用于删除、撤回等高危操作） */
export function isAdmin(): boolean {
  try {
    const userStr = localStorage.getItem('loginUser')
    if (!userStr) return false
    const user = JSON.parse(userStr)
    return user.isAdmin === true
  } catch {
    return false
  }
}
