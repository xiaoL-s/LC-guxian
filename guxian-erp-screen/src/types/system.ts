// 登录请求参数
export interface LoginDTO {
  username: string
  password: string
}

// 后端统一返回格式
export interface Result<T> {
  code: number
  msg: string
  data: T
}

// 登录返回用户信息
export interface LoginUser {
  id: number
  username: string
  realName: string
  /** 是否超级管理员：生产工单撤回等高风险操作仅超管可用 */
  isAdmin?: boolean
}
