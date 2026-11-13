import request from '../request.ts'
import type { LoginDTO } from '@/types/system.ts'

// 对接后端登录接口 POST /system/login/doLogin
export function loginApi(data: LoginDTO) {
  return request.post('/system/login/doLogin', data)
}

/**
 * 登出接口 POST /system/logout
 * 服务端会把当前 token 拉黑，退出后旧 token 立即失效（不再等到自然过期）
 */
export function logoutApi() {
  return request.post('/system/logout')
}
