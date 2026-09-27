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
}